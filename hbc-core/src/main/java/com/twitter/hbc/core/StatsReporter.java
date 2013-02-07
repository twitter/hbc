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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatsReporter {

  private final AtomicLong numMessages;

  private final AtomicLong numMessagesDropped;
  private final AtomicLong numClientEventsDropped;

  private final AtomicInteger numDisconnects;
  private final AtomicInteger numConnects;
  private final AtomicInteger numConnectionFailures;

  private final AtomicInteger num500s;
  private final AtomicInteger num400s;
  private final AtomicInteger num200s;

  public StatsReporter() {
    numMessages = new AtomicLong(0);
    numDisconnects = new AtomicInteger(0);
    numConnectionFailures = new AtomicInteger(0);
    numConnects = new AtomicInteger(0);
    num500s = new AtomicInteger(0);
    num400s = new AtomicInteger(0);
    num200s = new AtomicInteger(0);
    numClientEventsDropped = new AtomicLong(0);
    numMessagesDropped = new AtomicLong(0);
  }

  public int incrNum200s() {
    return num200s.incrementAndGet();
  }

  public int incrNum400s() {
    return num400s.incrementAndGet();
  }

  public int incrNum500s() {
    return num500s.incrementAndGet();
  }

  public long incrNumMessages() {
    return numMessages.incrementAndGet();
  }

  public int incrNumDisconnects() {
    return numDisconnects.incrementAndGet();
  }

  public int incrNumConnects() {
    return numConnects.incrementAndGet();
  }

  public int incrNumConnectionFailures() {
    return numConnectionFailures.incrementAndGet();
  }

  public long incrNumClientEventsDropped() {
    return numClientEventsDropped.incrementAndGet();
  }

  public long incrNumMessagesDropped() {
    return numMessagesDropped.incrementAndGet();
  }

  public StatsTracker getStatsTracker() {
    return new StatsTracker();
  }

  public class StatsTracker {

    /**
     * @return number of 200 http response codes in the lifetime of this client
     */
    public int getNum200s() {
      return num200s.get();
    }

    /**
     * @return number of 4xx http response codes in the lifetime of this client
     */
    public int getNum400s() {
      return num400s.get();
    }

    /**
     * @return number of 5xx http response codes in the lifetime of this client
     */
    public int getNum500s() {
      return num500s.get();
    }

    /**
     * @return number of messages the client has processed
     */
    public long getNumMessages() {
      return numMessages.get();
    }

    /**
     * @return number of disconnects
     */
    public int getNumDisconnects() {
      return numDisconnects.get();
    }

    /**
     * @return number of connections/reconnections
     */
    public int getNumConnects() {
      return numConnects.get();
    }

    /**
     * @return number of connection failures. This includes 4xxs, 5xxs, bad host names, etc
     */
    public int getNumConnectionFailures() {
      return numConnectionFailures.get();
    }

    /**
     * @return number of events dropped from the event queue
     */
    public long getNumClientEventsDropped() {
      return numClientEventsDropped.get();
    }

    /**
     * @return number of messages dropped from the message queue. Occurs when messages in the message queue aren't
     * dequeued fast enough
     */
    public long getNumMessagesDropped() {
      return numMessagesDropped.get();
    }
  }
}
