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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.StatsReporter;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;
import com.twitter.hbc.twitter4j.parser.JSONObjectParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import static com.twitter.hbc.twitter4j.parser.JSONObjectParser.parseFriendList;

class BaseTwitter4jClient implements Twitter4jClient {

  private final static Logger logger = LoggerFactory.getLogger(BaseTwitter4jClient.class);

  protected final Client client;
  private final BlockingQueue<String> messageQueue;
  private final ExecutorService executorService;
  private final PublicObjectFactory factory;

  protected BaseTwitter4jClient(Client client, BlockingQueue<String> blockingQueue, ExecutorService executorService) {
    this.client = Preconditions.checkNotNull(client);
    this.messageQueue = Preconditions.checkNotNull(blockingQueue);
    this.executorService = Preconditions.checkNotNull(executorService);
    this.factory = new PublicObjectFactory(new ConfigurationBuilder().build());
  }

  @Override
  public void connect() {
    client.connect();
  }

  @Override
  public void reconnect() {
    client.reconnect();
  }

  /**
   * Forks off a runnable with the executor provided. Multiple calls are allowed, but the listeners must be
   * threadsafe.
   */
  @Override
  public void process() {
    if (client.isDone() || executorService.isTerminated()) {
      throw new IllegalStateException("Client is already stopped");
    }
    Runnable runner = new Runnable() {
      @Override
      public void run() {
        try {
          while (!client.isDone()) {
            String msg = messageQueue.take();
            try {
              parseMessage(msg);
            } catch (Exception e) {
              logger.warn("Exception thrown during parsing msg " + msg, e);
              onException(e);
            }
          }
        } catch (Exception e) {
          onException(e);
        }
      }
    };

    executorService.execute(runner);
  }

  /**
   * Stops the client, and shuts down the executor service
   */
  @Override
  public void stop() {
    client.stop();
    executorService.shutdown();
  }

  @Override
  public void stop(int millis) {
    client.stop(millis);
    executorService.shutdown();
  }

  @Override
  public boolean isDone() {
    return client.isDone();
  }

  @Override
  public String getName() {
    return client.getName();
  }

  @Override
  public StreamingEndpoint getEndpoint() {
    return client.getEndpoint();
  }

  @Override
  public StatsReporter.StatsTracker getStatsTracker() {
    return client.getStatsTracker();
  }

  protected void parseMessage(String msg) throws JSONException, TwitterException, IOException {
    JSONObject json = new JSONObject(msg);
    long sitestreamUser = getSitestreamUser(json);
    processMessage(sitestreamUser, preprocessMessage(json));
  }

  /**
   * @return the user id of the message if its for a sitestreams connection. -1 otherwise
   */
  protected long getSitestreamUser(JSONObject json) throws JSONException {
    return -1;
  }

  /**
   * Removes the sitestreams envelope, if necessary
   */
  protected JSONObject preprocessMessage(JSONObject json) throws JSONException {
    return json;
  }

  @VisibleForTesting
  void processMessage(long sitestreamUser, JSONObject json) throws JSONException, TwitterException, IOException {
    JSONObjectType.Type type = JSONObjectType.determine(json);
    switch (type) {
      case STATUS:
        processStatus(sitestreamUser, json);
        break;
      case LIMIT:
        processLimit(sitestreamUser, json);
        break;
      case DELETE:
        processDelete(sitestreamUser, json);
        break;
      case SCRUB_GEO:
        processScrubGeo(sitestreamUser, json);
        break;
      case DIRECT_MESSAGE:
      case SENDER:
        processDirectMessage(sitestreamUser, json);
        break;
      case FRIENDS:
        processFriends(sitestreamUser, json);
        break;
      case FAVORITE:
        processFavorite(sitestreamUser, json);
        break;
      case UNFAVORITE:
        processUnfavorite(sitestreamUser, json);
        break;
      case FOLLOW:
        processFollow(sitestreamUser, json);
        break;
      case UNFOLLOW:
        processUnfollow(sitestreamUser, json);
        break;
      case USER_LIST_MEMBER_ADDED:
        processUserListMemberAddition(sitestreamUser, json);
        break;
      case USER_LIST_MEMBER_DELETED:
        processUserListMemberDeletion(sitestreamUser, json);
        break;
      case USER_LIST_SUBSCRIBED:
        processUserListSubscription(sitestreamUser, json);
        break;
      case USER_LIST_UNSUBSCRIBED:
        processUserListUnsubscription(sitestreamUser, json);
        break;
      case USER_LIST_CREATED:
        processUserListCreation(sitestreamUser, json);
        break;
      case USER_LIST_UPDATED:
        processUserListUpdated(sitestreamUser, json);
        break;
      case USER_LIST_DESTROYED:
        processUserListDestroyed(sitestreamUser, json);
        break;
      case BLOCK:
        processBlock(sitestreamUser, json);
        break;
      case UNBLOCK:
        processUnblock(sitestreamUser, json);
        break;
      case USER_UPDATE:
        processUserUpdate(sitestreamUser, json);
        break;
      case DISCONNECTION:
        processDisconnectMessage(json);
        break;
      case STALL_WARNING:
        processStallWarning(json);
        break;
      case UNKNOWN:
      default:
        if (JSONObjectParser.isRetweetMessage(json)) {
          processRetweet(sitestreamUser, json);
        } else if (JSONObjectParser.isControlStreamMessage(json)) {
          processControlStream(json);
        } else {
          onUnknownMessageType(json.toString());
        }
    }
  }

  private void processStatus(long sitestreamUser, JSONObject json) throws TwitterException {
    Status status = factory.createStatus(json);
    onStatus(sitestreamUser, status);
  }

  private void processDirectMessage(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    DirectMessage dm = factory.newDirectMessage(json.getJSONObject("direct_message"));
    onDirectMessage(sitestreamUser, dm);
  }

  private void processDelete(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    JSONObject deletionNotice = json.getJSONObject("delete");
    if (deletionNotice.has("status")) {
      onDelete(sitestreamUser, JSONObjectParser.parseStatusDelete(json));
    } else if (deletionNotice.has("direct_message")) {
      JSONObject dm = deletionNotice.getJSONObject("direct_message");
      final long statusId = dm.getLong("id");
      final long userId = dm.getLong("user_id");
      onDeleteDirectMessage(sitestreamUser, statusId, userId);
    }
  }

  private void processStallWarning(JSONObject json) throws JSONException {
    JSONObject warning = json.getJSONObject("warning");
    String code = ((String) warning.opt("code"));
    String message = ((String) warning.opt("message"));
    int percentFull = warning.getInt("percent_full");

    onStallWarning(new StallWarningMessage(code, message, percentFull));
  }

  private void processLimit(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    onTrackLimitationNotice(sitestreamUser, JSONObjectParser.parseTrackLimit(json));
  }

  private void processScrubGeo(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    JSONObject scrubGeo = json.getJSONObject("scrub_geo");
    long userId = scrubGeo.getLong("user_id");
    long upToStatusId = scrubGeo.getLong("up_to_status_id");
    onScrubGeo(sitestreamUser, userId, upToStatusId);
  }

  private void processFriends(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    onFriends(sitestreamUser, parseFriendList(json));
  }

  private void processFavorite(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User target = factory.createUser(JSONObjectParser.parseEventTarget(json));
    Status status = factory.createStatus(JSONObjectParser.parseEventTargetObject(json));
    onFavorite(sitestreamUser, source, target, status);
  }

  private void processUnfavorite(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User target = factory.createUser(JSONObjectParser.parseEventTarget(json));
    Status status = factory.createStatus(JSONObjectParser.parseEventTargetObject(json));
    onUnfavorite(sitestreamUser, source, target, status);
  }

  private void processRetweet(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User target = factory.createUser(JSONObjectParser.parseEventTarget(json));
    Status status = factory.createStatus(JSONObjectParser.parseEventTargetObject(json));
    onRetweet(sitestreamUser, source, target, status);
  }

  private void processFollow(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User target = factory.createUser(JSONObjectParser.parseEventTarget(json));
    onFollow(sitestreamUser, source, target);
  }

  private void processUnfollow(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User target = factory.createUser(JSONObjectParser.parseEventTarget(json));
    onUnfollow(sitestreamUser, source, target);
  }

  private void processUserListMemberAddition(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User addedUser = factory.createUser(JSONObjectParser.parseEventSource(json));
    User owner = factory.createUser(JSONObjectParser.parseEventTarget(json));
    UserList userList = factory.createAUserList(JSONObjectParser.parseEventTargetObject(json));
    onUserListMemberAddition(sitestreamUser, addedUser, owner, userList);
  }

  private void processUserListMemberDeletion(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User deletedMember = factory.createUser(JSONObjectParser.parseEventSource(json));
    User owner = factory.createUser(JSONObjectParser.parseEventTarget(json));
    UserList userList = factory.createAUserList(JSONObjectParser.parseEventTargetObject(json));
    onUserListMemberDeletion(sitestreamUser, deletedMember, owner, userList);
  }

  private void processUserListSubscription(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User owner = factory.createUser(JSONObjectParser.parseEventTarget(json));
    UserList userList = factory.createAUserList(JSONObjectParser.parseEventTargetObject(json));
    onUserListSubscription(sitestreamUser, source, owner, userList);
  }

  private void processUserListUnsubscription(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User owner = factory.createUser(JSONObjectParser.parseEventTarget(json));
    UserList userList = factory.createAUserList(JSONObjectParser.parseEventTargetObject(json));
    onUserListUnsubscription(sitestreamUser, source, owner, userList);
  }

  private void processUserListCreation(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    UserList userList = factory.createAUserList(JSONObjectParser.parseEventTargetObject(json));
    onUserListCreation(sitestreamUser, source, userList);
  }

  private void processUserListUpdated(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    UserList userList = factory.createAUserList(JSONObjectParser.parseEventTargetObject(json));
    onUserListUpdate(sitestreamUser, source, userList);
  }

  private void processUserListDestroyed(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    UserList userList = factory.createAUserList(JSONObjectParser.parseEventTargetObject(json));
    onUserListDeletion(sitestreamUser, source, userList);
  }

  private void processUserUpdate(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    onUserProfileUpdate(sitestreamUser, factory.createUser(JSONObjectParser.parseEventSource(json)));
  }

  private void processBlock(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User target = factory.createUser(JSONObjectParser.parseEventTarget(json));
    onBlock(sitestreamUser, source, target);
  }

  private void processUnblock(long sitestreamUser, JSONObject json) throws TwitterException, JSONException {
    User source = factory.createUser(JSONObjectParser.parseEventSource(json));
    User target = factory.createUser(JSONObjectParser.parseEventTarget(json));
    onUnblock(sitestreamUser, source, target);
  }

  private void processControlStream(JSONObject json) throws JSONException {
    onControlStreamMessage(JSONObjectParser.getStreamId(json));
  }

  private void processDisconnectMessage(JSONObject json) throws JSONException {
    onDisconnectMessage(JSONObjectParser.parseDisconnectMessage(json));
  }

  protected void onStatus(long sitestreamUser, final Status status) {
    logger.info("Unhandled event: onStatus");
  }

  protected void onDelete(long sitestreamUser, StatusDeletionNotice delete) {
    logger.info("Unhandled event: onDelete");
  }

  protected void onTrackLimitationNotice(long sitestreamUser, final int limit) {
    logger.info("Unhandled event: onTrackLimitationNotice");
  }

  protected void onScrubGeo(long sitestreamUser, long userId, long upToStatusId) {
    logger.info("Unhandled event: onScrubGeo");
  }

  protected void onDeleteDirectMessage(long sitestreamUser, long directMessageId, long userId) {
    logger.info("Unhandled event: onDeleteDirectMessage");
  }

  protected void onDirectMessage(long sitestreamUser, final DirectMessage directMessage) {
    logger.info("Unhandled event: onDirectMessage");
  }

  protected void onFriends(long sitestreamUser, final long[] json) {
    logger.info("Unhandled event: onFriends");
  }

  protected void onFavorite(long sitestreamUser, final User source, final User target, final Status targetObject) {
    logger.info("Unhandled event: onFavorite");
  }

  protected void onUnfavorite(long sitestreamUser, final User source, final User target, final Status targetObject) {
    logger.info("Unhandled event: onUnfavorite");
  }

  protected void onRetweet(long sitestreamUser, User source, User target, Status tweet) {
    logger.info("Unhandled event: onRetweet");
  }

  protected void onFollow(long sitestreamUser, final User source, final User target) throws TwitterException {
    logger.info("Unhandled event: onFollow");
  }

  protected void onUnfollow(long sitestreamUser, final User source, final User target) throws TwitterException {
    logger.info("Unhandled event: onUnfollow");
  }

  protected void onUserListMemberAddition(long sitestreamUser, final User addedMember, final User owner, final UserList userList) {
    logger.info("Unhandled event: onUserListMemberAddition");
  }

  protected void onUserListMemberDeletion(long sitestreamUser, final User deletedMember, final User owner, final UserList userList) {
    logger.info("Unhandled event: onUserListMemberDeletion");
  }

  protected void onUserListSubscription(long sitestreamUser, final User subscriber, final User owner, final UserList userList) {
    logger.info("Unhandled event: onUserListSubscription");
  }

  protected void onUserListUnsubscription(long sitestreamUser, final User deletedMember, final User owner, final UserList userList) {
    logger.info("Unhandled event: onUserListUnsubscription");
  }

  protected void onUserListCreation(long sitestreamUser, final User source, final UserList userList) {
    logger.info("Unhandled event: onUserListCreation");
  }

  protected void onUserListUpdate(long sitestreamUser, User source, UserList userList) {
    logger.info("Unhandled event: onUserListUpdate");
  }

  protected void onUserListDeletion(long sitestreamUser, final User source, final UserList userList) {
    logger.info("Unhandled event: onUserListDeletion");
  }

  protected void onUserProfileUpdate(long sitestreamUser, User source) {
    logger.info("Unhandled event: onUserProfileUpdate");
  }

  protected void onBlock(long sitestreamUser, User source, User target) {
    logger.info("Unhandled event: onBlock");
  }

  protected void onUnblock(long sitestreamUser, User source, User target) {
    logger.info("Unhandled event: onUnblock");
  }

  protected void onControlStreamMessage(String streamId) {
    logger.info("Unhandled event: onControlStreamMessage");
  }

  protected void onDisconnectMessage(DisconnectMessage disconnectMessage) {
    logger.info("Unhandled event: onDisconnectMessage - {}", disconnectMessage.toString());
  }

  protected void onException(Exception e) {
    logger.info("Exception caught", e);
  }

  protected void onStallWarning(StallWarningMessage stallWarning) {
    logger.info("Unhandled event: onStallWarning - {}", stallWarning);
  }

  protected void onUnknownMessageType(String msg) {
    logger.info("Unknown message (first 50 chars): " + msg.substring(0, Math.min(msg.length(), 50)));
  }
}
