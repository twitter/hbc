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

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Longs;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.httpclient.ControlStreamException;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.test.ArgumentValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SitestreamControllerTest {

  private HttpClient client;
  private Hosts hosts;
  private Authentication auth;
  private HttpUriRequest request;
  private HttpResponse mockResponse;
  private StatusLine mockStatusLine;
  private HttpEntity mockEntity;

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


  private SitestreamController setupSimplControlStreamRequest(int statusCode, String content) throws IOException {
    SitestreamController controlstreams = new SitestreamController(client, hosts, auth);

    when(client.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    when(mockStatusLine.getStatusCode()).thenReturn(statusCode);
    when(mockResponse.getEntity()).thenReturn(mockEntity);
    when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")));

    return controlstreams;
  }

  @Test
  public void testGetInfo() throws IOException, ControlStreamException, URISyntaxException {
    SitestreamController controlstreams = setupSimplControlStreamRequest(200, "{}");
    controlstreams.getInfo("mock_stream_id");
    Mockito.verify(client).execute(argThat(new ArgumentValidator<HttpGet>() {
      @Override
      public void validate(HttpGet get) {
        assertEquals("https://host.com/1.1/site/c/mock_stream_id/info.json", get.getURI().toString());
      }
    }));
  }

  @Test
  public void testDeprecatedGetInfo() throws IOException, ControlStreamException, URISyntaxException {
    SitestreamController controlstreams = setupSimplControlStreamRequest(200, "{}");
    controlstreams.getInfo("mock_stream_id", 1234567899L);
    Mockito.verify(client).execute(argThat(new ArgumentValidator<HttpGet>() {
      public void validate(HttpGet get) throws Exception {
        assertEquals("https://host.com/1.1/site/c/mock_stream_id/info.json", get.getURI().toString());
      }
    }));
  }

  @Test
  public void testAddUser() throws IOException, ControlStreamException, URISyntaxException {
    SitestreamController controlstreams = setupSimplControlStreamRequest(200, "{}");
    controlstreams.addUser("mock_stream_id", 123456789L);
    Mockito.verify(client).execute(argThat(new ArgumentValidator<HttpPost>() {
      public void validate(HttpPost post) throws Exception {
        assertEquals("application/x-www-form-urlencoded", post.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());
        assertEquals("https://host.com/1.1/site/c/mock_stream_id/add_user.json", post.getURI().toString());
        assertEquals("user_id=123456789", consumeUtf8String(post.getEntity().getContent()));
      }
    }));
  }

  @Test
  public void testAddUsers() throws IOException, ControlStreamException, URISyntaxException {
    SitestreamController controlstreams = setupSimplControlStreamRequest(200, "{}");
    controlstreams.addUsers("mock_stream_id", Longs.asList(1111, 2222, 3333, 4444));
    Mockito.verify(client).execute(argThat(new ArgumentValidator<HttpPost>() {
      public void validate(HttpPost post) throws Exception {
        assertEquals("application/x-www-form-urlencoded", post.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());
        assertEquals("https://host.com/1.1/site/c/mock_stream_id/add_user.json", post.getURI().toString());
        assertEquals("user_id=1111%2C2222%2C3333%2C4444", consumeUtf8String(post.getEntity().getContent()));
      }
    }));
  }

  @Test
  public void testAddUsersPrecondition() throws IOException, ControlStreamException {
    SitestreamController controlstreams = new SitestreamController(client, hosts, auth);
    try {
      controlstreams.addUsers("mock_stream_id", Longs.asList(new long[101]));
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }


  @Test
  public void testRemoveUser() throws IOException, ControlStreamException, URISyntaxException {
    SitestreamController controlstreams = setupSimplControlStreamRequest(200, "{}");
    controlstreams.removeUser("mock_stream_id", 123456789L);
    Mockito.verify(client).execute(argThat(new ArgumentValidator<HttpPost>() {
      public void validate(HttpPost post) throws Exception {
        assertEquals("application/x-www-form-urlencoded", post.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());
        assertEquals("https://host.com/1.1/site/c/mock_stream_id/remove_user.json", post.getURI().toString());
        assertEquals("user_id=123456789", consumeUtf8String(post.getEntity().getContent()));
      }
    }));
  }

  @Test
  public void testRemoveUsers() throws IOException, ControlStreamException, URISyntaxException {
    SitestreamController controlstreams = setupSimplControlStreamRequest(200, "{}");
    controlstreams.removeUsers("mock_stream_id", Longs.asList(1111, 2222, 3333, 4444));
    Mockito.verify(client).execute(argThat(new ArgumentValidator<HttpPost>() {
      public void validate(HttpPost post) throws Exception {
        assertEquals("application/x-www-form-urlencoded", post.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());
        assertEquals("https://host.com/1.1/site/c/mock_stream_id/remove_user.json", post.getURI().toString());
        assertEquals("user_id=1111%2C2222%2C3333%2C4444", consumeUtf8String(post.getEntity().getContent()));
      }
    }));
  }

  @Test
  public void testRemoveUsersPrecondition() throws IOException, ControlStreamException {
    SitestreamController controlstreams = new SitestreamController(client, hosts, auth);
    try {
      controlstreams.removeUsers("mock_stream_id", Longs.asList(new long[101]));
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  private String consumeUtf8String(InputStream ins) throws IOException {
    return new String(ByteStreams.toByteArray(ins), "UTF-8");
  }

}
