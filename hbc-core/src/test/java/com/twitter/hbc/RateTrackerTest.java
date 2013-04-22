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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RateTrackerTest {

  private ScheduledExecutorService scheduler;

  @Before
  public void setup() {
    scheduler = mock(ScheduledExecutorService.class);
  }

  @Test
  public void testRateTracking() {
    int millis = 100;
    int buckets = 10;
    BasicRateTracker rateTracker = new BasicRateTracker(millis, buckets, false, scheduler);
    assertTrue(Double.isNaN(rateTracker.getCurrentRateSeconds()));

    // 10 buckets, each bucket represents 10 millis, so 100 events / s
    for (int i = 0; i < buckets; i++) {
      rateTracker.eventObserved();
      rateTracker.recalculate();
    }

    assertEquals(100.0, rateTracker.getCurrentRateSeconds());
  }

  @Test
  public void testRateConsistency() {
    int millis = 100;
    int buckets = 10;
    BasicRateTracker rateTracker = new BasicRateTracker(millis, buckets, false, scheduler);
    assertTrue(Double.isNaN(rateTracker.getCurrentRateSeconds()));

    // 10 buckets, each bucket represents 10 millis, so 100 events / s
    for (int i = 0; i < buckets; i++) {
      rateTracker.eventObserved();
      rateTracker.recalculate();
    }

    assertEquals(100.0, rateTracker.getCurrentRateSeconds());
    for (int i = 0; i < buckets; i++) {
      rateTracker.eventObserved();
    }
    assertEquals(100.0, rateTracker.getCurrentRateSeconds());

    rateTracker.recalculate();
    assertFalse(100.0 == rateTracker.getCurrentRateSeconds());
  }


  @Test
  public void testSlidingWindow() {
    int millis = 100;
    int buckets = 2;
    BasicRateTracker rateTracker = new BasicRateTracker(millis, buckets, false, scheduler);
    assertTrue(Double.isNaN(rateTracker.getCurrentRateSeconds()));

    // 2 buckets, each bucket represents 10 millis, so 20 events / s
    for (int i = 0; i < buckets; i++) {
      rateTracker.eventObserved();
      rateTracker.recalculate();
    }
    // [1, 1]
    assertEquals(20.0, rateTracker.getCurrentRateSeconds());

    // [1, 2]
    rateTracker.eventObserved();
    rateTracker.eventObserved();
    rateTracker.recalculate();
    assertEquals(30.0, rateTracker.getCurrentRateSeconds());

    // [2, 3]
    rateTracker.eventObserved();
    rateTracker.eventObserved();
    rateTracker.eventObserved();
    rateTracker.recalculate();
    assertEquals(50.0, rateTracker.getCurrentRateSeconds());

    // [3, 0]
    rateTracker.recalculate();
    assertEquals(30.0, rateTracker.getCurrentRateSeconds());
  }

  @Test
  public void testPausing() {
    int millis = 100;
    int buckets = 10;
    BasicRateTracker rateTracker = new BasicRateTracker(millis, buckets, false, scheduler);
    assertTrue(Double.isNaN(rateTracker.getCurrentRateSeconds()));

    // 10 buckets, each bucket represents 10 millis, so 100 events / s
    for (int i = 0; i < buckets; i++) {
      rateTracker.eventObserved();
      rateTracker.recalculate();
    }

    double originalRate = rateTracker.getCurrentRateSeconds();
    assertTrue(100.0 == originalRate);

    rateTracker.pause();

    // try to alter the rate
    rateTracker.eventObserved();
    rateTracker.eventObserved();
    rateTracker.eventObserved();
    rateTracker.recalculate();
    assertTrue(rateTracker.getCurrentRateSeconds() == originalRate);

    rateTracker.resume();

    // this should be ignored as well
    rateTracker.eventObserved();
    rateTracker.eventObserved();
    rateTracker.recalculate();
    assertTrue(rateTracker.getCurrentRateSeconds() == originalRate);

    // this should now change the rate
    rateTracker.eventObserved();
    rateTracker.eventObserved();
    rateTracker.recalculate();
    assertFalse(rateTracker.getCurrentRateSeconds() == originalRate);
  }
}
