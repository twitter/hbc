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

import com.twitter.hbc.core.HttpHosts;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class HttpHostsTest {

  @Test
  public void testExceptionIfBadScheme() {
    int size = 50;
    List<String> hosts = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      if (i == size / 2) {
        hosts.add("thisfails.com");
      } else {
        hosts.add("https://thisismyawesomehost " + i + ".com");
      }
    }
    try {
      new HttpHosts(hosts);
      fail();
    } catch (RuntimeException e) {
      // is expected
    }
  }

  @Test
  public void testIsScrambled() {
    int size = 50;
    List<String> hosts = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      hosts.add("http://thisismyawesomehost " + i + ".com");
    }
    HttpHosts httpHosts = new HttpHosts(hosts);

    boolean allSame = true;
    for (String string : hosts) {
      if (!string.equals(httpHosts.nextHost())) {
        allSame = false;
        break;
      }
    }
    assertFalse("This test failed unless you got EXTREMELY unlucky.", allSame);
  }

  @Test
  public void testContainsAll() {
    int size = 30;
    List<String> hosts = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      hosts.add("http://thisismyawesomehost " + i + ".com");
    }
    HttpHosts httpHosts = new HttpHosts(hosts);
    Set<String> hostsSet = new HashSet<String>(hosts);
    for (int i = 0; i < size; i++) {
      assertTrue(hostsSet.remove(httpHosts.nextHost()));
    }
    assertTrue(hostsSet.isEmpty());
  }

  @Test
  public void testInfiniteIteration() {
    int size = 10;
    List<String> hosts = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      hosts.add("http://thisismyawesomehost " + i + ".com");
    }
    HttpHosts httpHosts = new HttpHosts(hosts);
    Set<String> hostsSet = new HashSet<String>(hosts);
    for (int i = 0; i < size * 10; i++) {
      assertTrue(hostsSet.contains(httpHosts.nextHost()));
    }
  }
}
