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

package com.twitter.hbc.twitter4j.parser;

import com.google.common.primitives.Longs;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import twitter4j.StatusDeletionNotice;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.JSONObjectType;

public class JSONObjectParser {

  public static StatusDeletionNotice parseStatusDelete(JSONObject json) throws JSONException {
    JSONObject statusDelete = json.getJSONObject("delete").getJSONObject("status");
    final long statusId = statusDelete.getLong("id");
    final long userId = statusDelete.getLong("user_id");
    return new StatusDeletionNotice() {
      @Override
      public long getStatusId() {
        return statusId;
      }

      @Override
      public long getUserId() {
        return userId;
      }

      @Override
      public int compareTo(StatusDeletionNotice o) {
        return Longs.compare(getStatusId(), o.getStatusId());
      }
    };
  }

  public static int parseTrackLimit(JSONObject json) throws JSONException {
    return json.getJSONObject("limit").getInt("track");
  }

  public static JSONObject parseEventSource(JSONObject json) throws JSONException {
    return json.getJSONObject("source");
  }

  public static JSONObject parseEventTarget(JSONObject json) throws JSONException {
    return json.getJSONObject("target");
  }

  public static JSONObject parseEventTargetObject(JSONObject json) throws JSONException {
    return json.getJSONObject("target_object");
  }

  public static long[] parseFriendList(JSONObject json) throws JSONException {
    JSONArray friends = json.getJSONArray("friends");
    long[] friendIds = new long[friends.length()];
    for (int i = 0; i < friendIds.length; ++i) {
      friendIds[i] = friends.getLong(i);
    }
    return friendIds;
  }

  public static boolean hasSitestreamUser(JSONObject envelope) {
    return envelope.has("for_user");
  }

  public static long getSitestreamUser(JSONObject envelope) throws JSONException {
    return envelope.getLong("for_user");
  }

  public static boolean hasSitestreamMessage(JSONObject envelope) {
    return envelope.has("message");
  }

  public static JSONObject getSitestreamMessage(JSONObject envelope) throws JSONException {
    return envelope.getJSONObject("message");
  }

  public static boolean isControlStreamMessage(JSONObject message) throws JSONException {
    return message.has("control");
  }

  public static String getStreamId(JSONObject message) throws JSONException {
    String uri = message.getJSONObject("control").getString("control_uri");
    String[] split = uri.split("/");
    // do some basic validation to make sure the url is valid
    // url should look like this: /1.1/site/c/1_1_54e345d655ee3e8df359ac033648530bfbe26c5f
    if (split.length != 5) {
      throw new IllegalStateException("Unknown url format: " + uri);
    }
    return split[split.length - 1];
  }

  @Deprecated
  public static boolean isDisconnectMessage(JSONObject message) {
    return JSONObjectType.determine(message) == JSONObjectType.Type.DISCONNECTION;
  }

  public static boolean isRetweetMessage(JSONObject message) throws JSONException {
    Object event = message.opt("event");
    if (!(event instanceof String))
      return false;
    return "retweet".equals(event);
  }

  public static DisconnectMessage parseDisconnectMessage(JSONObject message) throws JSONException {
    JSONObject json = message.getJSONObject("disconnect");
    int code = json.getInt("code");
    String streamName = json.getString("stream_name");
    String reason = json.getString("reason");
    return new DisconnectMessage(code, streamName, reason);
  }
}
