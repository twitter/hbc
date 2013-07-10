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
 * This manages all of the reconnection logic.
 */
public interface ReconnectionManager {
  void handleExponentialBackoff();
  void handleLinearBackoff();
  boolean shouldReconnectOn400s();

  /**
   * Estimates the backfill count param given the tps
   */
  int estimateBackfill(double tps);

  /**
   * Call this when we have an active connection established
   */
  void resetCounts();
}
