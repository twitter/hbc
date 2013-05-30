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

import com.twitter.hbc.BasicReconnectionManager;
import com.twitter.hbc.RateTracker;
import com.twitter.hbc.ReconnectionManager;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.RawEndpoint;
import com.twitter.hbc.core.processor.HosebirdMessageProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ClientBaseTest {

  private HttpClient mock;
  private HttpResponse mockResponse;
  private StatusLine mockStatusLine;
  private Connection mockConnection;
  private ReconnectionManager mockReconnectionManager;
  private HosebirdMessageProcessor mockProcessor;
  private Authentication mockAuth;
  private RateTracker mockRateTracker;

  private InputStream mockInputStream;

  @Before
  public void setup() throws Exception {
    mock = mock(HttpClient.class);
    mockResponse = mock(HttpResponse.class);
    mockStatusLine = mock(StatusLine.class);

    mockConnection = mock(Connection.class);
    mockReconnectionManager = mock(BasicReconnectionManager.class);
    mockRateTracker = mock(RateTracker.class);

    mockInputStream = mock(InputStream.class);
    mockAuth = mock(Authentication.class);

    mockProcessor = mock(HosebirdMessageProcessor.class);

    HttpEntity mockHttpEntity = mock(HttpEntity.class);

    // set up required mocks to mock out all of the clientbase stuff
    when(mock.execute(any(HttpUriRequest.class)))
            .thenReturn(mockResponse);
    when(mockResponse.getStatusLine())
            .thenReturn(mockStatusLine);
    when(mockResponse.getEntity())
            .thenReturn(mockHttpEntity);
    when(mockHttpEntity.getContent())
            .thenReturn(mockInputStream);
  }

  @Test
  public void testProperlyHandleSuccessfulConnection() {
    ClientBase clientBase = new ClientBase("name",
            mock, new HttpHosts("http://hi"), new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );

    mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode())
            .thenReturn(HttpConstants.Codes.SUCCESS);

    assertTrue(clientBase.handleConnectionResult(mockStatusLine));

    InOrder inOrder = inOrder(mockStatusLine, mockReconnectionManager);
    inOrder.verify(mockStatusLine).getStatusCode();
    inOrder.verify(mockReconnectionManager).resetCounts();

    assertFalse(clientBase.isDone());
  }

  @Test
  public void testHandleIOExceptionOnConnection() throws IOException {
    ClientBase clientBase = new ClientBase("name",
            mock, new HttpHosts("http://hi"), new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );
    when(mockConnection.connect(any(HttpUriRequest.class)))
            .thenThrow(new IOException());

    HttpUriRequest mockRequest = mock(HttpUriRequest.class);
    assertNull(clientBase.establishConnection(mockConnection, mockRequest));

    InOrder inOrder = inOrder(mockConnection, mockReconnectionManager);

    inOrder.verify(mockConnection).connect(any(HttpUriRequest.class));
    inOrder.verify(mockReconnectionManager).handleLinearBackoff();

    assertFalse(clientBase.isDone());
  }

  @Test
  public void testRetryTransientAuthFailures() {
    ClientBase clientBase = new ClientBase("name",
            mock, new HttpHosts("http://hi"), new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );

    when(mockStatusLine.getStatusCode())
            .thenReturn(401);
    when(mockStatusLine.getReasonPhrase())
            .thenReturn("reason");
    when(mockReconnectionManager.shouldReconnectOn400s())
            .thenReturn(true, true, false);

    // auth failure 3 times. We'll retry the first two times, but give up on the 3rd
    clientBase.handleConnectionResult(mockStatusLine);
    clientBase.handleConnectionResult(mockStatusLine);
    verify(mockReconnectionManager, times(2)).handleExponentialBackoff();
    assertFalse(clientBase.isDone());
    clientBase.handleConnectionResult(mockStatusLine);
    verify(mockReconnectionManager, times(2)).handleExponentialBackoff();
    assertTrue(clientBase.isDone());
  }

  @Test
  public void testUnknownEndpointFails() {
    ClientBase clientBase = new ClientBase("name",
            mock, new HttpHosts("http://hi"), new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );
    when(mockStatusLine.getStatusCode())
            .thenReturn(404);
    when(mockStatusLine.getReasonPhrase())
            .thenReturn("reason");
    clientBase.handleConnectionResult(mockStatusLine);
    assertTrue(clientBase.isDone());
  }

  @Test
  public void testServiceUnavailable() {
    ClientBase clientBase = new ClientBase("name",
            mock, new HttpHosts("http://hi"), new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );
    when(mockStatusLine.getStatusCode())
            .thenReturn(503);

    clientBase.handleConnectionResult(mockStatusLine);
    clientBase.handleConnectionResult(mockStatusLine);
    clientBase.handleConnectionResult(mockStatusLine);
    clientBase.handleConnectionResult(mockStatusLine);

    verify(mockReconnectionManager, times(4)).handleExponentialBackoff();
    assertFalse(clientBase.isDone());
  }
}
