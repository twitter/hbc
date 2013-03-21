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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.endpoint.Endpoint;
import com.twitter.hbc.core.endpoint.SitestreamEndpoint;
import com.twitter.hbc.httpclient.ControlStreamException;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.twitter.hbc.core.Constants.DEFAULT_CHARSET;

public class SitestreamController {

  private static final Logger logger = LoggerFactory.getLogger(SitestreamController.class);

  private final HttpClient client;
  private final Hosts hosts;
  private final Authentication auth;

  public SitestreamController(HttpClient client, Hosts hosts, Authentication auth) {
    this.client = Preconditions.checkNotNull(client);
    this.hosts = Preconditions.checkNotNull(hosts);
    this.auth = Preconditions.checkNotNull(auth);
  }

  /**
   * TODO: This must be limited to 25 adds per seconds
   */
  public void addUser(String streamId, long userId) throws IOException, ControlStreamException {
    Endpoint endpoint = SitestreamEndpoint.addUserEndpoint(streamId);
    endpoint.addPostParameter(Constants.USER_ID_PARAM, Long.toString(userId));

    HttpUriRequest request = HttpConstants.constructRequest(hosts.nextHost(), endpoint, auth);
    consumeHttpEntityContent(makeControlStreamRequest(request));
  }

  public void removeUser(String streamId, long userId) throws IOException, ControlStreamException {
    Endpoint endpoint = SitestreamEndpoint.removeUserEndpoint(streamId);
    endpoint.addPostParameter(Constants.USER_ID_PARAM, Long.toString(userId));

    HttpUriRequest request = HttpConstants.constructRequest(hosts.nextHost(), endpoint, auth);
    consumeHttpEntityContent(makeControlStreamRequest(request));
  }

  public String getFriends(String streamId, long userId) throws IOException, ControlStreamException {
    return getFriends(streamId, userId, 0);
  }

  public String getFriends(String streamId, long userId, int cursor) throws IOException, ControlStreamException {
    Endpoint endpoint = SitestreamEndpoint.friendsEndpoint(streamId);
    endpoint.addPostParameter(Constants.USER_ID_PARAM, Long.toString(userId));
    endpoint.addPostParameter(Constants.CURSOR_PARAM, Integer.toString(cursor));

    HttpUriRequest request = HttpConstants.constructRequest(hosts.nextHost(), endpoint, auth);
    HttpResponse response = makeControlStreamRequest(request);
    return consumeHttpEntityContent(response);
  }

  public String getInfo(String streamId, long userId) throws IOException, ControlStreamException {
    Endpoint endpoint = SitestreamEndpoint.streamInfoEndpoint(streamId);
    endpoint.addPostParameter(Constants.USER_ID_PARAM, Long.toString(userId));

    HttpUriRequest request = HttpConstants.constructRequest(hosts.nextHost(), endpoint, auth);
    HttpResponse response = makeControlStreamRequest(request);
    return consumeHttpEntityContent(response);
  }

  @VisibleForTesting
  HttpResponse makeControlStreamRequest(HttpUriRequest request) throws IOException, ControlStreamException {
    HttpResponse response = client.execute(request);
    if (response.getStatusLine() == null) {
      throw new ControlStreamException("No status line in response");
    }
    logger.debug("{} returned with status line: {}", request.getURI(), response.getStatusLine());
    if (response.getStatusLine().getStatusCode() != HttpConstants.Codes.SUCCESS) {
      logger.warn("{} returned with status code {}", request.getURI(), response.getStatusLine().getStatusCode());
      if (response.getEntity() != null) {
        // close the resources if the request failed. this might be redundant
        EntityUtils.consume(response.getEntity());
      }
      throw new ControlStreamException(response.getStatusLine());
    }
    return response;
  }

  @VisibleForTesting
  String consumeHttpEntityContent(HttpResponse response) throws IOException {
    InputStream contentStream = response.getEntity().getContent();

    try {
      return CharStreams.toString(new InputStreamReader(contentStream, DEFAULT_CHARSET));
    } finally {
      contentStream.close();
    }
  }
}
