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
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.endpoint.DefaultStreamingEndpoint;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class EndpointTest {

  @Test
  public void testDefaultToCurrentApiVersion() throws InterruptedException {
    StreamingEndpoint endpoint = new DefaultStreamingEndpoint("/path", HttpConstants.HTTP_GET, false);
    // "/<api-version>/path"
    assertEquals(endpoint.getURI().split("/")[1], Constants.CURRENT_API_VERSION);
  }

  @Test
  public void testCanSetApiVersion() {
    StreamingEndpoint endpoint = new DefaultStreamingEndpoint("/path", HttpConstants.HTTP_GET, false);
    endpoint.setApiVersion("2");
    assertEquals(endpoint.getURI().split("/")[1], "2");
  }
}
