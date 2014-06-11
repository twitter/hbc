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

package com.twitter.hbc;

import com.google.common.collect.Lists;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpConstants;
import com.twitter.hbc.core.endpoint.*;
import com.twitter.joauth.UrlCodec;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

import static junit.framework.Assert.*;

public class EndpointTest {

  @Test
  public void testDefaultToCurrentApiVersion() throws InterruptedException {
    StreamingEndpoint endpoint = new DefaultStreamingEndpoint("/path", HttpConstants.HTTP_GET, false);
    // "/<api-version>/path"
    assertEquals(endpoint.getURI().split("/")[1], Constants.CURRENT_API_VERSION);
  }

  @Test
  public void testCanSetApiVersion() {
    StreamingEndpoint endpoint = new DefaultStreamingEndpoint("/path", HttpConstants.HTTP_GET, false);
    endpoint.setApiVersion("2");
    assertEquals(endpoint.getURI().split("/")[1], "2");
  }

  @Test
  public void testDefaultParams() throws MalformedURLException {
    StreamingEndpoint endpoint = new DefaultStreamingEndpoint("/path", HttpConstants.HTTP_GET, false);
    URL url = new URL(Constants.STREAM_HOST + endpoint.getURI());
    assertTrue(url.getQuery().contains(Constants.DELIMITED_PARAM + "=" + Constants.DELIMITED_VALUE));
    assertTrue(url.getQuery().contains(Constants.STALL_WARNING_PARAM + "=" + Constants.STALL_WARNING_VALUE));
  }

  @Test
  public void testChangeDefaultParamValues() throws MalformedURLException {
    DefaultStreamingEndpoint endpoint = new DefaultStreamingEndpoint("/path", HttpConstants.HTTP_GET, false);
    endpoint.delimited(false);
    URL url = new URL(Constants.STREAM_HOST + endpoint.getURI());
    assertFalse(url.getQuery().contains(Constants.DELIMITED_PARAM));
    assertTrue(url.getQuery().contains(Constants.STALL_WARNING_PARAM + "=" + Constants.STALL_WARNING_VALUE));

    endpoint.stallWarnings(false);
    url = new URL(Constants.STREAM_HOST + endpoint.getURI());
    assertNull(url.getQuery());
  }

  @Test
  public void testStatusesFilterEndpointTest() {
    StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
    endpoint.locations(Lists.newArrayList(
            new Location(new Location.Coordinate(-122.75, 36.8), new Location.Coordinate(-121.75, 37.8))));
    assertEquals(endpoint.getPostParamString(), "locations=" + UrlCodec.encode("-122.75,36.8,-121.75,37.8"));

    StatusesFilterEndpoint endpoint2 = new StatusesFilterEndpoint();
    endpoint2.trackTerms(Lists.newArrayList(
            "twitterapi", "#!@?"));
    assertEquals(endpoint2.getPostParamString(), "track=" + UrlCodec.encode("twitterapi,#!@?"));
  }

  @Test
  public void testEnterpriseStreamingEndpoint() {
    EnterpriseStreamingEndpoint endpoint = new EnterpriseStreamingEndpoint("account", "track", "label");
    String expected = "/accounts/account/publishers/twitter/streams/track/label.json";
    assertEquals(endpoint.getURI(), expected);
  }

  @Test
  public void testEnterpriseReplayStreamEndpointAddsDateParams() throws ParseException {
    GregorianCalendar calendar = new GregorianCalendar();
    Date toDate = new Date();

    calendar.setTime(toDate);
    calendar.add(Calendar.MONTH, -1);
    Date fromDate = calendar.getTime();

    EnterpriseReplayStreamingEndpoint endpoint = new EnterpriseReplayStreamingEndpoint("account", "track", "label", fromDate, toDate);

    assertTrue(endpoint.getURI().matches(".+fromDate=[0-9]+.+"));
    assertTrue(endpoint.getURI().matches(".+toDate=[0-9]+.+"));
  }

  @Test
  public void testEnterpriseReplayStreamingEndpointFormatsDateParams() {
    String expectedFormat = "201406102102";
    Calendar calendar = new GregorianCalendar(2014, 5, 10, 21, 02); // Months are 0 indexed
    Date fromDate = calendar.getTime();
    Date toDate = new Date();

    EnterpriseReplayStreamingEndpoint endpoint = new EnterpriseReplayStreamingEndpoint("account", "track", "label", fromDate, toDate);
    String uri = endpoint.getURI();
    assertTrue(uri.contains(expectedFormat));
  }

  @Test
  public void testBackfillParamOnEnterpriseStreamEndpoint() {
    EnterpriseStreamingEndpoint endpoint = new EnterpriseStreamingEndpoint("account", "label", "track", 1);
    assertTrue("Endpoint should contain clientId", endpoint.getURI().contains("client=1"));
  }

  @Test
  public void testLanguages() {
    DefaultStreamingEndpoint endpoint = new StatusesFilterEndpoint();
    endpoint.languages(Lists.newArrayList("en", "de"));
    assertEquals(endpoint.getPostParamString(), "language=" + UrlCodec.encode("en,de"));
  }

  @Test
  public void testFilterLevel() {
    DefaultStreamingEndpoint endpoint = new StatusesFilterEndpoint();
    endpoint.filterLevel(Constants.FilterLevel.Medium);
    assertEquals(endpoint.getPostParamString(), "filter_level=medium");
  }

  @Test
  public void testSiteStreamEndpoint() {
    List<Long> followings = new ArrayList<Long>();
    followings.add(111111111L);
    followings.add(222222222L);

    SitestreamEndpoint endpoint = new SitestreamEndpoint(followings);

    assertEquals(Constants.FOLLOW_PARAM + "=111111111" + UrlCodec.encode(",") + "222222222", endpoint.getQueryParamString());
  }
}
