/**
 * Copyright 2014 Twitter, Inc.
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

import com.google.common.base.Preconditions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReplayEnterpriseStreamingEndpoint extends EnterpriseStreamingEndpoint {
  private static final String DATE_FMT_STR = "yyyyMMddHHmm";
  private static final String BASE_PATH = "/accounts/%s/publishers/twitter/replay/%s/%s.json";
  private final Date fromDate;
  private final Date toDate;

  public ReplayEnterpriseStreamingEndpoint(String account, String product, String label, Date fromDate, Date toDate) {
    super(account, product, label);
    this.fromDate = Preconditions.checkNotNull(fromDate);
    this.toDate = Preconditions.checkNotNull(toDate);
  }

  @Override
  public String getURI() {
    String uri = String.format(BASE_PATH, account.trim(), product.trim(), label.trim());

    addQueryParameter("fromDate", formatDate(this.fromDate));
    addQueryParameter("toDate", formatDate(this.toDate));

    return uri + "?" + generateParamString(queryParameters);
  }

  public Date getFromDate() {
    return fromDate;
  }

  public Date getToDate() {
    return toDate;
  }

  private String formatDate(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FMT_STR);
    return dateFormat.format(date);
  }

}
