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

import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpConstants;

public class UserstreamEndpoint extends DefaultStreamingEndpoint {

  public static final String PATH = "/user.json";

  public UserstreamEndpoint() {
    this(PATH);
  }

  protected UserstreamEndpoint(String path) {
    super(path, HttpConstants.HTTP_GET, false);
  }

  /**
   * Corresponds to setting `with=followings` when true.
   * See https://dev.twitter.com/docs/streaming-apis/parameters#with
   */
  public void withFollowings(boolean followings) {
    if (followings) {
      addQueryParameter(Constants.WITH_PARAM, Constants.WITH_FOLLOWINGS);
    } else {
      removeQueryParameter(Constants.WITH_PARAM);
    }
  }

  /**
   * Corresponds to setting `with=user` when true.
   * See https://dev.twitter.com/docs/streaming-apis/parameters#with
   */
  public void withUser(boolean user) {
    if (user) {
      addQueryParameter(Constants.WITH_PARAM, Constants.WITH_USER);
    } else {
      removeQueryParameter(Constants.WITH_PARAM);
    }
  }

  public void allReplies(boolean all) {
    if (all) {
      addQueryParameter(Constants.REPLIES_PARAM, Constants.REPLIES_ALL);
    } else {
      removeQueryParameter(Constants.REPLIES_PARAM);
    }
  }
}
