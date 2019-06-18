/**
 * Copyright 2016 Twitter, Inc.
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

package com.twitter.hbc.core.endpoint.v2;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.endpoint.Endpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract enterprise streaming endpoint class used for consuming GNIP's v2.0 streaming API's
 */
public abstract class EnterpriseStreamingEndpoint_v2 implements Endpoint {
  private static final String BASE_PATH = "/stream/%s/accounts/%s/publishers/%s/%s.json";
  protected final String account;
  protected final String publisher;
  protected final String product;
  protected final String label;
  protected final ConcurrentMap<String, String> queryParameters = Maps.newConcurrentMap();

  public EnterpriseStreamingEndpoint_v2(String account, String product, String label) {
      this(account, "twitter", product, label);
  }

  public EnterpriseStreamingEndpoint_v2(String account, String publisher, String product, String label) {
    this.product = Preconditions.checkNotNull(product);
    this.account = Preconditions.checkNotNull(account);
    this.publisher = Preconditions.checkNotNull(publisher);
    this.label = Preconditions.checkNotNull(label);
  }

  @Override
  public String getURI() {
    String uri = String.format(BASE_PATH, product.trim(), account.trim(), publisher.trim(), label.trim());

    if (queryParameters.isEmpty()) {
      return uri;
    } else {
      return uri + "?" + generateParamString(queryParameters);
    }
  }

  protected String generateParamString(Map<String, String> params) {
    return Joiner.on("&")
            .withKeyValueSeparator("=")
            .join(params);
  }

  @Override
  public String getHttpMethod() {
    return HttpConstants.HTTP_GET;
  }

  @Override
  public String getPostParamString() {
    return null;
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

  @Override
  public void addPostParameter(String param, String value) { }

  @Override
  public void removePostParameter(String param) { }

}
