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

import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.event.EventType;
import com.twitter.hbc.httpclient.auth.BasicAuth;
import com.twitter.hbc.processor.NullProcessor;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.fail;

public class ClientBuilderTest {

  @Test
  public void testBuilderFailure() {
    /**
     * Client builder fails to build with no auth specified
     */
    try {
      new ClientBuilder()
              .hosts(new HttpHosts(Constants.STREAM_HOST))
              .endpoint(new StatusesSampleEndpoint())
              .processor(new NullProcessor())
              .build();
      fail();
    } catch (Exception e) {
      // expected
    }

    /**
     * Client builder fails to build with no host specified
     */
    try {
      new ClientBuilder()
              .endpoint(new StatusesSampleEndpoint())
              .processor(new NullProcessor())
              .authentication(new BasicAuth("username", "password"))
              .build();
      fail();
    } catch (Exception e) {
      // expected
    }


    /**
     * Client builder fails to build with no endpoint specified
     */
    try {
      new ClientBuilder()
              .hosts(new HttpHosts(Constants.STREAM_HOST))
              .processor(new NullProcessor())
              .authentication(new BasicAuth("username", "password"))
              .build();
      fail();
    } catch (Exception e) {
      // expected
    }

    /**
     * Client builder fails to build with no processor specified
     */
    try {
      new ClientBuilder()
              .hosts(new HttpHosts(Constants.STREAM_HOST))
              .endpoint(new StatusesSampleEndpoint())
              .authentication(new BasicAuth("username", "password"))
              .build();
      fail();
    } catch (Exception e) {
      // expected
    }
  }



  @Test
  public void testBuilderSuccess() {
    new ClientBuilder()
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(new StatusesSampleEndpoint())
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .build();

  }

  @Test
  public void testInvalidHttpMethod() {
    try {
      new ClientBuilder()
              .hosts(new HttpHosts(Constants.STREAM_HOST))
              .endpoint(StatusesSampleEndpoint.PATH, "FAIL!")
              .processor(new NullProcessor())
              .authentication(new BasicAuth("username", "password"))
              .build();
      fail();
    } catch (Exception e) {
      // expected
    }
  }

  @Test
  public void testValidHttpMethod() {
    new ClientBuilder()
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .build();

  }

  @Test
  public void testNotNullName() {
    new ClientBuilder()
            .name("abc")
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .build();

  }

  @Test
  public void testGzipEnable() {
    new ClientBuilder()
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .gzipEnabled(true)
            .build();
  }
  @Test
  public void testStringHost() {
    new ClientBuilder()
            .hosts("https://stream.twitter.com")
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .build();
  }

  @Test
  public void testTimeout() {
    new ClientBuilder()
            .hosts("https://stream.twitter.com")
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .socketTimeout(500)
            .connectionTimeout(1000)
            .build();
  }

  @Test
  public void testRetries() {
    new ClientBuilder()
            .hosts("https://stream.twitter.com")
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .socketTimeout(500)
            .retries(2)
            .build();
  }

  @Test
  public void testProxyHost() {
    new ClientBuilder()
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .socketTimeout(500)
            .proxy("https://stream.twitter.com", 22)
            .build();
  }

  @Test
  public void testEventMessageQueue() {
    Event event1 = new Event(EventType.CONNECTION_ATTEMPT, "abc");
    BlockingQueue<Event> blockingQueue = new LinkedBlockingDeque();
    blockingQueue.add(event1);
    new ClientBuilder()
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .eventMessageQueue(blockingQueue)
            .build();
  }

  @Test
  public void testReconnectionMAnager() {
    ReconnectionManager manager  = new ReconnectionManager() {
      @Override
      public void handleExponentialBackoff() {
      }

      @Override
      public void handleLinearBackoff() {
      }

      @Override
      public boolean shouldReconnectOn400s() {
        return false;
      }

      @Override
      public int estimateBackfill(double tps) {
        return 0;
      }

      @Override
      public void resetCounts() {
      }
    };
    new ClientBuilder()
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .reconnectionManager(manager)
            .build();
  }

  @Test
  public void testRateTracker() {
    RateTracker tracker = new RateTracker() {
      @Override
      public void eventObserved() {

      }

      @Override
      public void pause() {

      }

      @Override
      public void resume() {

      }

      @Override
      public void start() {

      }

      @Override
      public void stop() {

      }

      @Override
      public void shutdown() {

      }

      @Override
      public double getCurrentRateSeconds() {
        return 0;
      }
    };
    new ClientBuilder()
            .name("abc")
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .rateTracker(tracker)
            .build();

  }

  @Test
  public void testSchemeRegistry() {
    new ClientBuilder()
            .hosts(new HttpHosts(Constants.STREAM_HOST))
            .endpoint(StatusesSampleEndpoint.PATH, "gEt")
            .processor(new NullProcessor())
            .authentication(new BasicAuth("username", "password"))
            .schemeRegistry(new SchemeRegistry())
            .build();
  }
}
