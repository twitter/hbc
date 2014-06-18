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

import com.google.common.base.Charsets;

import java.nio.charset.Charset;

public class Constants {

  public static final String CURRENT_API_VERSION = "1.1";

  /**
   * Max backoff, in milliseconds.
   */
  public static final int MAX_BACKOFF_COUNT = 150000;

  /**
   * Min backoff, in milliseconds.
   */
  public static final int MIN_BACKOFF_MILLIS = 250;

  public static final int NUM_FIREHOSE_PARTITIONS = 16;

  public static final String DELIMITED_PARAM = "delimited";
  public static final String DELIMITED_VALUE = "length";

  public static final String COUNT_PARAM = "count";

  public static final String STALL_WARNING_PARAM = "stall_warnings";
  public static final String STALL_WARNING_VALUE = "true";

  public static final String PARTITION_PARAM = "partitions";

  public static final String TRACK_PARAM = "track";

  public static final String LOCATION_PARAM = "locations";

  public static final String LANGUAGE_PARAM = "language";

  public static final String FILTER_LEVEL_PARAM = "filter_level";

  public static final String FOLLOW_PARAM = "follow";

  public static final String WITH_PARAM = "with";
  public static final String WITH_FOLLOWINGS = "followings";
  public static final String WITH_USER = "user";

  public static final String REPLIES_PARAM = "replies";
  public static final String REPLIES_ALL = "all";

  public static final String USER_ID_PARAM = "user_id";
  public static final String CURSOR_PARAM = "cursor";

  public static final String STREAM_HOST = "https://stream.twitter.com";
  public static final String SITESTREAM_HOST = "https://sitestream.twitter.com";
  public static final String USERSTREAM_HOST = "https://userstream.twitter.com";

  public static final String ENTERPRISE_STREAM_HOST = "https://stream.gnip.com";
  public static final String FROM_DATE_PARAM = "fromDate";
  public static final String TO_DATE_PARAM = "toDate";

  public static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

  /**
   * Disconnect codes for disconnection messages
   */
  public class DisconnectCode {
    public static final int SERVER_SHUTDOWN = 1;
    public static final int DUPLICATE_STREAM = 2;
    public static final int CONTROL_STREAM_REQUEST = 3;
    public static final int STALL = 4;
    public static final int NORMAL = 5;
    public static final int TOKEN_REVOKED = 6;
  }

  public static enum FilterLevel {
    /** No filtering */
    None("none"),

    Low("low"),

    /** Highest level of filtering currently available */
    Medium("medium");

    private final String parameterValue;

    private FilterLevel(String value) {
      this.parameterValue = value;
    }

    public String asParameter() {
      return this.parameterValue;
    }
  }
}