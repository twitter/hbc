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

package com.twitter.hbc.example;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.SitestreamController;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.SitestreamEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.ControlStreamException;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jSitestreamClient;
import twitter4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class SitestreamExample {

  // A no-op SiteStreamsListener
  private SiteStreamsListener listener = new SiteStreamsListener() {
    @Override
    public void onDisconnectionNotice(String line) { }

    @Override
    public void onStatus(long forUser, Status status) { }

    @Override
    public void onDeletionNotice(long forUser, StatusDeletionNotice statusDeletionNotice) { }

    @Override
    public void onFriendList(long forUser, long[] friendIds) { }

    @Override
    public void onFavorite(long forUser, User source, User target, Status status) { }

    @Override
    public void onUnfavorite(long forUser, User source, User target, Status status) { }

    @Override
    public void onFollow(long forUser, User source, User target) { }

    @Override
    public void onUnfollow(long forUser, User source, User target) { }

    @Override
    public void onDirectMessage(long forUser, DirectMessage directMessage) { }

    @Override
    public void onDeletionNotice(long forUser, long dmId, long userId) { }

    @Override
    public void onUserListMemberAddition(long forUser, User source, User target, UserList userList) { }

    @Override
    public void onUserListMemberDeletion(long forUser, User source, User target, UserList userList) { }

    @Override
    public void onUserListSubscription(long forUser, User source, User target, UserList userList) { }

    @Override
    public void onUserListUnsubscription(long forUser, User source, User target, UserList userList) { }

    @Override
    public void onUserListCreation(long forUser, User source, UserList userList) { }

    @Override
    public void onUserListUpdate(long forUser, User source, UserList userList) { }

    @Override
    public void onUserListDeletion(long forUser, User source, UserList userList) { }

    @Override
    public void onUserProfileUpdate(long forUser, User user) { }

    @Override
    public void onBlock(long forUser, User source, User target) { }

    @Override
    public void onUnblock(long forUser, User source, User target) { }

    @Override
    public void onException(Exception e) { }
  };

  public static void main(String[] args) {
    try {
      SitestreamExample sitestreamExample = new SitestreamExample();

      sitestreamExample.run(args[0], args[1], args[2], args[3]);

    } catch (InterruptedException e) {
      System.out.println(e);
    } catch (IOException e) {
      System.out.println(e);
    } catch (ControlStreamException e) {
      System.out.println(e);
    }
  }

  public void run(String consumerKey, String consumerSecret, String token, String tokenSecret)
          throws InterruptedException, ControlStreamException, IOException {
    // Create an appropriately sized blocking queue
    BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

    // Define our endpoint: By default, delimited=length is set (we need this for our processor)
    // and stall warnings are on.
    List<Long> followings = new ArrayList<Long>();
    followings.add(111111111L);
    followings.add(222222222L);

    SitestreamEndpoint endpoint = new SitestreamEndpoint(followings);
    Authentication auth = new OAuth1(consumerKey, consumerSecret, token, tokenSecret);

    // Create a new BasicClient. By default gzip is enabled.
    BasicClient client = new ClientBuilder()
            .hosts(Constants.SITESTREAM_HOST)
            .endpoint(endpoint)
            .authentication(auth)
            .processor(new StringDelimitedProcessor(queue))
            .build();

    // Create an executor service which will spawn threads to do the actual work of parsing the incoming messages and
    // calling the listeners on each message
    int numProcessingThreads = 4;
    ExecutorService service = Executors.newFixedThreadPool(numProcessingThreads);

    // Wrap our BasicClient with the twitter4j client
    Twitter4jSitestreamClient t4jClient = new Twitter4jSitestreamClient(
            client, queue, Lists.newArrayList(listener), service);

    // Establish a connection
    t4jClient.connect();
    for (int threads = 0; threads < numProcessingThreads; threads++) {
      // This must be called once per processing thread
      t4jClient.process();
    }

    Thread.sleep(5000);

    // Create a sitestream controller to issue controlstream requests
    SitestreamController controller = new SitestreamController(auth);

    controller.getFriends(t4jClient.getStreamId(), 12345L);
    controller.addUser(t4jClient.getStreamId(), 987765L);

    client.stop();
  }
}
