/**
 * Copyright 2014 Twitter, Inc.
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

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.RealTimeEnterpriseStreamingEndpoint;
import com.twitter.hbc.core.processor.LineStringProcessor;
import com.twitter.hbc.httpclient.auth.BasicAuth;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EnterpriseStreamExample {

  public static void run(String username,
                         String password,
                         String account,
                         String label,
                         String product) throws InterruptedException {
    BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

    BasicAuth auth = new BasicAuth(username, password);

    RealTimeEnterpriseStreamingEndpoint endpoint = new RealTimeEnterpriseStreamingEndpoint(account, product, label);

    // Create a new BasicClient. By default gzip is enabled.
    Client client = new ClientBuilder()
            .name("PowerTrackClient-01")
            .hosts(Constants.ENTERPRISE_STREAM_HOST)
            .endpoint(endpoint)
            .authentication(auth)
            .processor(new LineStringProcessor(queue))
            .build();

    // Establish a connection
    client.connect();

    // Do whatever needs to be done with messages
    for (int msgRead = 0; msgRead < 1000; msgRead++) {
      String msg = queue.take();
      System.out.println(msg);
    }

    client.stop();
  }

//  public static void main(String[] args) {
//    try {
//      EnterpriseStreamExample.run(args[0], args[1], args[2], args[3], args[4]);
//    } catch (InterruptedException e) {
//      System.out.println(e);
//    }
//  }
}
