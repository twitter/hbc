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

package com.twitter.hbc.twitter4j.v3.handler;

import com.twitter.hbc.twitter4j.v3.message.DisconnectMessage;
import twitter4j.StatusListener;

public interface StatusStreamHandler extends StatusListener {
  /**
   * See documentation on disconnect messages here: https://dev.twitter.com/docs/streaming-apis/messages#Disconnect_messages_disconnect
   */
  public void onDisconnectMessage(DisconnectMessage message);

  /**
   * Any message we receive that isn't handled by the other methods
   */
  public void onUnknownMessageType(String msg);
}
