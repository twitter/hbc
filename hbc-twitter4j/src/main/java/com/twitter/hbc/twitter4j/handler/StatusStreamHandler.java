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

package com.twitter.hbc.twitter4j.handler;

import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;
import twitter4j.StatusListener;

public interface StatusStreamHandler extends StatusListener {
  /**
   * See documentation on disconnect messages here: https://dev.twitter.com/docs/streaming-apis/messages#Disconnect_messages_disconnect
   */
  public void onDisconnectMessage(DisconnectMessage message);

  /**
   * See documentation on stall warnings here:
   * See https://dev.twitter.com/docs/streaming-apis/parameters#stall_warnings
   *
   * Ideally, twitter4j would make it's StallWarning's constructor public and we could remove this.
   */
  public void onStallWarningMessage(StallWarningMessage warning);

  /**
   * Any message we receive that isn't handled by the other methods
   */
  public void onUnknownMessageType(String msg);
}
