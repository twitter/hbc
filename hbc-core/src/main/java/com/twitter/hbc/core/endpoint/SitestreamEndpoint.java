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
import com.google.common.base.Preconditions;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpConstants;

import java.util.List;

public class SitestreamEndpoint extends UserstreamEndpoint {

  public static final String PATH = "/site.json";

  public SitestreamEndpoint(List<Long> userIds) {
    super(PATH);

    Preconditions.checkNotNull(userIds, "List of users to follow must be provided");
    Preconditions.checkArgument(userIds.size() > 0, "List of users to follow must not be empty");
    Preconditions.checkArgument(userIds.size() <= 100, "Number of users to follow must be less than or equal to 100");

    addQueryParameter(Constants.FOLLOW_PARAM, Joiner.on(',').join(userIds));
  }

  /**
   * Control stream endpoints
   */

  public static Endpoint streamInfoEndpoint(String streamId) {
    return new BaseEndpoint("/site/c/" + streamId + "/info.json", HttpConstants.HTTP_GET);
  }

  public static Endpoint addUserEndpoint(String streamId) {
    return new BaseEndpoint("/site/c/" + streamId + "/add_user.json", HttpConstants.HTTP_POST);
  }

  public static Endpoint removeUserEndpoint(String streamId) {
    return new BaseEndpoint("/site/c/" + streamId + "/remove_user.json", HttpConstants.HTTP_POST);
  }

  public static Endpoint friendsEndpoint(String streamId) {
    return new BaseEndpoint("/site/c/" + streamId + "/friends/ids.json", HttpConstants.HTTP_POST);
  }
}
