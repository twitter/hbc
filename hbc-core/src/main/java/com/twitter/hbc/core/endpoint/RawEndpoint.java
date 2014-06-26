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

package com.twitter.hbc.core.endpoint;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.joauth.UrlCodec;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A convenience class for users that want to hit an arbitrary endpoint w/o having to implement
 * their own Endpoint
 */
public class RawEndpoint implements StreamingEndpoint {

  private final String uri;
  private final String httpMethod;
  private final ConcurrentMap<String, String> postParams;
  private final ConcurrentMap<String, String> queryParameters;

  /**
   * @param uri should be the full uri, including the starting "/" and the api version, and any query params
   */
  public RawEndpoint(String uri, String httpMethod) {
    this(uri, httpMethod, Collections.<String, String>emptyMap());
  }

  /**
   * @param uri        should be the full uri, including the starting "/" and the api version, and any query params.
   * @param postParams any http POST parameters (not encoded)
   */
  public RawEndpoint(String uri, String httpMethod, Map<String, String> postParams) {
    this.uri = Preconditions.checkNotNull(uri);
    this.httpMethod = Preconditions.checkNotNull(httpMethod);
    Preconditions.checkArgument(HttpConstants.checkHttpMethod(httpMethod));
    Preconditions.checkNotNull(postParams);
    this.postParams = Maps.newConcurrentMap();
    this.queryParameters = Maps.newConcurrentMap();
    postParams.putAll(postParams);
  }

  /**
   * These don't do anything
   */
  @Override
  public void setBackfillCount(int count) { }

  @Override
  public void setApiVersion(String apiVersion) { }

  @Override
  public String getURI() {
    if (queryParameters.isEmpty()) {
      return this.uri;
    } else {
      return this.uri + "?" + generateParamString(queryParameters);
    }
  }

  @Override
  public String getHttpMethod() {
    return this.httpMethod;
  }

  @Override
  public String getPostParamString() {
    return Joiner.on("&")
            .withKeyValueSeparator("=")
            .join(postParams);
  }

  @Override
  public void addPostParameter(String param, String value) {
    postParams.put(UrlCodec.encode(param), UrlCodec.encode(value));
  }

  @Override
  public void removePostParameter(String param) {
    postParams.remove(UrlCodec.encode(param));
  }

  @Override
  public String getQueryParamString() {
    return generateParamString(queryParameters);
  }

  @Override
  public void addQueryParameter(String param, String value) {
    queryParameters.put(param, value);
  }

  @Override
  public void removeQueryParameter(String param) {
    queryParameters.remove(param);
  }

  private String generateParamString(Map<String, String> params) {
    return Joiner.on("&")
            .withKeyValueSeparator("=")
            .join(params);
  }
}
