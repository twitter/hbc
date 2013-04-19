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

/**
 * Tracks the rate of a recurring event with a sliding window.
 * Threadsafe
 */
public interface RateTracker {

  public void eventObserved();

  /**
   * Pauses the rate tracker: the rate will be frozen.
   */
  public void pause();

  public void resume();

  public void start();

  /**
   * Stops tracking the rate
   */
  public void stop();

  /**
   * Stops and shuts down the underlying executor
   */
  public void shutdown();

  /**
   * @return the current rate if it is available, NaN if not.
   * The rate is unavailable if <code>granularityMillis</code> hasn't elapsed
   */
  public double getCurrentRateSeconds();
}