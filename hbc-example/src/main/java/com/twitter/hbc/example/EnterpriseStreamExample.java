package com.twitter.hbc.example;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.EnterpriseStreamingEndpoint;
import com.twitter.hbc.core.processor.SimpleStringProcessor;
import com.twitter.hbc.httpclient.auth.BasicAuth;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EnterpriseStreamExample {

  public static void run(String username, String password, String account, String label, String product) throws InterruptedException {
    BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

    BasicAuth auth = new BasicAuth(username, password);

    EnterpriseStreamingEndpoint endpoint = new EnterpriseStreamingEndpoint(account, product, label);

    // Create a new BasicClient. By default gzip is enabled.
    Client client = new ClientBuilder()
            .name("PowerTrackClient-01")
            .hosts(Constants.ENTERPRISE_STREAM_HOST)
            .endpoint(endpoint)
            .authentication(auth)
            .processor(new SimpleStringProcessor(queue))
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

  public static void main(String[] args) {
    try {
      EnterpriseStreamExample.run(args[0], args[1], args[2], args[3], args[4]);
    } catch (InterruptedException e) {
      System.out.println(e);
    }
  }
}
