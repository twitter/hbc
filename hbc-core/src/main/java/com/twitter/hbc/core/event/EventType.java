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

package com.twitter.hbc.core.event;

public enum EventType {
  /**
   * When an http request is made
   */
  CONNECTION_ATTEMPT,
  /**
   * When a connection is established w/ a 200 response
   */
  CONNECTED,
  /**
   * When we begin receiving/processing messages
   */
  PROCESSING,
  /**
   * When an established connection gets disconnected for any reason
   */
  DISCONNECTED,
  /**
   * When a connection fails due to either a bad request (invalid host, invalid requests)
   */
  CONNECTION_ERROR,
  /**
   * When a connection fails due to a 400/500 response
   */
  HTTP_ERROR,
  /**
   * When the client is explicitly stopped by the user. No more connections will be attempted
   */
  STOPPED_BY_USER,
  /**
   * When the client is stopped due to an error. No more connections will be attempted
   */
  STOPPED_BY_ERROR
}
