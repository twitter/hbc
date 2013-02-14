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

package com.twitter.hbc.core.endpoint;

import com.google.common.base.Joiner;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpConstants;

import java.util.List;

public class StatusesFilterEndpoint extends DefaultStreamingEndpoint {

  public static final String PATH = "/statuses/filter.json";

  public StatusesFilterEndpoint() {
    this(false);
  }

  /**
   * @param backfillable set to true if you have elevated access
   */
  public StatusesFilterEndpoint(boolean backfillable) {
    super(PATH, HttpConstants.HTTP_POST, backfillable);
  }

  public StatusesFilterEndpoint followings(List<Long> userIds) {
    addPostParameter(Constants.FOLLOW_PARAM, Joiner.on(',').join(userIds));
    return this;
  }

  /**
   * @param terms a list of Strings to track. These strings should NOT be url-encoded.
   */
  public StatusesFilterEndpoint trackTerms(List<String> terms) {
    addPostParameter(Constants.TRACK_PARAM, Joiner.on(',').join(terms));
    return this;
  }

  public StatusesFilterEndpoint locations(List<Location> locations) {
    addPostParameter(Constants.LOCATION_PARAM, Joiner.on(',').join(locations));
    return this;
  }
}