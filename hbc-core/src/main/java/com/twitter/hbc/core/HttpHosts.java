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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HttpHosts implements Hosts {

  private final Iterator<String> hosts;

  public HttpHosts(String address) {
    this(Collections.singletonList(address));
  }

  /**
   * All httpHosts must start with the http scheme("http:// or https://")
   */
  public HttpHosts(Iterable<String> addresses) {
    Preconditions.checkNotNull(addresses);
    Preconditions.checkArgument(!Iterables.isEmpty(addresses));
    for (String address : addresses) {
      if (!address.toLowerCase().startsWith(HttpConstants.HTTP_SCHEME + "://") &&
          !address.toLowerCase().startsWith(HttpConstants.HTTPS_SCHEME + "://")) {
        throw new IllegalArgumentException("Address doesn't have an http scheme: " + address);
      }
    }
    List<String> copy = Lists.newArrayList(addresses);
    Collections.shuffle(copy);
    this.hosts = Iterators.cycle(copy);
  }

  @Override
  public String nextHost() {
    return hosts.next();
  }

  public static final HttpHosts STREAM_HOST = new HttpHosts("https://stream.twitter.com");
  public static final HttpHosts USERSTREAM_HOST = new HttpHosts("https://userstream.twitter.com");
  public static final HttpHosts SITESTREAM_HOST = new HttpHosts("https://sitestream.twitter.com");
}
