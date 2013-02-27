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

package com.twitter.hbc.core.processor;

import com.twitter.hbc.common.IOUtils;
import com.twitter.hbc.core.Constants;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

public class StringDelimitedProcessor extends AbstractProcessor<String> {

  private BufferedReader br;
  private final static int DEFAULT_BUFFER_SIZE = 50000;
  private final static int MAX_ALLOWABLE_BUFFER_SIZE = 500000;

  private final static String EMPTY_LINE = "";

  public StringDelimitedProcessor(BlockingQueue<String> queue) {
    super(queue);
  }

  public StringDelimitedProcessor(BlockingQueue<String> queue, long offerTimeoutMillis) {
    super(queue, offerTimeoutMillis);
  }

  @Override
  public void setup(InputStream input) {
    br = new BufferedReader(new InputStreamReader(input, Constants.DEFAULT_CHARSET), DEFAULT_BUFFER_SIZE);
  }

  @Override @Nullable
  protected String processNextMessage() throws IOException {
    // TODO: read raw bytes ourselves
    String line = br.readLine();
    if (line == null) {
      throw new IOException("Unable to read new line from stream");
    } else if (line.equals(EMPTY_LINE)) {
      return null;
    }
    int delimitedCount = Integer.parseInt(line);
    if (delimitedCount > MAX_ALLOWABLE_BUFFER_SIZE) {
      // this is to protect us from nastiness
      throw new IOException("Unreasonable message size " + delimitedCount);
    }
    char[] buffer = new char[delimitedCount];
    IOUtils.readFully(br, buffer, 0);
    return new String(buffer);
  }
}
