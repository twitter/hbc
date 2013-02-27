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

package com.twitter.hbc.test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SimpleStreamProvider implements InputStreamProvider {

  private final List<String> messages;

  private final String CRLF = "\r\n";

  public SimpleStreamProvider(String[] messages, boolean delimited, boolean idleProbes) {
    this.messages = new ArrayList<String>(messages.length);
    for (String message : messages) {
      if (idleProbes) {
        this.messages.add("");
      }
      if (delimited) {
        if (idleProbes) {
          this.messages.add("");  // add some idle probes
          this.messages.add("");
        }
        this.messages.add(delimitedMessage(message));
      } else {
        this.messages.add(message);
      }
    }
  }

  @Override
  public InputStream createInputStream() {
    String stream = Joiner.on(CRLF).join(messages) + CRLF;
    return new ByteArrayInputStream(stream.getBytes(Charsets.UTF_8));
  }

  private String delimitedMessage(String message) {
    return (message.length() + CRLF.length()) + "\n" + message;
  }
}
