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

package com.twitter.hbc.httpclient;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.twitter.hbc.RateTracker;
import com.twitter.hbc.ReconnectionManager;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.StatsReporter;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.HosebirdMessageProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class and classes in this package will depend on HttpClient
 */

public class BasicClient implements Client {

  private final static int DEFAULT_STOP_TIMEOUT_MILLIS = 5000;

  private final ExecutorService executorService;

  private final ClientBase clientBase;
  private final AtomicBoolean canRun;

  private final static Logger logger = LoggerFactory.getLogger(BasicClient.class);

  public BasicClient(String name, Hosts hosts, StreamingEndpoint endpoint, Authentication auth, boolean enableGZip, HosebirdMessageProcessor processor,
                     ReconnectionManager reconnectionManager, RateTracker rateTracker, ExecutorService executorService,
                     @Nullable BlockingQueue<Event> eventsQueue, HttpParams params, SchemeRegistry schemeRegistry) {
    Preconditions.checkNotNull(auth);
    HttpClient client;
    if (enableGZip) {
      client = new RestartableHttpClient(auth, enableGZip, params, schemeRegistry);
    } else {
      DefaultHttpClient defaultClient = new DefaultHttpClient(new PoolingClientConnectionManager(schemeRegistry), params);

      /** Set auth **/
      auth.setupConnection(defaultClient);
      client = defaultClient;
    }

    this.canRun = new AtomicBoolean(true);
    this.executorService = executorService;
    this.clientBase = new ClientBase(name, client, hosts, endpoint, auth, processor, reconnectionManager, rateTracker, eventsQueue);
  }

  /**
   * For testing only
   */
  @VisibleForTesting
  BasicClient(final ClientBase clientBase, ExecutorService executorService) {
    this.canRun = new AtomicBoolean(true);
    this.clientBase = clientBase;
    this.executorService = executorService;
  }

  /**
   * {@inheritDoc}
   * Forks a new thread to do the IO in.
   */
  @Override
  public void connect() {
    if (!canRun.compareAndSet(true, false) || clientBase.isDone()) {
      throw new IllegalStateException("There is already a connection thread running for " + this.clientBase);
    }
    executorService.execute(clientBase);
    logger.info("New connection executed: {}", this.clientBase);
  }

  @Override
  public StatsReporter.StatsTracker getStatsTracker() {
    return clientBase.getStatsTracker();
  }

  public boolean isDone() {
    return clientBase.isDone();
  }

  /**
   * This method should only be called after the client is done
   */
  public Event getExitEvent() {
    return clientBase.getExitEvent();
  }

  @Override
  public void stop() {
    stop(DEFAULT_STOP_TIMEOUT_MILLIS);
  }

  @Override
  public void stop(int waitMillis) {
    logger.info("Stopping the client: " + this.clientBase);
    try {
      clientBase.stop(waitMillis);
      logger.info("Successfully stopped the client: {}", this.clientBase);
    } catch (InterruptedException e) {
      logger.info("Thread interrupted when attempting to stop the client: {}", this.clientBase);
    }
    executorService.shutdown();
  }

  @Override
  public String getName() {
    return clientBase.getName();
  }

  @Override
  public StreamingEndpoint getEndpoint() {
    return clientBase.getEndpoint();
  }

  @Override
  public void reconnect() {
    clientBase.reconnect();
  }

  @VisibleForTesting
  boolean waitForFinish(int millis) throws InterruptedException {
    return clientBase.waitForFinish(millis);
  }
}
