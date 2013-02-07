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
import com.twitter.hbc.httpclient.auth.BasicAuth;
import com.twitter.hbc.processor.NullProcessor;
import org.junit.Test;

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
}
