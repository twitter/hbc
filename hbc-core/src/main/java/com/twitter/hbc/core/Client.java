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

package com.twitter.hbc.core;

import com.twitter.hbc.core.endpoint.StreamingEndpoint;

public interface Client {
  /**
   * Connects to the endpoint and begins streaming.
   *
   * Should handle unexpected disconnects by reconnecting with appropriate backoffs/backfill param
   * Should only be called once.
   */
  public void connect();

  public void reconnect();

  /**
   * Permanently stops the current connection and does any necessary cleanup.
   * Waits until the connection {@link #isDone()}.
   *
   * Note: after being called, neither {@link #connect()} nor {@link #reconnect()} is possible.
   */
  public void stop();

  /**
   * Permanently stops the current connection and does any necessary cleanup.
   * Waits up to waitMillis milliseconds for the connection to be {@link #isDone()}.
   *
   * Note: after being called, neither {@link #connect()} nor {@link #reconnect()} is permitted.
   *
   * @param waitMillis milliseconds to wait for the client to stop
   */
  public void stop(int waitMillis);

  public boolean isDone();

  /**
   * Name of the client used for logging and other diagnostic purposes.
   */
  public String getName();

  public StreamingEndpoint getEndpoint();

  public StatsReporter.StatsTracker getStatsTracker();
}
