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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
    RealTimeEnterpriseStreamingEndpoint endpoint = new RealTimeEnterpriseStreamingEndpoint("account_name", "track", "stream_label");
    String expected = "/accounts/account_name/publishers/twitter/streams/track/stream_label.json";
    assertEquals(endpoint.getURI(), expected);
  }

  @Test
  public void testEnterpriseStreamingEndpointProduct() {
    String account = "account_name";
    String label = "test_label";
    String powerTrackProduct = "track";
    String decaHoseProduct = "decahose";
    String powerTrackURI = "/accounts/account_name/publishers/twitter/streams/track/test_label.json";
    String decaHoseProductURI = "/accounts/account_name/publishers/twitter/streams/decahose/test_label.json";

    RealTimeEnterpriseStreamingEndpoint trackEndpoint = new RealTimeEnterpriseStreamingEndpoint(account, powerTrackProduct, label);
    RealTimeEnterpriseStreamingEndpoint decaHoseEndpoint = new RealTimeEnterpriseStreamingEndpoint(account, decaHoseProduct, label);

    assertEquals(powerTrackURI, trackEndpoint.getURI());
    assertEquals(decaHoseProductURI, decaHoseEndpoint.getURI());
  }

  @Test
  public void testEnterpriseReplayStreamingEndpointFormatsDateParamsAndIncludesThem() {
    String expectedBaseUri = "/accounts/account_name/publishers/twitter/replay/track/stream_label.json";
    String expectedFormat = "201401020304";

    Date fromDate = new GregorianCalendar(2014, 0, 02, 03, 04).getTime(); // Months are 0 indexed
    Date toDate = new GregorianCalendar(2015, 1, 03, 04, 05).getTime(); // Months are 0 indexed

    ReplayEnterpriseStreamingEndpoint endpoint = new ReplayEnterpriseStreamingEndpoint("account_name", "track", "stream_label", fromDate, toDate);
    String uri = endpoint.getURI();

    assertTrue(uri.startsWith(expectedBaseUri));
    assertTrue(uri.contains(expectedFormat));
    assertTrue(endpoint.getURI().matches(".+fromDate=[0-9]+.+"));
    assertTrue(endpoint.getURI().matches(".+toDate=[0-9]+.+"));
  }

  @Test
  public void testBackfillParamOnEnterpriseStreamEndpoint() {
    RealTimeEnterpriseStreamingEndpoint endpoint = new RealTimeEnterpriseStreamingEndpoint("account_name", "stream_label", "track", 1);
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
