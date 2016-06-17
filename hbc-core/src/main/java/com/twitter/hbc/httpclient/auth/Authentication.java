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

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;

public interface Authentication {

  void setupConnection(AbstractHttpClient client);
  void signRequest(HttpUriRequest request, String postContent);
  
  //Adding these here is not ideal, since this interface is implemented by both BasicAuth and OAuth1 classes, and OAuth1 has no use for username/password.
  //This interface is small, and could be eliminated.  
  String getUsername();
  String getPassword();
  
}
