package com.twitter.hbc.httpclient;

import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.HttpParams;
import org.junit.Before;
import org.junit.Test;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import java.net.URI;
import java.net.UnknownHostException;

import static org.mockito.Mockito.mock;

/**
 * Created by oparry on 5/29/14.
 */
public class RestartableHttpClientTest {
  private Authentication mockAuth;
  private SchemeRegistry defaultSchemeRegistry;
  private HttpParams mockParams;
  private HttpUriRequest request;

  @Before
  public void setup() throws Exception {
    mockAuth = mock(Authentication.class);
    mockParams = mock(HttpParams.class);
    defaultSchemeRegistry = SchemeRegistryFactory.createDefault();
    request = new HttpGet("http://hi");
  }

  @Test
  public void testRestart() throws Exception {
    RestartableHttpClient client = new RestartableHttpClient(mockAuth, true, mockParams, defaultSchemeRegistry);
    client.setup();
    client.restart();
    try {
      client.execute(request); // used to crash, https://github.com/twitter/hbc/issues/113
    } catch (UnknownHostException e) {
      // expected
    }
  }
}
