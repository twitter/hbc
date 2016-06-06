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

package com.twitter.hbc.httpclient.auth;

import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Preconditions;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;

public class BasicAuth implements Authentication {

  private final String username, password;

  public BasicAuth(String username, String password) {
    this.username = Preconditions.checkNotNull(username);
    this.password = Preconditions.checkNotNull(password);
  }

  public void setupConnection(AbstractHttpClient client) {
    client.getCredentialsProvider().setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
    );
  }

  @Override
  public void signRequest(HttpUriRequest request, String postParams) {
    String authToken = username + ":" + password;
    String encoded = DatatypeConverter.printBase64Binary(authToken.getBytes());
    request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
  }
}
