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

/**
 * Struct representing a stall warning message.
 * See https://dev.twitter.com/docs/streaming-apis/parameters#stall_warnings
 */
public class StallWarningMessage {

  private final String code;
  private final String message;
  private final int percentFull;

  public StallWarningMessage(String code, String message, int percentFull) {
    this.code = code;
    this.message = message;
    this.percentFull = percentFull;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public int getPercentFull() {
    return percentFull;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StallWarningMessage that = (StallWarningMessage) o;

    if (percentFull != that.percentFull) return false;
    if (code != null ? !code.equals(that.code) : that.code != null) return false;
    if (message != null ? !message.equals(that.message) : that.message != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = code != null ? code.hashCode() : 0;
    result = 31 * result + (message != null ? message.hashCode() : 0);
    result = 31 * result + percentFull;
    return result;
  }

  @Override
  public String toString() {
    return "StallWarningMessage{" +
      "code='" + code + '\'' +
      ", message='" + message + '\'' +
      ", percentFull=" + percentFull +
      '}';
  }
}
