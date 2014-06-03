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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.twitter4j.handler.SitestreamHandler;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;
import com.twitter.hbc.twitter4j.parser.JSONObjectParser;
import twitter4j.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class Twitter4jSitestreamClient extends BaseTwitter4jClient {

  private final List<SiteStreamsListener> sitestreamListeners;
  private final AtomicReference<String> streamId;

  public Twitter4jSitestreamClient(Client client, BlockingQueue<String> blockingQueue, List<SiteStreamsListener> listeners, ExecutorService executorService) {
    super(client, blockingQueue, executorService);
    Preconditions.checkNotNull(listeners);
    this.sitestreamListeners = ImmutableList.copyOf(listeners);
    this.streamId = new AtomicReference<String>();
  }

  @Override
  protected long getSitestreamUser(JSONObject json) throws JSONException {
    try {
      if (JSONObjectParser.hasSitestreamUser(json)) {
        return JSONObjectParser.getSitestreamUser(json);
      } else {
        return super.getSitestreamUser(json);
      }
    } catch (JSONException e) {
      onException(e);
      throw e;
    }
  }

  @Override
  protected JSONObject preprocessMessage(JSONObject json) throws JSONException {
    if (JSONObjectParser.hasSitestreamMessage(json)) {
      try {
        return JSONObjectParser.getSitestreamMessage(json);
      } catch (JSONException e) {
        onException(e);
        throw e;
      }
    } else {
      return json;
    }
  }


  @Override
  protected void onStatus(long sitestreamUser, final Status status) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onStatus(sitestreamUser, status);
    }
  }

  @Override
  protected void onDelete(long sitestreamUser, StatusDeletionNotice delete) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onDeletionNotice(sitestreamUser, delete);
    }
  }

  @Override
  protected void onDeleteDirectMessage(long sitestreamUser, long directMessageId, long userId) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onDeletionNotice(sitestreamUser, directMessageId, userId);
    }
  }

  @Override
  protected void onDirectMessage(long sitestreamUser, final DirectMessage directMessage) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onDirectMessage(sitestreamUser, directMessage);
    }
  }

  @Override
  protected void onFriends(long sitestreamUser, long[] friendIds) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onFriendList(sitestreamUser, friendIds);
    }
  }

  @Override
  protected void onFavorite(long sitestreamUser, final User source, final User target, final Status faved) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onFavorite(sitestreamUser, source, target, faved);
    }
  }

  @Override
  protected void onUnfavorite(long sitestreamUser, final User source, final User target, final Status unfaved) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUnfavorite(sitestreamUser, source, target, unfaved);
    }
  }

  @Override
  protected void onFollow(long sitestreamUser, final User source, final User target) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onFollow(sitestreamUser, source, target);
    }
  }

  @Override
  protected void onUnfollow(long sitestreamUser, final User source, final User target) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUnfollow(sitestreamUser, source, target);
    }
  }

  @Override
  protected void onBlock(long sitestreamUser, final User source, final User blockedUser) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onBlock(sitestreamUser, source, blockedUser);
    }
  }

  @Override
  protected void onUnblock(long sitestreamUser, final User source, final User unblockedUser) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUnblock(sitestreamUser, source, unblockedUser);
    }
  }

  @Override
  protected void onUserListCreation(long sitestreamUser, final User user, final UserList userList) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserListCreation(sitestreamUser, user, userList);
    }
  }

  @Override
  protected void onUserListUpdate(long sitestreamUser, final User user, final UserList userList) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserListUpdate(sitestreamUser, user, userList);
    }
  }

  @Override
  protected void onUserListDeletion(long sitestreamUser, final User user, final UserList userList) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserListDeletion(sitestreamUser, user, userList);
    }
  }

  @Override
  protected void onUserListMemberAddition(long sitestreamUser, final User addedUser, final User user, final UserList userList) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserListMemberAddition(sitestreamUser, addedUser, user, userList);
    }
  }

  @Override
  protected void onUserListMemberDeletion(long sitestreamUser, final User deletedUser, final User user, final UserList userList) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserListMemberDeletion(sitestreamUser, deletedUser, user, userList);
    }
  }

  @Override
  protected void onUserListSubscription(long sitestreamUser, final User subscriber, final User listOwner, final UserList userList) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserListSubscription(sitestreamUser, subscriber, listOwner, userList);
    }
  }

  @Override
  protected void onUserListUnsubscription(long sitestreamUser, final User subscriber, final User listOwner, final UserList userList) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserListUnsubscription(sitestreamUser, subscriber, listOwner, userList);
    }
  }

  @Override
  protected void onUserProfileUpdate(long sitestreamUser, final User user) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onUserProfileUpdate(sitestreamUser, user);
    }
  }

  @Override
  protected void onControlStreamMessage(String streamId) {
    this.streamId.set(streamId);
  }

  @Override
  protected void onDisconnectMessage(DisconnectMessage disconnect) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      if (listener instanceof SitestreamHandler) {
        ((SitestreamHandler) listener).onDisconnectMessage(disconnect);
      }
    }
  }

  @Override
  protected void onStallWarning(StallWarningMessage stallWarning) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      if (listener instanceof SitestreamHandler) {
        ((SitestreamHandler) listener).onStallWarningMessage(stallWarning);
      }
    }
  }

  @Override
  protected void onUnknownMessageType(String msg) {
    for (SiteStreamsListener listener : sitestreamListeners) {
      if (listener instanceof SitestreamHandler) {
        ((SitestreamHandler) listener).onUnknownMessageType(msg);
      } else {
        super.onUnknownMessageType(msg);
      }
    }
  }

  @Override
  protected void onException(Exception e) {
    super.onException(e);
    for (SiteStreamsListener listener : sitestreamListeners) {
      listener.onException(e);
    }
  }

  @Nullable
  public String getStreamId() {
    return streamId.get();
  }
}
