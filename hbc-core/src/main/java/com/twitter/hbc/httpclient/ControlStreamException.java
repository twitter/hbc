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

package com.twitter.hbc.httpclient;

import org.apache.http.StatusLine;

public class ControlStreamException extends Exception {

  private final int statusCode;

  public ControlStreamException(StatusLine statusLine) {
    super(statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
    this.statusCode = statusLine.getStatusCode();
  }

  public ControlStreamException(String message) {
    super(message);
    this.statusCode = -1;
  }

  /**
   * @return the http status code of the control stream request. -1 if the request was never made
   * or an error occurred before the request was made
   */
  public int getStatusCode() {
    return statusCode;
  }
}