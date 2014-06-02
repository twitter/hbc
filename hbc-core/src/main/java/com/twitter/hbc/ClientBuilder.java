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

package com.twitter.hbc;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.RawEndpoint;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.HosebirdMessageProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A builder class for our BasicClient.
 */
public class ClientBuilder {

  private static final AtomicInteger clientNum = new AtomicInteger(0);
  protected Authentication auth;
  protected Hosts hosts;
  protected HosebirdMessageProcessor processor;
  protected StreamingEndpoint endpoint;
  protected boolean enableGZip;
  protected String name;
  protected RateTracker rateTracker;
  protected final ExecutorService executorService;
  protected BlockingQueue<Event> eventQueue;
  protected ReconnectionManager reconnectionManager;
  protected int socketTimeoutMillis;
  protected int connectionTimeoutMillis;
  protected SchemeRegistry schemeRegistry;

  private static String loadVersion() {
    String userAgent = "Hosebird-Client";
    try {
      InputStream stream = ClientBuilder.class.getClassLoader().getResourceAsStream("build.properties");
      try {
        Properties prop = new Properties();
        prop.load(stream);

        String version = prop.getProperty("version");
        userAgent += " " + version;
      } finally {
        stream.close();
      }
    } catch (IOException ex) {
      // ignore
    }
    return userAgent;
  }

  private static final String USER_AGENT = loadVersion();

  public ClientBuilder() {
    enableGZip = true;
    name = "hosebird-client-" + clientNum.getAndIncrement();
    ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("hosebird-client-io-thread-%d")
            .build();
    executorService = Executors.newSingleThreadExecutor(threadFactory);

    ThreadFactory rateTrackerThreadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("hosebird-client-rateTracker-thread-%d")
            .build();

    ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1, rateTrackerThreadFactory);
    rateTracker = new BasicRateTracker(30000, 100, true, scheduledExecutor);
    reconnectionManager = new BasicReconnectionManager(5);

    socketTimeoutMillis = 60000;
    connectionTimeoutMillis = 4000;

    schemeRegistry = SchemeRegistryFactory.createDefault();
  }

  /**
   * @param name Name of the client used for logging and other diagnostic purposes.
   */
  public ClientBuilder name(String name) {
    this.name = Preconditions.checkNotNull(name);
    return this;
  }

  /**
   * @param gzip Turn gzip on or off. Enabled by default
   */
  public ClientBuilder gzipEnabled(boolean gzip) {
    this.enableGZip = gzip;
    return this;
  }

  /**
   * @param host Http host in the form of <scheme>://<host>
   */
  public ClientBuilder hosts(String host) {
    this.hosts = new HttpHosts(Preconditions.checkNotNull(host));
    return this;
  }

  public ClientBuilder hosts(Hosts hosts) {
    this.hosts = Preconditions.checkNotNull(hosts);
    return this;
  }

  /**
   * @param endpoint StreamingEndpoint that the client will connect to
   */
  public ClientBuilder endpoint(StreamingEndpoint endpoint){
    this.endpoint = Preconditions.checkNotNull(endpoint);
    return this;
  }

  public ClientBuilder authentication(Authentication auth) {
    this.auth = auth;
    return this;
  }

  public ClientBuilder processor(HosebirdMessageProcessor processor) {
    this.processor = processor;
    return this;
  }

  public ClientBuilder eventMessageQueue(BlockingQueue<Event> events) {
    this.eventQueue = events;
    return this;
  }

  public ClientBuilder socketTimeout(int millis) {
    this.socketTimeoutMillis = millis;
    return this;
  }

  public ClientBuilder connectionTimeout(int millis) {
    this.connectionTimeoutMillis = millis;
    return this;
  }

  /**
   * @param retries Number of retries to attempt when we experience retryable connection errors
   */
  public ClientBuilder retries(int retries) {
    this.reconnectionManager = new BasicReconnectionManager(retries);
    return this;
  }

  public ClientBuilder reconnectionManager(ReconnectionManager manager) {
      this.reconnectionManager = Preconditions.checkNotNull(manager);
      return this;
  }

  public ClientBuilder endpoint(String uri, String httpMethod) {
    Preconditions.checkNotNull(uri);
    this.endpoint = new RawEndpoint(uri, httpMethod);
    return this;
  }

  public ClientBuilder rateTracker(RateTracker rateTracker) {
      this.rateTracker = Preconditions.checkNotNull(rateTracker);
      return this;
  }

  public ClientBuilder schemeRegistry(SchemeRegistry schemeRegistry) {
      this.schemeRegistry = Preconditions.checkNotNull(schemeRegistry);
      return this;
  }

  public BasicClient build() {
    HttpParams params = new BasicHttpParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setUserAgent(params, USER_AGENT);
    HttpConnectionParams.setSoTimeout(params, socketTimeoutMillis);
    HttpConnectionParams.setConnectionTimeout(params, connectionTimeoutMillis);
    return new BasicClient(name, hosts, endpoint, auth, enableGZip, processor, reconnectionManager,
            rateTracker, executorService, eventQueue, params, schemeRegistry);
  }
}

