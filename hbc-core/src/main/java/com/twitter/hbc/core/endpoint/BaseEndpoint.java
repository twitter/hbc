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
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.joauth.UrlCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BaseEndpoint implements Endpoint {

  protected final ConcurrentMap<String, String> queryParameters;
  protected final ConcurrentMap<String, String> postParameters;

  protected final String path;
  protected final String httpMethod;

  protected volatile String apiVersion;

  public BaseEndpoint(String path, String httpMethod) {
    this.path = Preconditions.checkNotNull(path);
    this.httpMethod = Preconditions.checkNotNull(httpMethod);

    Preconditions.checkArgument(HttpConstants.checkHttpMethod(httpMethod));

    this.queryParameters = new ConcurrentHashMap<String, String>();
    this.postParameters = new ConcurrentHashMap<String, String>();
    this.apiVersion = Constants.CURRENT_API_VERSION;
  }

  public String getPath(String apiVersion) {
    return "/" + apiVersion + this.path;
  }

  public final String getPath() {
    return getPath(apiVersion);
  }

  @Override
  public String getURI() {
    addDefaultParams();
    if (queryParameters.isEmpty()) {
      return getPath();
    } else {
      return getPath() + "?" + generateParamString(queryParameters);
    }
  }

  protected void addDefaultParams() {}

  protected String generateParamString(Map<String, String> params) {
    return Joiner.on("&")
            .withKeyValueSeparator("=")
            .join(params);
  }

  @Override
  public String getQueryParamString() {
    return generateParamString(queryParameters);
  }

  @Override
  public String getPostParamString() {
    return generateParamString(postParameters);
  }

  @Override
  public String getHttpMethod() {
    return httpMethod;
  }

  @Override
  public void addPostParameter(String param, String value) {
    postParameters.put(UrlCodec.encode(param), UrlCodec.encode(value));
  }

  @Override
  public void removePostParameter(String param) {
    postParameters.remove(UrlCodec.encode(param));
  }

  @Override
  public void addQueryParameter(String param, String value) {
    queryParameters.put(UrlCodec.encode(param), UrlCodec.encode(value));
  }

  @Override
  public void removeQueryParameter(String param) {
    queryParameters.remove(UrlCodec.encode(param));
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = Preconditions.checkNotNull(apiVersion);
  }
}
