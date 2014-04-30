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
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.StatsReporter;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import com.twitter.hbc.core.event.ConnectionEvent;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.event.EventType;
import com.twitter.hbc.core.event.HttpResponseEvent;
import com.twitter.hbc.core.processor.HosebirdMessageProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe.
 * TODO: better name?!?
 */
class ClientBase implements Runnable {

  private final static Logger logger = LoggerFactory.getLogger(ClientBase.class);

  private final String name;
  private final HttpClient client;

  private final StreamingEndpoint endpoint;
  private final Hosts hosts;
  private final Authentication auth;
  private final HosebirdMessageProcessor processor;
  private final ReconnectionManager reconnectionManager;

  private final AtomicReference<Event> exitEvent;

  private final CountDownLatch isRunning;

  private final RateTracker rateTracker;
  private final BlockingQueue<Event> eventsQueue;
  private final StatsReporter statsReporter;

  private final AtomicBoolean connectionEstablished;
  private final AtomicBoolean reconnect;

  ClientBase(String name, HttpClient client, Hosts hosts, StreamingEndpoint endpoint, Authentication auth,
             HosebirdMessageProcessor processor, ReconnectionManager manager, RateTracker rateTracker) {
    this(name, client, hosts, endpoint, auth, processor, manager, rateTracker, null);
  }

  // TODO: support setting some http timeouts?
  ClientBase(String name, HttpClient client, Hosts hosts, StreamingEndpoint endpoint, Authentication auth,
             HosebirdMessageProcessor processor, ReconnectionManager manager, RateTracker rateTracker,
             @Nullable BlockingQueue<Event> eventsQueue) {
    this.client = Preconditions.checkNotNull(client);
    this.name = Preconditions.checkNotNull(name);

    this.endpoint = Preconditions.checkNotNull(endpoint);
    this.hosts = Preconditions.checkNotNull(hosts);
    this.auth = Preconditions.checkNotNull(auth);

    this.processor = Preconditions.checkNotNull(processor);
    this.reconnectionManager = Preconditions.checkNotNull(manager);
    this.rateTracker = Preconditions.checkNotNull(rateTracker);

    this.eventsQueue = eventsQueue;

    this.exitEvent = new AtomicReference<Event>();

    this.isRunning = new CountDownLatch(1);
    this.statsReporter = new StatsReporter();

    this.connectionEstablished = new AtomicBoolean(false);
    this.reconnect = new AtomicBoolean(false);
  }

  @Override
  public void run() {
    // establish the initial connection
    //   if connection fails due to auth or some other 400, stop immediately
    //   if connection fails due to a 500, back off and retry
    //   if no response or other code, stop immediately
    // begin reading from the stream
    // while the stop signal hasn't been sent, and no IOException from processor, keep processing
    // if  IOException, time to restart the connection:
    //   handle http connection cleanup
    //   do some backoff, set backfill
    // if stop signal set, time to kill/clean the connection, and end this thread.
    try {
      if (client instanceof RestartableHttpClient) {
        ((RestartableHttpClient) client).setup();
      }
      rateTracker.start();
      while (!isDone()) {
        String host = hosts.nextHost();
        if (host == null) {
          setExitStatus(new Event(EventType.STOPPED_BY_ERROR, "No hosts available"));
          break;
        }

        double rate = rateTracker.getCurrentRateSeconds();
        if (!Double.isNaN(rate)) {
          endpoint.setBackfillCount(reconnectionManager.estimateBackfill(rate));
        }

        HttpUriRequest request = HttpConstants.constructRequest(host, endpoint, auth);
        if (request != null) {
          String postContent = null;
          if (endpoint.getHttpMethod().equalsIgnoreCase(HttpConstants.HTTP_POST)) {
            postContent = endpoint.getPostParamString();
          }
          auth.signRequest(request, postContent);
          Connection conn = new Connection(client, processor);
          StatusLine status = establishConnection(conn, request);
          if (handleConnectionResult(status)) {
            rateTracker.resume();
            processConnectionData(conn);
            rateTracker.pause();
          }
          logger.info("{} Done processing, preparing to close connection", name);
          conn.close();
        } else {
          addEvent(
            new Event(
              EventType.CONNECTION_ERROR,
              String.format("Error creating request: %s, %s, %s", endpoint.getHttpMethod(), host, endpoint.getURI())
            )
          );
        }
      }
    } catch (Throwable e) {
      logger.warn(name + " Uncaught exception", e);
      Exception laundered = (e instanceof Exception) ? (Exception) e : new RuntimeException(e);
      setExitStatus(new Event(EventType.STOPPED_BY_ERROR, laundered));
    } finally {
      rateTracker.stop();
      logger.info("{} Shutting down httpclient connection manager", name);
      client.getConnectionManager().shutdown();
      isRunning.countDown();
    }
  }

  @Nullable()
  @VisibleForTesting
  StatusLine establishConnection(Connection conn, HttpUriRequest request) {
    logger.info("{} Establishing a connection", name);
    // establish connection
    StatusLine status = null;
    try {
      addEvent(new ConnectionEvent(EventType.CONNECTION_ATTEMPT, request));
      status = conn.connect(request);
    } catch (UnknownHostException e) {
      // banking on some httpHosts.nextHost() being legitimate, or else this connection will fail.
      logger.warn("{} Unknown host - {}", name, request.getURI().getHost());
      addEvent(new Event(EventType.CONNECTION_ERROR, e));
    } catch (IOException e) {
      logger.warn("{} IOException caught when establishing connection to {}", name, request.getURI());
      addEvent(new Event(EventType.CONNECTION_ERROR, e));
      reconnectionManager.handleLinearBackoff();
    } catch (Exception e) {
      logger.error(String.format("%s Unknown exception while establishing connection to %s", name, request.getURI()), e);
      setExitStatus(new Event(EventType.STOPPED_BY_ERROR, e));
    }
    return status;
  }

  /**
   * @return whether a successful connection has been established
   */
  @VisibleForTesting
  boolean handleConnectionResult(@Nullable StatusLine statusLine) {
    statsReporter.incrNumConnects();
    if (statusLine == null) {
      logger.warn("{} failed to establish connection properly", name);
      addEvent(new Event(EventType.CONNECTION_ERROR, "Failed to establish connection properly"));
      return false;
    }
    int statusCode = statusLine.getStatusCode();
    if (statusCode == HttpConstants.Codes.SUCCESS) {
      logger.debug("{} Connection successfully established", name);
      statsReporter.incrNum200s();
      connectionEstablished.set(true);
      addEvent(new HttpResponseEvent(EventType.CONNECTED, statusLine));
      reconnectionManager.resetCounts();
      return true;
    }

    logger.warn(name + " Error connecting w/ status code - {}, reason - {}", statusCode, statusLine.getReasonPhrase());
    statsReporter.incrNumConnectionFailures();
    addEvent(new HttpResponseEvent(EventType.HTTP_ERROR, statusLine));
    if (HttpConstants.FATAL_CODES.contains(statusCode)) {
      setExitStatus(new Event(EventType.STOPPED_BY_ERROR, "Fatal error code: " + statusCode));
    } else if (statusCode < 500 && statusCode >= 400) {
      statsReporter.incrNum400s();
      // we will retry these a set number of times, then fail
      if (reconnectionManager.shouldReconnectOn400s()) {
        logger.debug("{} Reconnecting on {}", name, statusCode);
        reconnectionManager.handleExponentialBackoff();
      } else {
        logger.debug("{} Reconnecting retries exhausted for {}", name, statusCode);
        setExitStatus(new Event(EventType.STOPPED_BY_ERROR, "Retries exhausted"));
      }
    } else if (statusCode >= 500) {
      statsReporter.incrNum500s();
      reconnectionManager.handleExponentialBackoff();
    } else {
      setExitStatus(new Event(EventType.STOPPED_BY_ERROR, statusLine.getReasonPhrase()));
    }
    return false;
  }

  private void processConnectionData(Connection conn) {
    logger.info("{} Processing connection data", name);
    try {
      addEvent(new Event(EventType.PROCESSING, "Processing messages"));
      while(!isDone() && !reconnect.getAndSet(false)) {
        if (conn.processResponse()) {
          statsReporter.incrNumMessages();
        } else {
          statsReporter.incrNumMessagesDropped();
        }
        rateTracker.eventObserved();
      }
    } catch (RuntimeException e) {
      logger.warn(name + " Unknown error processing connection: ", e);
      statsReporter.incrNumDisconnects();
      addEvent(new Event(EventType.DISCONNECTED, e));
    } catch (IOException ex) {
      // connection issue? whatever. let's try connecting again
      // we can't really diagnosis the actual disconnection reason without parsing (looking at disconnect message)
      // but we can make a good guess at when we're stalling. TODO
      logger.info("{} Disconnected during processing - will reconnect", name);
      statsReporter.incrNumDisconnects();
      addEvent(new Event(EventType.DISCONNECTED, ex));
    } catch (InterruptedException interrupt) {
      // interrupted while trying to append message to queue. exit
      logger.info("{} Thread interrupted during processing, exiting", name);
      statsReporter.incrNumDisconnects();
      setExitStatus(new Event(EventType.STOPPED_BY_ERROR, interrupt));
    } catch (Exception e) {
      // Unexpected exception thrown, killing everything
      logger.warn(name + " Unexpected exception during processing", e);
      statsReporter.incrNumDisconnects();
      setExitStatus(new Event(EventType.STOPPED_BY_ERROR, e));
    }
  }

  private void setExitStatus(Event event) {
    logger.info("{} exit event - {}", name, event.getMessage());
    addEvent(event);
    exitEvent.set(event);
  }

  private void addEvent(Event event) {
    if (eventsQueue != null) {
      if (!eventsQueue.offer(event)) {
        statsReporter.incrNumClientEventsDropped();
      }
    }
  }

  public void reconnect() {
    if (connectionEstablished.get()) {
      reconnect.set(true);
    }
  }

  /**
   * Stops the current connection. No reconnecting will occur. Kills thread + cleanup.
   * Waits for the loop to end
   **/
  public void stop(int waitMillis) throws InterruptedException {
    try {
      if (!isDone()) {
        setExitStatus(new Event(EventType.STOPPED_BY_USER, String.format("Stopped by user: waiting for %d ms", waitMillis)));
      }
      if (!waitForFinish(waitMillis)) {
        logger.warn("{} Client thread failed to finish in {} millis", name, waitMillis);
      }
    } finally {
      rateTracker.shutdown();
    }
  }

  public void shutdown(int millis) {
    try {
      stop(millis);
    } catch (InterruptedException e) {
      logger.warn("Client failed to shutdown due to interruption", e);
    }
  }

  public boolean isDone() {
    return exitEvent.get() != null;
  }

  public Event getExitEvent() {
    if (!isDone()) {
      throw new IllegalStateException(name + " Still running");
    }
    return exitEvent.get();
  }

  public boolean waitForFinish(int millis) throws InterruptedException {
    return isRunning.await(millis, TimeUnit.MILLISECONDS);
  }

  public void waitForFinish() throws InterruptedException {
    isRunning.await();
  }

  @Override
  public String toString() {
    return String.format("%s, endpoint: %s", getName(), endpoint.getURI());
  }

  public String getName() {
    return name;
  }

  public StreamingEndpoint getEndpoint() {
    return endpoint;
  }

  public StatsReporter.StatsTracker getStatsTracker() {
    return statsReporter.getStatsTracker();
  }
}
