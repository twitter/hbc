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

package com.twitter.hbc.twitter4j;

import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.parser.JSONObjectParser;
import org.junit.Before;
import org.junit.Test;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;

public class JSONObjectParserTest extends ResourceReader {

  private final ResourceReader reader = new ResourceReader();

  private String statusDeletionNotice;
  private String limit;
  private String friendsList;
  private String sitestreamFriendsList;
  private String controlMessage;
  private String disconnectMessage;

  @Before
  public void setup() throws IOException {
    statusDeletionNotice = reader.readFile("status-deletion.json");
    limit = reader.readFile("limit.json");
    friendsList = reader.readFile("friends-list.json");
    sitestreamFriendsList = reader.readFile("sitestream-friends-list.json");
    controlMessage = reader.readFile("control-message.json");
    disconnectMessage = reader.readFile("disconnect-message.json");
  }

  @Test
  public void testParseStatusDelete() throws JSONException {
    JSONObject json = new JSONObject(statusDeletionNotice);
    StatusDeletionNotice delete = JSONObjectParser.parseStatusDelete(json);
    assertEquals(delete.getStatusId(), 1234);
    assertEquals(delete.getUserId(), 3);
  }

  @Test
  public void testTrackLimitNotice() throws JSONException {
    JSONObject json = new JSONObject(limit);
    assertEquals(JSONObjectParser.parseTrackLimit(json), 1234);
  }

  @Test
  public void testParseTestList() throws JSONException, TwitterException {
    long[] list = JSONObjectParser.parseFriendList(new JSONObject(friendsList));
    assertEquals(list.length, 4);
    assertEquals(list[0], 1497);
    assertEquals(list[1], 169686021);
    assertEquals(list[2], 790205);
    assertEquals(list[3], 15211564);
  }

  @Test
  public void testParseSitestreamUserId() throws JSONException {
    JSONObject json = new JSONObject(sitestreamFriendsList);
    assertTrue(JSONObjectParser.hasSitestreamUser(json));
    assertEquals(JSONObjectParser.getSitestreamUser(json), 1888);
  }

  @Test
  public void testParseSitestreamMessage() throws JSONException {
    JSONObject json = new JSONObject(sitestreamFriendsList);
    assertTrue(JSONObjectParser.hasSitestreamUser(json));
    long[] list = JSONObjectParser.parseFriendList(JSONObjectParser.getSitestreamMessage(json));
    assertEquals(list[0], 1);
    assertEquals(list[1], 2);
    assertEquals(list[2], 3);
    assertEquals(list[3], 4);
    assertEquals(list.length, 4);
  }

  @Test
  public void testMissingFields() throws JSONException {
    try {
      JSONObject json = new JSONObject("{\"limit\":{}}");
      JSONObjectParser.parseTrackLimit(json);
      fail();
    } catch (JSONException e) {
      // expectation
    }
  }

  @Test
  public void testParseControlStreamMessage() throws JSONException {
    JSONObject json = new JSONObject(controlMessage);
    assertFalse(JSONObjectParser.hasSitestreamUser(json));
    assertFalse(JSONObjectParser.hasSitestreamMessage(json));
    String streamId = JSONObjectParser.getStreamId(json);
    assertEquals(streamId, "01_225167_334389048B872A533002B34D73F8C29FD09EFC50");
  }

  @Test
  public void testParseDisconnectMessage() throws JSONException {
    JSONObject json = new JSONObject(disconnectMessage);
    assertFalse(JSONObjectParser.hasSitestreamUser(json));
    assertFalse(JSONObjectParser.hasSitestreamMessage(json));
    DisconnectMessage message = JSONObjectParser.parseDisconnectMessage(json);
    assertEquals(message.getDisconnectCode(), 5);
    assertEquals(message.getStreamName(), "somestreamname123");
    assertEquals(message.getDisconnectReason(), "reason for disconnection");
  }
}
