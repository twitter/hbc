package com.twitter.hbc.core.endpoint;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.twitter.hbc.core.HttpConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class EnterpriseStreamingEndpoint implements StreamingEndpoint {
  protected static final String BASE_PATH = "/accounts/%s/publishers/twitter/streams/track/%s.json";
  protected final String account;
  protected final String label;
  protected final ConcurrentMap<String, String> queryParameters;

  public EnterpriseStreamingEndpoint(String account, String label) {
    this(account, label, null);
  }

  public EnterpriseStreamingEndpoint(String account, String label, String clientId) {
    this.account = Preconditions.checkNotNull(account);
    this.label = Preconditions.checkNotNull(label);

    this.queryParameters = Maps.newConcurrentMap();
    if (clientId != null) {
      addQueryParameter("client", clientId);
    }
  }

  @Override
  public String getURI() {
    String uri = String.format(BASE_PATH, account.trim(), label.trim());

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

  // These don't do anything
  @Override
  public void setBackfillCount(int count) {
  }

  @Override
  public void setApiVersion(String apiVersion) {
  }

  @Override
  public void addPostParameter(String param, String value) {
  }

  @Override
  public void removePostParameter(String param) {
  }

}
