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

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.twitter.hbc.core.processor.HosebirdMessageProcessor;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStream;

public class Connection {

  private final HttpClient client;

  private HttpResponse response;
  private HttpUriRequest request;
  private InputStream stream;

  private final HosebirdMessageProcessor processor;

  public Connection(HttpClient client, HosebirdMessageProcessor processor) {
    this.client = Preconditions.checkNotNull(client);
    this.processor = Preconditions.checkNotNull(processor);
  }

  public StatusLine connect(HttpUriRequest request) throws IOException {
    this.request = request;
    this.response = client.execute(request);
    this.stream = response.getEntity().getContent();
    processor.setup(stream);
    return response.getStatusLine();
  }

  public boolean processResponse() throws IOException, InterruptedException {
    return processor.process();
  }

  public void close() {
    if (this.request != null && !this.request.isAborted()) {
      // aborting the request
      this.request.abort();
    }
    if (client instanceof RestartableHttpClient) {
      // restart the entire client
      ((RestartableHttpClient) client).restart();
    }
    try {
      Closeables.close(this.stream, true);
    } catch (IOException e) {
      throw new RuntimeException(e); // should never happen
    }
  }
}
