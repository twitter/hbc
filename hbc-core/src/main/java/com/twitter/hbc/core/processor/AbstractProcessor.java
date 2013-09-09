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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An abstract class for processing the stream and putting it onto the blockingQueue.
 * This class should probably not be extended externally, unless you want to process messages
 * yourself.
 */
public abstract class AbstractProcessor<T> implements HosebirdMessageProcessor {

  public static final long DEFAULT_OFFER_TIMEOUT_MILLIS = 500;

  protected final BlockingQueue<T> queue;
  protected final long offerTimeoutMillis;

  public AbstractProcessor(BlockingQueue<T> queue) {
    this(queue, DEFAULT_OFFER_TIMEOUT_MILLIS);
  }

  public AbstractProcessor(BlockingQueue<T> queue, long offerTimeoutMillis) {
    this.queue = queue;
    this.offerTimeoutMillis = offerTimeoutMillis;
  }

  @Override
  public boolean process() throws IOException, InterruptedException {
    T msg = processNextMessage();
    while (msg == null) {
      msg = processNextMessage();
    }
    return queue.offer(msg, offerTimeoutMillis, TimeUnit.MILLISECONDS);
  }

  @Nullable
  protected abstract T processNextMessage() throws IOException;
}
