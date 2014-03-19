/**
 * Copyright 2013 Twitter, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package com.twitter.hbc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Tracks the rate of a recurring event with a sliding window.
 * Threadsafe
 */
public class BasicRateTracker implements RateTracker {

    private final int granularityMillis;
    private final int numBuckets;

    private final RateUpdater rateUpdater;

    private ScheduledFuture<?> future;
    private final ScheduledExecutorService executor;

    public BasicRateTracker(int granularityMillis, int numBuckets, boolean startPaused, ScheduledExecutorService executor) {
        Preconditions.checkArgument(numBuckets > 0);
        Preconditions.checkArgument(granularityMillis > 0);
        Preconditions.checkArgument(granularityMillis / numBuckets > 0);

        this.granularityMillis = granularityMillis;
        this.numBuckets = numBuckets;
        this.executor = Preconditions.checkNotNull(executor);

        this.rateUpdater = new RateUpdater(startPaused);
    }

    @Override
    public void eventObserved() {
        rateUpdater.eventObserved();
    }

    /**
     * Pauses the rate tracker: the rate will be frozen.
     */
    @Override
    public void pause() {
        rateUpdater.pause();
    }

    @Override
    public void resume() {
        rateUpdater.resume();
    }

    @Override
    public void start() {
        this.future = executor.scheduleAtFixedRate(rateUpdater, granularityMillis / numBuckets, granularityMillis / numBuckets, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops tracking the rate
     */
    @Override
    public void stop() {
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * Stops and shuts down the underlying executor
     */
    @Override
    public void shutdown() {
        stop();
        executor.shutdown();
    }

    /**
     * Only used for testing
     */
    @VisibleForTesting
    void recalculate() {
        rateUpdater.run();
    }

    /**
     * @return the current rate if it is available, NaN if not.
     * The rate is unavailable if <code>granularityMillis</code> hasn't elapsed
     */
    public double getCurrentRateSeconds() {
        return rateUpdater.getCurrentRateSeconds();
    }

    class RateUpdater implements Runnable {

        private final int[] buckets;
        private final Object lock;

        private boolean paused;
        private double rate;

        private boolean rateValid;
        private int total;
        private int currentBucket;
        private boolean previouslyPaused;
        private int currentBucketCount;

        RateUpdater() {
            this(false);
        }

        RateUpdater(boolean paused) {
            this.rateValid = false;
            this.buckets = new int[numBuckets];
            this.paused = paused;
            this.rate = Double.NaN;
            this.lock = new Object();
        }

        @Override
        public void run() {
            synchronized (lock) {
                int currentCount = currentBucketCount;
                currentBucketCount = 0;

                if (paused) {
                    previouslyPaused = true;
                    return;
                }

                // skip the first estimation after a pause, since it could be in the middle of an estimation
                if (previouslyPaused) {
                    previouslyPaused = false;
                    return;
                }

                int prevBucket = currentBucket;
                currentBucket = (currentBucket + 1) % numBuckets;

                if (currentBucket == 0) {
                    // we've wrapped around again. this rate is now valid
                    rateValid = true;
                }

                int prevBucketCount = buckets[prevBucket];
                buckets[prevBucket] = currentCount;

                total += currentCount - prevBucketCount;
                if (rateValid) {
                    rate = total * 1000d / granularityMillis;
                }
            }
        }

        public void eventObserved() {
            synchronized (lock) {
                currentBucketCount++;
            }
        }

        public void pause() {
            synchronized (lock) {
                paused = true;
            }
        }

        public void resume() {
            synchronized (lock) {
                paused = false;
            }
        }

        public double getCurrentRateSeconds() {
            synchronized (lock) {
                return rate;
            }
        }
    }
}
