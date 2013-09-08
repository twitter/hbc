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

package com.twitter.hbc.core;

import com.google.common.collect.ImmutableSet;
import com.twitter.hbc.core.endpoint.Endpoint;
import com.twitter.hbc.httpclient.auth.Authentication;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import java.util.Set;

public class HttpConstants {
  public static final String HTTP_GET = "GET";
  public static final String HTTP_POST = "POST";

  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";

  public static final int DEFAULT_HTTP_PORT = 80;
  public static final int DEFAULT_HTTPS_PORT = 443;

  public class Codes {
    public static final int SUCCESS = 200;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int UNKNOWN = 404;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PARAMETER_LIST_TOO_LONG = 413;
    public static final int RANGE_UNACCEPTABLE = 416;
    public static final int RATE_LIMITED = 420;
    public static final int SERVICE_UNAVAILABLE = 503;
  }

  public static final Set<Integer> FATAL_CODES = ImmutableSet.of(
          Codes.UNKNOWN,
          Codes.NOT_ACCEPTABLE,
          Codes.PARAMETER_LIST_TOO_LONG,
          Codes.RANGE_UNACCEPTABLE
  );

  public static boolean checkHttpMethod(String httpMethod) {
    if (httpMethod.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
      return true;
    } else if (httpMethod.equalsIgnoreCase(HttpPost.METHOD_NAME) ) {
      return true;
    }
    return false;
  }

  public static boolean isValidHttpScheme(String address) {
    return address.toLowerCase().equalsIgnoreCase(HttpConstants.HTTP_SCHEME) ||
           address.toLowerCase().equalsIgnoreCase(HttpConstants.HTTPS_SCHEME);
  }

  public static HttpUriRequest constructRequest(String host, Endpoint endpoint, Authentication auth) {
    String url = host + endpoint.getURI();
    if (endpoint.getHttpMethod().equalsIgnoreCase(HttpGet.METHOD_NAME)) {
      HttpGet get = new HttpGet(url);
      if (auth != null)
        auth.signRequest(get, null);
      return get;
    } else if (endpoint.getHttpMethod().equalsIgnoreCase(HttpPost.METHOD_NAME) ) {
      HttpPost post = new HttpPost(url);

      post.setEntity(new StringEntity(endpoint.getPostParamString(), Constants.DEFAULT_CHARSET));
      post.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
      if (auth != null)
        auth.signRequest(post, endpoint.getPostParamString());

      return post;
    } else {
      throw new IllegalArgumentException("Bad http method: " + endpoint.getHttpMethod());
    }
  }
}
