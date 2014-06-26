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

package com.twitter.hbc.core.processor;

import com.google.common.base.Charsets;
import com.twitter.hbc.common.DelimitedStreamReader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

/**
 * Reads new line delimited strings according to {@link com.twitter.hbc.common.DelimitedStreamReader#readLine() }
 * */
public class LineStringProcessor extends AbstractProcessor<String> {
  private final static int DEFAULT_BUFFER_SIZE = 50000;
  private DelimitedStreamReader reader;

  public LineStringProcessor(BlockingQueue<String> queue) {
    super(queue);
  }

  public LineStringProcessor(BlockingQueue<String> queue, long offerTimeoutMillis) {
    super(queue, offerTimeoutMillis);
  }

  @Nullable
  @Override
  protected String processNextMessage() throws IOException {
    String line = reader.readLine();
    if (line == null) {
      throw new IOException("Unable to read new line from stream");
    } else if (line.isEmpty()) {
      return null;
    }
    return line;
  }

  @Override
  public void setup(InputStream input) {
    reader = new DelimitedStreamReader(input, Charsets.UTF_8, DEFAULT_BUFFER_SIZE);
  }
}
