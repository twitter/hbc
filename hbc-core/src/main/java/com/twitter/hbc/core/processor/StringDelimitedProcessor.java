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

import com.twitter.hbc.common.DelimitedStreamReader;
import com.twitter.hbc.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

public class StringDelimitedProcessor extends AbstractProcessor<String> {

  private final static Logger logger = LoggerFactory.getLogger(StringDelimitedProcessor.class);
  private final static int DEFAULT_BUFFER_SIZE = 50000;
  private final static int MAX_ALLOWABLE_BUFFER_SIZE = 500000;
  private final static String EMPTY_LINE = "";

  private DelimitedStreamReader reader;

  public StringDelimitedProcessor(BlockingQueue<String> queue) {
    super(queue);
  }

  public StringDelimitedProcessor(BlockingQueue<String> queue, long offerTimeoutMillis) {
    super(queue, offerTimeoutMillis);
  }

  @Override
  public void setup(InputStream input) {
    reader = new DelimitedStreamReader(input, Constants.DEFAULT_CHARSET, DEFAULT_BUFFER_SIZE);
  }

  @Override @Nullable
  protected String processNextMessage() throws IOException {
    int delimitedCount = -1;
    int retries = 0;
    while (delimitedCount < 0 && retries < 3) {
      String line = reader.readLine();
      if (line == null) {
        throw new IOException("Unable to read new line from stream");
      } else if (line.equals(EMPTY_LINE)) {
        return null;
      }

      try {
        delimitedCount = Integer.parseInt(line);
      } catch (NumberFormatException n) {
        // resilience against the occasional malformed message
        logger.warn("Error parsing delimited length", n);
      }
      retries += 1;
    }

    if (delimitedCount < 0) {
      throw new RuntimeException("Unable to process delimited length");
    }

    if (delimitedCount > MAX_ALLOWABLE_BUFFER_SIZE) {
      // this is to protect us from nastiness
      throw new IOException("Unreasonable message size " + delimitedCount);
    }
    return reader.read(delimitedCount);
  }
}
