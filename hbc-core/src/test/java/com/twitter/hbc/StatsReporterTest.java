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

import com.twitter.hbc.core.StatsReporter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class StatsReporterTest {

  @Test
  public void testStatsReporter() {
    StatsReporter statsReporter = new StatsReporter();
    StatsReporter.StatsTracker stats = statsReporter.getStatsTracker();

    assertEquals(stats.getNum200s(), 0);
    statsReporter.incrNum200s();
    statsReporter.incrNum200s();
    statsReporter.incrNum200s();
    assertEquals(stats.getNum200s(), 3);

    assertEquals(stats.getNumMessages(), 0);
    statsReporter.incrNumMessages();
    statsReporter.incrNumMessages();
    assertEquals(stats.getNumMessages(), 2);
  }
}
