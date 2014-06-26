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

package com.twitter.hbc.processor;

import com.twitter.hbc.core.processor.HosebirdMessageProcessor;
import com.twitter.hbc.core.processor.LineStringProcessor;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.test.SimpleStreamProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.*;

public class LineDelimitedStreamProcessorTest {

  private String[] messages;

  @Before
  public void setup() {
    // just an arbitrary set of messages
    messages = new String[100];
    for (int i = 0; i < messages.length; i++) {
      messages[i] = "messages" + i;
    }
  }

  @Test
  public void testLineStringProcessing() throws Exception {
    SimpleStreamProvider simpleStream = new SimpleStreamProvider(messages, false, false);

    BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);
    HosebirdMessageProcessor processor = new LineStringProcessor(queue);

    int count = processStream(simpleStream, processor, queue);

    assertEquals(messages.length, count);

  }

  private int processStream(SimpleStreamProvider simpleStream, HosebirdMessageProcessor processor, BlockingQueue<String> queue) throws InterruptedException {
    int count = 0;
    try {
      InputStream stream = simpleStream.createInputStream();

      processor.setup(stream);
      // read until we hit the IOException
      while (count < messages.length * 2) {
        processor.process();
        // trimming to get rid of the CRLF
        assertTrue(messages[count].equals(queue.take().trim()));
        count++;
      }
      fail();
    } catch (IOException e) {
      // expected
    }
    return count;
  }

  @Test
  public void testIdleProbe() throws Exception {
    SimpleStreamProvider simpleStream = new SimpleStreamProvider(messages, true, true);
    int count = 0;
    try {
      InputStream stream = simpleStream.createInputStream();
      BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);
      HosebirdMessageProcessor processor = new StringDelimitedProcessor(queue);
      processor.setup(stream);
      // read until we hit the IOException
      while (count < messages.length * 2) {
        processor.process();
        // trimming to get rid of the CRLF
        assertTrue(messages[count].equals(queue.take().trim()));
        count++;
      }
      fail();
    } catch (IOException e) {
      // expected
    }
    assertEquals(messages.length, count);
  }
}
