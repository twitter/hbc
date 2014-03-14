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
import com.twitter.hbc.twitter4j.handler.UserstreamHandler;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;
import twitter4j.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class Twitter4jUserstreamClient extends BaseTwitter4jClient {

  private final List<UserStreamListener> userstreamListeners;

  public Twitter4jUserstreamClient(Client client, BlockingQueue<String> blockingQueue, List<UserStreamListener> listeners, ExecutorService executorService) {
    super(client, blockingQueue, executorService);
    Preconditions.checkNotNull(listeners);
    this.userstreamListeners = ImmutableList.copyOf(listeners);
  }

  @Override
  protected void onStatus(long sitestreamUser, final Status status) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onStatus(status);
    }
  }

  @Override
  protected void onDelete(long sitestreamUser, StatusDeletionNotice delete) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onDeletionNotice(delete);
    }
  }

  @Override
  protected void onTrackLimitationNotice(long sitestreamUser, final int limit) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onTrackLimitationNotice(limit);
    }
  }

  @Override
  protected void onScrubGeo(long sitestreamUser, long userId, long upToStatusId) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onScrubGeo(userId, upToStatusId);
    }
  }

  @Override
  protected void onException(Exception e) {
    super.onException(e);
    for (UserStreamListener listener : userstreamListeners) {
      listener.onException(e);
    }
  }

  @Override
  protected void onDeleteDirectMessage(long sitestreamUser, long directMessageId, long userId) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onDeletionNotice(directMessageId, userId);
    }
  }

  @Override
  protected void onDirectMessage(long sitestreamUser, final DirectMessage directMessage) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onDirectMessage(directMessage);
    }
  }

  @Override
  protected void onFriends(long sitestreamUser, long[] friendIds) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onFriendList(friendIds);
    }
  }

  @Override
  protected void onFavorite(long sitestreamUser, final User source, final User target, final Status faved) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onFavorite(source, target, faved);
    }
  }

  @Override
  protected void onUnfavorite(long sitestreamUser, final User source, final User target, final Status unfaved) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUnfavorite(source, target, unfaved);
    }
  }

  @Override
  protected void onBlock(long sitestreamUser, final User source, final User blockedUser) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onBlock(source, blockedUser);
    }
  }

  @Override
  protected void onUnblock(long sitestreamUser, final User source, final User unblockedUser) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUnblock(source, unblockedUser);
    }
  }

  @Override
  protected void onRetweet(long sitestreamUser, final User user, final User target, final Status status) {
    for (UserStreamListener listener : userstreamListeners) {
      if (listener instanceof UserstreamHandler) {
        ((UserstreamHandler) listener).onRetweet(user, target, status);
      }
    }
  }

  @Override
  protected void onUserListCreation(long sitestreamUser, final User user, final UserList userList) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserListCreation(user, userList);
    }
  }

  @Override
  protected void onUserListDeletion(long sitestreamUser, final User user, final UserList userList) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserListDeletion(user, userList);
    }
  }

  @Override
  protected void onUserListMemberAddition(long sitestreamUser, final User addedUser, final User user, final UserList userList) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserListMemberAddition(addedUser, user, userList);
    }
  }

  @Override
  protected void onUserListMemberDeletion(long sitestreamUser, final User deletedUser, final User user, final UserList userList) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserListMemberDeletion(deletedUser, user, userList);
    }
  }

  @Override
  protected void onUserListSubscription(long sitestreamUser, final User subscriber, final User listOwner, final UserList userList) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserListSubscription(subscriber, listOwner, userList);
    }
  }

  @Override
  protected void onUserListUnsubscription(long sitestreamUser, final User subscriber, final User listOwner, final UserList userList) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserListUnsubscription(subscriber, listOwner, userList);
    }
  }

  @Override
  protected void onUserListUpdate(long sitestreamUser, final User listOwner, final UserList userList) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserListUpdate(listOwner, userList);
    }
  }

  @Override
  protected void onUserProfileUpdate(long sitestreamUser, final User user) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onUserProfileUpdate(user);
    }
  }

  @Override
  protected void onFollow(long sitestreamUser, final User user, final User target) {
    for (UserStreamListener listener : userstreamListeners) {
      listener.onFollow(user, target);
    }
  }

  @Override
  protected void onUnfollow(long sitestreamUser, final User user, final User target) {
    for (UserStreamListener listener : userstreamListeners) {
      if (listener instanceof UserstreamHandler) {
        ((UserstreamHandler) listener).onUnfollow(user, target);
      }
    }
  }

  @Override
  protected void onDisconnectMessage(DisconnectMessage disconnect) {
    for (UserStreamListener listener : userstreamListeners) {
      if (listener instanceof UserstreamHandler) {
        ((UserstreamHandler) listener).onDisconnectMessage(disconnect);
      }
    }
  }

  @Override
  protected void onStallWarning(StallWarningMessage stallWarning) {
    for (UserStreamListener listener : userstreamListeners) {
      if (listener instanceof UserstreamHandler) {
        ((UserstreamHandler) listener).onStallWarningMessage(stallWarning);
      }
    }
  }

  @Override
  protected void onUnknownMessageType(String msg) {
    for (UserStreamListener listener : userstreamListeners) {
      if (listener instanceof UserstreamHandler) {
        ((UserstreamHandler) listener).onUnknownMessageType(msg);
      } else {
        super.onUnknownMessageType(msg);
      }
    }
  }
}
