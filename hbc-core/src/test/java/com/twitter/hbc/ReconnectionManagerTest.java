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
    ReconnectionManager b = new ReconnectionManager(10);

    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 3, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 4, b.incrAndGetLinearBackoff());

    b.resetCounts();

    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
  }

  /**
   * Backoff manager can back off exponentially
   */
  @Test
  public void testExponentialBackoff() {
    ReconnectionManager b = new ReconnectionManager(10);

    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS, b.incrAndGetExponentialBackoff());
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 2, b.incrAndGetExponentialBackoff());
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 4, b.incrAndGetExponentialBackoff());
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 8, b.incrAndGetExponentialBackoff());

    b.resetCounts();

    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS, b.incrAndGetExponentialBackoff());
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 2, b.incrAndGetExponentialBackoff());
  }

  /**
   * Backoff manager backs off correctly when switching from one to the other
   */
  @Test
  public void testBackoffSwitching() {
    ReconnectionManager b = new ReconnectionManager(10);

    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 3, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 4, b.incrAndGetLinearBackoff());

    // should automatically restart counts
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS, b.incrAndGetExponentialBackoff());
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 2, b.incrAndGetExponentialBackoff());
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 4, b.incrAndGetExponentialBackoff());
    assertEquals(ReconnectionManager.INITIAL_EXPONENTIAL_BACKOFF_MILLIS * 8, b.incrAndGetExponentialBackoff());

    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS, b.incrAndGetLinearBackoff());
    assertEquals(ReconnectionManager.INITIAL_LINEAR_BACKOFF_MILLIS * 2, b.incrAndGetLinearBackoff());
  }

  @Test
  public void testRetries() {
    int retries = 10;
    ReconnectionManager b = new ReconnectionManager(retries);

    for (int i = 0; i < retries; i++) {
      assertTrue(b.shouldReconnectOn400s());
    }

    assertFalse(b.shouldReconnectOn400s());
    assertFalse(b.shouldReconnectOn400s());

    b.resetCounts();

    assertTrue(b.shouldReconnectOn400s());
  }
}
