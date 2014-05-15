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

import com.twitter.hbc.core.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReconnectionManagerTest {
  /**
   * Backoff manager can back off linearly
   */
  @Test
  public void testLinearBackoff() {
    BasicReconnectionManager b = new BasicReconnectionManager(10);

    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 3, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 4, b.incrAndGetLinearBackoff());

    b.resetCounts();

    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
  }

  /**
   * Backoff manager can back off exponentially
   */
  @Test
  public void testExponentialBackoff() {
    BasicReconnectionManager b = new BasicReconnectionManager(10);

    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS, b.incrAndGetExponentialBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 2, b.incrAndGetExponentialBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 4, b.incrAndGetExponentialBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 8, b.incrAndGetExponentialBackoff());

    b.resetCounts();

    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS, b.incrAndGetExponentialBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 2, b.incrAndGetExponentialBackoff());
  }

  /**
   * Backoff manager backs off correctly when switching from one to the other
   */
  @Test
  public void testBackoffSwitching() {
    BasicReconnectionManager b = new BasicReconnectionManager(10);

    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 3, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 4, b.incrAndGetLinearBackoff());

    // should automatically restart counts
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS, b.incrAndGetExponentialBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 2, b.incrAndGetExponentialBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 4, b.incrAndGetExponentialBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 8, b.incrAndGetExponentialBackoff());

    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(BasicReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
  }

  @Test
  public void testEstimateBackfill() {
    ReconnectionManager rm = new BasicReconnectionManager(1);

    // some negative value should use the lower bound
    assertEquals(Constants.MIN_BACKOFF_MILLIS, rm.estimateBackfill(-1d));

    // some large value should use the upper bound
    assertEquals(Constants.MAX_BACKOFF_COUNT, rm.estimateBackfill(1000000d));

    // a value in the middle
    assertEquals(Constants.MIN_BACKOFF_MILLIS * 30, rm.estimateBackfill(30d));
  }

  @Test
  public void testRetries() {
    int retries = 10;
    ReconnectionManager b = new BasicReconnectionManager(retries);

    for (int i = 0; i < retries; i++) {
      assertTrue(b.shouldReconnectOn400s());
    }

    assertFalse(b.shouldReconnectOn400s());
    assertFalse(b.shouldReconnectOn400s());

    b.resetCounts();

    assertTrue(b.shouldReconnectOn400s());
  }
}
