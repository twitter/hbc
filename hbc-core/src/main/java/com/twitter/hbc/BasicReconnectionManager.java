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
import com.twitter.hbc.core.Constants;

/**
 * This manages all of the reconnection logic. Mostly just keeps a bunch of information about whether we should
 * reconnect at all, how much we should backfill, and how much to back off from connection failures.
 */
public class BasicReconnectionManager implements ReconnectionManager {

  public static final int INITIAL_EXPONENTIAL_BACKOFF_MILLIS = 5000;
  public static final int INITIAL_LINEAR_BACKOFF_MILLIS = 250;

  public static final int MAX_LINEAR_BACKOFF_MILLIS = 16000;
  public static final int MAX_EXPONENTIAL_BACKOFF_MILLIS = 320000;

  private final int maxRetries;

  private int currentRetryCount;
  private int exponentialBackoffCount;
  private int linearBackoffCount;
  private int backoffMillis;

  public BasicReconnectionManager(int maxRetries) {
    this.maxRetries = maxRetries;
    this.backoffMillis = Constants.MIN_BACKOFF_MILLIS;
  }

  @Override
  public void handleExponentialBackoff() {
    handleBackoff(incrAndGetExponentialBackoff());
  }

  @Override
  public void handleLinearBackoff() {
    handleBackoff(incrAndGetLinearBackoff());
  }

  @Override
  public boolean shouldReconnectOn400s() {
    currentRetryCount++;
    return currentRetryCount <= maxRetries;
  }

  @Override
  public int estimateBackfill(double tps) {
    int upperBound = Math.min(Constants.MAX_BACKOFF_COUNT, (int) tps * (backoffMillis));
    return Math.max(upperBound, Constants.MIN_BACKOFF_MILLIS);
  }

  @Override
  public void resetCounts() {
    linearBackoffCount = 0;
    exponentialBackoffCount = 0;
    currentRetryCount = 0;
    backoffMillis = 0;
  }

  private void handleBackoff(int millis) {
    backoffMillis += millis;
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      // TODO: log an error
    }
  }

  @VisibleForTesting
  int incrAndGetExponentialBackoff() {
    linearBackoffCount = 0;
    exponentialBackoffCount += 1;
    return calculateExponentialBackoffMillis();
  }

  @VisibleForTesting
  int incrAndGetLinearBackoff() {
    exponentialBackoffCount = 0;
    linearBackoffCount += 1;
    return calculateLinearBackoffMillis();
  }

  private int calculateExponentialBackoffMillis() {
    assert(exponentialBackoffCount > 0);
    return Math.min(MAX_EXPONENTIAL_BACKOFF_MILLIS, INITIAL_EXPONENTIAL_BACKOFF_MILLIS << (exponentialBackoffCount - 1));
  }

  private int calculateLinearBackoffMillis() {
    return Math.min(MAX_LINEAR_BACKOFF_MILLIS, INITIAL_LINEAR_BACKOFF_MILLIS * linearBackoffCount);
  }
}
