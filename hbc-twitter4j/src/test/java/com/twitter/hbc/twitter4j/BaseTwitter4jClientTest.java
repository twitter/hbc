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

import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import org.junit.Before;
import org.junit.Test;
import twitter4j.*;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BaseTwitter4jClientTest {

  private final ResourceReader reader = new ResourceReader();

  private BasicClient mockClient;
  private ExecutorService executor;
  private BaseTwitter4jClient t4jClient;
  private BlockingQueue<String> queue;
  private String statusDeletionNotice;
  private String limit;
  private String friendsList;
  private String status;
  private String scrubGeo;
  private String user;
  private String directMessage;
  private String disconnectMessage;
  private String controlMessage;
  private String directMessageDelete;

  @Before
  public void setup() throws IOException {
    mockClient = mock(BasicClient.class);
    queue = new LinkedBlockingQueue<String>();
    executor = mock(ExecutorService.class);
    t4jClient = spy(new BaseTwitter4jClient(mockClient, queue, executor));
    status = reader.readFile("status.json");
    user = reader.readFile("user.json");
    statusDeletionNotice = reader.readFile("status-deletion.json");
    limit = reader.readFile("limit.json");
    scrubGeo = reader.readFile("scrub-geo.json");
    friendsList = reader.readFile("friends-list.json");
    disconnectMessage = reader.readFile("disconnect-message.json");
    controlMessage = reader.readFile("control-message.json");
    directMessage = reader.readFile("direct-message.json");
    directMessageDelete = reader.readFile("direct-message-delete.json");
  }

  @Test
  public void testClientStartsStopsProperly() {
    t4jClient.connect();
    verify(mockClient).connect();

    t4jClient.stop();
    verify(mockClient).stop();
    verify(executor).shutdown();
  }

  @Test
  public void testClientMultipleProcessCalls() {
    t4jClient.connect();
    verify(mockClient).connect();

    t4jClient.process();
    t4jClient.process();
    t4jClient.process();
    t4jClient.process();
    verify(executor, times(4)).execute(any(Runnable.class));

    t4jClient.stop();
    verify(mockClient).stop();
    verify(executor).shutdown();
  }

  @Test
  public void testStatusListener() throws JSONException, TwitterException, IOException {
    t4jClient.processMessage(-1, new JSONObject(status));
    verify(t4jClient).onStatus(anyInt(), any(Status.class));
  }

  @Test
  public void testStatusDeletionListener() throws JSONException, TwitterException, IOException {
    t4jClient.processMessage(-1, new JSONObject(statusDeletionNotice));
    verify(t4jClient).onDelete(anyInt(), any(StatusDeletionNotice.class));
  }

  @Test
  public void testLimitListener() throws JSONException, TwitterException, IOException {
    t4jClient.processMessage(-1, new JSONObject(limit));
    verify(t4jClient).onTrackLimitationNotice(anyInt(), anyInt());
  }

  @Test
  public void testScrubGeoListener() throws JSONException, TwitterException, IOException {
    t4jClient.processMessage(-1, new JSONObject(scrubGeo));
    verify(t4jClient).onScrubGeo(anyInt(), anyInt(), anyInt());
  }

  @Test
  public void testDirectMessageListener() throws TwitterException, IOException, JSONException {
    t4jClient.processMessage(-1, new JSONObject(directMessage));
    verify(t4jClient).onDirectMessage(anyInt(), any(DirectMessage.class));
  }

  @Test
  public void testDirectMessageDeleteListener() throws TwitterException, IOException, JSONException {
    t4jClient.processMessage(-1, new JSONObject(directMessageDelete));
    verify(t4jClient).onDeleteDirectMessage(anyInt(), anyLong(), anyLong());
  }

  @Test
  public void testFriendsListListener() throws TwitterException, IOException, JSONException {
    t4jClient.processMessage(-1, new JSONObject(friendsList));
    verify(t4jClient).onFriends(anyInt(), any(long[].class));
  }

  @Test
  public void testFavoriteListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("favorite", user, user, status);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onFavorite(anyInt(), any(User.class), any(User.class), any(Status.class));
  }

  @Test
  public void testUnfavoriteListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("unfavorite", user, user, status);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUnfavorite(anyInt(), any(User.class), any(User.class), any(Status.class));
  }

  @Test
  public void testRetweetListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("retweet", user, user, status);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onRetweet(anyInt(), any(User.class), any(User.class), any(Status.class));
  }

  @Test
  public void testFollowListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("follow", user, user, null);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onFollow(anyInt(), any(User.class), any(User.class));
  }

  @Test
  public void testUnfollowListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("unfollow", user, user, null);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUnfollow(anyInt(), any(User.class), any(User.class));
  }

  @Test
  public void testListMemberAddedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("list_member_added", user, user, friendsList);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserListMemberAddition(anyInt(), any(User.class), any(User.class), any(UserList.class));
  }

  @Test
  public void testListMemberDeletedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("list_member_removed", user, user, friendsList);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserListMemberDeletion(anyInt(), any(User.class), any(User.class), any(UserList.class));
  }

  @Test
  public void testListSubscribedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("list_user_subscribed", user, user, friendsList);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserListSubscription(anyInt(), any(User.class), any(User.class), any(UserList.class));
  }

  @Test
  public void testListUnsubscribedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("list_user_unsubscribed", user, user, friendsList);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserListUnsubscription(anyInt(), any(User.class), any(User.class), any(UserList.class));
  }

  @Test
  public void testListCreatedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("list_created", user, user, friendsList);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserListCreation(anyInt(), any(User.class), any(UserList.class));
  }

  @Test
  public void testListDestroyedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("list_destroyed", user, user, friendsList);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserListDeletion(anyInt(), any(User.class), any(UserList.class));
  }

  @Test
  public void testListUpdatedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("list_updated", user, user, friendsList);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserListUpdate(anyInt(), any(User.class), any(UserList.class));
  }

  @Test
  public void testBlockedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("block", user, user, null);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onBlock(anyInt(), any(User.class), any(User.class));
  }

  @Test
  public void testUnblockedListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("unblock", user, user, null);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUnblock(anyInt(), any(User.class), any(User.class));
  }

  @Test
  public void testUserUpdateListener() throws TwitterException, IOException, JSONException {
    JSONObject json = CreateEvent.createEvent("user_update", user, user, null);
    t4jClient.processMessage(-1, json);
    verify(t4jClient).onUserProfileUpdate(anyInt(), any(User.class));
  }

  @Test
  public void testControlStreamMessage() throws TwitterException, IOException, JSONException {
    t4jClient.processMessage(-1, new JSONObject(controlMessage));
    verify(t4jClient).onControlStreamMessage(anyString());
  }

  @Test
  public void testDisconnectListener() throws TwitterException, IOException, JSONException {
    t4jClient.processMessage(-1, new JSONObject(disconnectMessage));
    verify(t4jClient).onDisconnectMessage(any(DisconnectMessage.class));
  }
}