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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.twitter.hbc.BasicReconnectionManager;
import com.twitter.hbc.RateTracker;
import com.twitter.hbc.ReconnectionManager;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.RawEndpoint;
import com.twitter.hbc.core.event.EventType;
import com.twitter.hbc.core.processor.HosebirdMessageProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BasicClientTest {

  private HttpClient mockClient;
  private HttpResponse mockResponse;
  private HttpEntity mockHttpEntity;
  private StatusLine mockStatusLine;
  private ReconnectionManager mockReconnectionManager;
  private HosebirdMessageProcessor mockProcessor;
  private Authentication mockAuth;
  private RateTracker mockRateTracker;

  private InputStream mockInputStream;

  private ClientConnectionManager mockConnectionManager;

  private final ExecutorService executorService;

  public BasicClientTest() {
    ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("hosebird-client-unit-test-%d")
            .build();
    executorService = Executors.newSingleThreadExecutor(threadFactory);
  }

  @Before
  public void setup() throws Exception {
    mockClient = mock(HttpClient.class);
    mockResponse = mock(HttpResponse.class);
    mockStatusLine = mock(StatusLine.class);

    mockReconnectionManager = mock(BasicReconnectionManager.class);
    mockConnectionManager = mock(ClientConnectionManager.class);
    mockRateTracker = mock(RateTracker.class);

    mockInputStream = mock(InputStream.class);
    mockAuth = mock(Authentication.class);

    mockProcessor = mock(HosebirdMessageProcessor.class);

    mockHttpEntity = mock(HttpEntity.class);

    // set up required mocks to mock out all of the clientbase stuff
    when(mockClient.execute(any(HttpUriRequest.class)))
            .thenReturn(mockResponse);
    when(mockClient.getConnectionManager())
            .thenReturn(mockConnectionManager);

    when(mockResponse.getStatusLine())
            .thenReturn(mockStatusLine);
    when(mockResponse.getEntity())
            .thenReturn(mockHttpEntity);
    when(mockHttpEntity.getContent())
            .thenReturn(mockInputStream);
    when(mockStatusLine.getReasonPhrase())
            .thenReturn("reason");
  }

  // These tests are going to get a little hairy in terms of mocking, but doable.
  // Some of the functionality is already tested in ClientBaseTest, but this tests
  // the overall flow. Worth it?

  @Test
  public void testIOExceptionDuringProcessing() throws Exception {
    ClientBase clientBase = new ClientBase("name",
            mockClient, new HttpHosts("http://hi"), new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );
    BasicClient client = new BasicClient(clientBase, executorService);
    final CountDownLatch latch = new CountDownLatch(1);
    when(mockStatusLine.getStatusCode())
            .thenReturn(200);

    doNothing().when(mockProcessor).setup(any(InputStream.class));
    doThrow(new IOException()).
            doThrow(new IOException()).
            doThrow(new IOException()).
            doAnswer(new Answer() {
              @Override
              public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                latch.countDown();
                return null;
              }
            }).when(mockProcessor).process();

    client.connect();
    latch.await();
    assertFalse(clientBase.isDone());
    verify(mockProcessor, times(4)).setup(any(InputStream.class));
    // throw 3 exceptions, 4th one keeps going
    verify(mockProcessor, atLeast(4)).process();

    client.stop();
    verify(mockConnectionManager, atLeastOnce()).shutdown();
    assertTrue(client.isDone());
    assertEquals(EventType.STOPPED_BY_USER, clientBase.getExitEvent().getEventType());
  }

  @Test
  public void testInterruptedExceptionDuringProcessing() throws Exception {
    ClientBase clientBase = new ClientBase("name",
            mockClient, new HttpHosts("http://hi"), new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );

    when(mockStatusLine.getStatusCode())
            .thenReturn(200);

    doThrow(new InterruptedException()).when(mockProcessor).process();

    when(mockClient.getConnectionManager())
            .thenReturn(mockConnectionManager);

    BasicClient client = new BasicClient(clientBase, executorService);

    assertFalse(clientBase.isDone());
    client.connect();
    assertTrue(client.waitForFinish(100));
    assertTrue(client.isDone());
    verify(mockProcessor).setup(any(InputStream.class));
    verify(mockConnectionManager, atLeastOnce()).shutdown();
    assertEquals(EventType.STOPPED_BY_ERROR, client.getExitEvent().getEventType());
    assertTrue(client.getExitEvent().getUnderlyingException() instanceof InterruptedException);
  }

  @Test
  public void testConnectionRetries() throws Exception {
    HttpHosts mockHttpHosts = mock(HttpHosts.class);
    ClientBase clientBase = new ClientBase("name",
            mockClient, mockHttpHosts, new RawEndpoint("/endpoint", HttpConstants.HTTP_GET), mockAuth,
            mockProcessor, mockReconnectionManager, mockRateTracker
    );

    BasicClient client = new BasicClient(clientBase, executorService);
    final CountDownLatch latch = new CountDownLatch(1);
    when(mockHttpHosts.nextHost())
            .thenReturn("http://somehost.com");
    when(mockClient.execute(any(HttpUriRequest.class)))
            .thenReturn(mockResponse)
            .thenReturn(mockResponse)
            .thenThrow(new IOException())
            .thenReturn(mockResponse);
    when(mockStatusLine.getStatusCode())
            .thenReturn(HttpConstants.Codes.UNAUTHORIZED)
            .thenReturn(HttpConstants.Codes.SERVICE_UNAVAILABLE)
            .thenReturn(HttpConstants.Codes.SUCCESS);

    // turn off the client when we start processing
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        latch.countDown();
        return null;
      }
    }).when(mockProcessor).process();

    // for 401 Unauthorized
    when(mockReconnectionManager.shouldReconnectOn400s()).thenReturn(true);

    /** for shutdown **/
    when(mockClient.getConnectionManager())
            .thenReturn(mockConnectionManager);

    assertFalse(clientBase.isDone());
    client.connect();
    latch.await();
    client.stop();
    assertTrue(client.isDone());

    // exponential backoff twice: once for 401 once for 503
    verify(mockReconnectionManager, times(2)).handleExponentialBackoff();
    // for thrown IOException
    verify(mockReconnectionManager).handleLinearBackoff();
    // for successful connection
    verify(mockReconnectionManager).resetCounts();

    // finally start setting up processor/processing for the last attempt that goes through
    verify(mockProcessor, atLeastOnce()).setup(any(InputStream.class));
    verify(mockProcessor, atLeastOnce()).process();

    assertEquals(EventType.STOPPED_BY_USER, clientBase.getExitEvent().getEventType());
    verify(mockConnectionManager, atLeastOnce()).shutdown();
  }
}
