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
import com.twitter.hbc.twitter4j.handler.StatusStreamHandler;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Threadsafe
 */
public class Twitter4jStatusClient extends BaseTwitter4jClient {

  // immutable
  private final List<? extends StatusListener> statusListeners;

  public Twitter4jStatusClient(Client client, BlockingQueue<String> blockingQueue, List<? extends StatusListener> listeners, ExecutorService executorService) {
    super(client, blockingQueue, executorService);
    Preconditions.checkNotNull(listeners);
    this.statusListeners = ImmutableList.copyOf(listeners);
  }

  @Override
  protected void onStatus(long sitestreamUser, final Status status) {
    for (StatusListener listener : statusListeners) {
      listener.onStatus(status);
    }
  }

  @Override
  protected void onDelete(long sitestreamUser, StatusDeletionNotice delete) {
    for (StatusListener listener : statusListeners) {
      listener.onDeletionNotice(delete);
    }
  }

  @Override
  protected void onTrackLimitationNotice(long sitestreamUser, final int limit) {
    for (StatusListener listener : statusListeners) {
      listener.onTrackLimitationNotice(limit);
    }
  }

  @Override
  protected void onScrubGeo(long sitestreamUser, long userId, long upToStatusId) {
    for (StatusListener listener : statusListeners) {
      listener.onScrubGeo(userId, upToStatusId);
    }
  }

  @Override
  protected void onDisconnectMessage(DisconnectMessage disconnect) {
    for (StatusListener listener : statusListeners) {
      if (listener instanceof StatusStreamHandler) {
        ((StatusStreamHandler) listener).onDisconnectMessage(disconnect);
      }
    }
  }

  @Override
  protected void onStallWarning(StallWarningMessage stallWarning) {
    for (StatusListener listener : statusListeners) {
      if (listener instanceof StatusStreamHandler) {
        ((StatusStreamHandler) listener).onStallWarningMessage(stallWarning);
      }
    }
  }

  @Override
  protected void onUnknownMessageType(String msg) {
    for (StatusListener listener : statusListeners) {
      if (listener instanceof StatusStreamHandler) {
        ((StatusStreamHandler) listener).onUnknownMessageType(msg);
      } else {
        super.onUnknownMessageType(msg);
      }
    }
  }

  @Override
  protected void onException(Exception e) {
    super.onException(e);
    for (StatusListener listener : statusListeners) {
      listener.onException(e);
    }
  }
}
