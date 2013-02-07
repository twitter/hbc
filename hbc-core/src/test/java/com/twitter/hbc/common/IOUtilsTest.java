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

package com.twitter.hbc.common;

import com.google.common.base.Charsets;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IOUtilsTest {

  @Test
  public void testReadFully() throws Exception {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    Reader reader = new InputStreamReader(stream);

    char[] buffer = new char[myMessage.length()];
    IOUtils.readFully(reader, buffer, 0);

    for (int i = 0; i < myMessage.length(); i++) {
      assertEquals(buffer[i], myMessage.charAt(i));
    }
  }

  @Test
  public void testReadFullyPartial() throws Exception {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    Reader reader = new InputStreamReader(stream);

    char[] partialBuffer = new char[myMessage.length()/2];
    IOUtils.readFully(reader, partialBuffer, 0);
    for (int i = 0; i < partialBuffer.length; i++) {
      assertEquals(partialBuffer[i], myMessage.charAt(i));
    }
  }

  @Test
  public void testEmptyReadFully() throws Exception {
    Reader reader = mock(Reader.class);

    when(reader.read(any(char[].class), anyInt(), anyInt()))
            .thenReturn(-1);

    char[] buffer = new char[10];

    try {
      IOUtils.readFully(reader, buffer, 0);
      fail();
    } catch (IOException e) {
      // expected
    }
  }

  @Test
  public void testReadFullyRemainder() throws Exception {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    Reader reader = new InputStreamReader(stream);

    char[] buffer = new char[myMessage.length()];
    IOUtils.readFully(reader, buffer, myMessage.length()/2);

    for (int i = 0; i < myMessage.length(); i++) {
      if (i < myMessage.length()/2) {
        assertEquals(buffer[i], 0);
      } else {
        assertEquals(buffer[i], myMessage.charAt(i - myMessage.length()/2));
      }
    }
  }

  @Test
  public void testIncompleteReadFully() {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    Reader reader = new InputStreamReader(stream);

    char[] buffer = new char[myMessage.length() * 2];

    try {
      IOUtils.readFully(reader, buffer, 0);
      fail();
    } catch (IOException e) {
      // expected
    }

    // reset the stream
    stream = new ByteArrayInputStream(bytes);
    reader = new InputStreamReader(stream);
    try {
      IOUtils.readFully(reader, buffer, myMessage.length()/2);
      fail();
    } catch (IOException e) {
      // expected
    }
  }
}