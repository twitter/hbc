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

package com.twitter.hbc.twitter4j.message;

import com.google.common.base.Preconditions;

public class DisconnectMessage {

  private final int disconnectCode;
  private final String streamName;
  private final String disconnectReason;

  public DisconnectMessage(int disconnectCode, String streamName, String disconnectReason) {
    this.disconnectCode = disconnectCode;
    this.streamName = Preconditions.checkNotNull(streamName);
    this.disconnectReason = Preconditions.checkNotNull(disconnectReason);
  }

  public int getDisconnectCode() {
    return disconnectCode;
  }

  public String getStreamName() {
    return streamName;
  }

  public String getDisconnectReason() {
    return disconnectReason;
  }

  @Override
  public String toString() {
    return String.format("Stream %s disconnected: %d %s", streamName, disconnectCode, disconnectReason);
  }
}
