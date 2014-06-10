package com.twitter.hbc.core.endpoint;

import com.google.common.base.Preconditions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EnterpriseReplayStreamingEndpoint extends EnterpriseStreamingEndpoint {
  protected Date fromDate;
  protected Date toDate;
  private static final String DATE_FMT_STR = "yyyyMMddHHmm";
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FMT_STR);

  protected static final String BASE_PATH = "/accounts/%s/publishers/twitter/replay/track/%s.json";

  public EnterpriseReplayStreamingEndpoint(String account, String label, Date fromDate, Date toDate) {
    super(account, label);

    this.fromDate        = Preconditions.checkNotNull(fromDate);
    this.toDate          = Preconditions.checkNotNull(toDate);
  }

  @Override
  public String getURI() {
    String uri = String.format(BASE_PATH, this.account.trim(), this.label.trim());
    String queryString;

    String _toDate   = formateDate(this.toDate);
    String _fromDate = formateDate(this.fromDate);
    addQueryParameter("toDate", _toDate);
    addQueryParameter("fromDate", _fromDate);

    queryString = "?" + generateParamString(this.queryParameters);

    return uri + queryString;
  }

  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate;
  }

  public void setToDate(Date toDate) {
    this.toDate = toDate;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public Date getToDate() {
    return toDate;
  }

  private String formateDate(Date date) {
    return DATE_FORMAT.format(date);
  }


}
