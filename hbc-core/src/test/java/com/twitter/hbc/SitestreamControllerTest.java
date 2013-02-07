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
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.httpclient.ControlStreamException;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SitestreamControllerTest {

  private HttpClient client;
  private Hosts hosts;
  private Authentication auth;
  private HttpUriRequest request;
  private HttpResponse mockResponse;
  private StatusLine mockStatusLine;
  private HttpEntity mockEntity;
  private InputStream mockContent = mock(InputStream.class);

  @Before
  public void setup() {
    client = mock(HttpClient.class);
    hosts = new HttpHosts("https://host.com");
    auth = mock(Authentication.class);
    request = mock(HttpUriRequest.class);
    mockResponse = mock(HttpResponse.class);
    mockStatusLine = mock(StatusLine.class);
    mockEntity = mock(HttpEntity.class);
  }

  @Test
  public void testFailControlStreamRequestOnNon200s() throws IOException, URISyntaxException {
    SitestreamController controlstreams = new SitestreamController(client, hosts, auth);

    when(client.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    when(mockStatusLine.getStatusCode()).thenReturn(400);

    try {
      controlstreams.makeControlStreamRequest(request);
      fail();
    } catch (ControlStreamException c) {
      // expected
    }

  }

  @Test
  public void testProperlyCloseEntityContent() throws IOException, ControlStreamException {
    SitestreamController controlstreams = new SitestreamController(client, hosts, auth);
    InputStream stream = spy(new ByteArrayInputStream("message".getBytes(Constants.DEFAULT_CHARSET)));

    when(mockResponse.getEntity()).thenReturn(mockEntity);
    when(mockEntity.getContent()).thenReturn(stream);

    controlstreams.consumeHttpEntityContent(mockResponse);
    verify(stream).close();
  }

  @Test
  public void testCloseEntityContentOnError() throws IOException, ControlStreamException {
    SitestreamController controlstreams = new SitestreamController(client, hosts, auth);
    InputStream mockStream = mock(InputStream.class);

    when(mockResponse.getEntity()).thenReturn(mockEntity);
    when(mockEntity.getContent()).thenReturn(mockStream);
    when(mockStream.read(any(byte[].class), anyInt(), anyInt()))
      .thenReturn(10)
      .thenThrow(new IOException());

    try {
      controlstreams.consumeHttpEntityContent(mockResponse);
      fail();
    } catch (IOException e) {
      // expected
    }
    verify(mockStream).close();
  }
}
