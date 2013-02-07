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

package com.twitter.hbc.core.event;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

public class Event {

  private final EventType eventType;
  private final String message;
  private final Exception exception;

  public Event(EventType eventType) {
    this(eventType, eventType.toString());
  }

  public Event(EventType eventType, String message) {
    this.eventType = Preconditions.checkNotNull(eventType);
    this.message = Preconditions.checkNotNull(message);
    this.exception = null;
  }

  public Event(EventType eventType, Exception exception) {
    this.eventType = Preconditions.checkNotNull(eventType);
    this.exception = Preconditions.checkNotNull(exception);
    this.message = exception.getMessage();
  }

  public String getMessage() {
    return message;
  }

  public EventType getEventType() {
    return eventType;
  }

  @Nullable
  public Exception getUnderlyingException() {
    return exception;
  }
}
