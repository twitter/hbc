package com.twitter.hbc.httpclient.auth;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;

import java.nio.charset.Charset;

/**
 * Created by paulrizzo on 8/25/16.
 */
public class HeaderAuth implements Authentication {

  private final String username;
  private final String password;

  public HeaderAuth(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public void setupConnection(AbstractHttpClient client) {
    //noop
  }

  @Override
  public void signRequest(HttpUriRequest request, String postContent) {
    String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
    String authHeader = "Basic " + new String(encodedAuth);
    request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
  }
}
