package com.twitter.hbc.core.endpoint;

import com.twitter.hbc.core.HttpConstants;

public class EnterpriseStreamingEndpoint implements StreamingEndpoint {
  protected static final String BASE_PATH = "/accounts/%s/publishers/twitter/streams/track/%s.json";
  protected final String account;
  protected final String label;

  public EnterpriseStreamingEndpoint(String account, String label) {
    this.account = account;
    this.label = label;
  }

  @Override
  public String getURI() {
    return String.format(BASE_PATH, account.trim(), label.trim());
  }

  @Override
  public String getHttpMethod() {
    return HttpConstants.HTTP_GET;
  }

  @Override
  public String getPostParamString() {
    return null;
  }

  // These don't do anything
  @Override
  public void setBackfillCount(int count) {}

  @Override
  public void setApiVersion(String apiVersion) {}

  @Override
  public void addPostParameter(String param, String value) {}

  @Override
  public void removePostParameter(String param) {}
}
