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

public class CharacterStreamReaderTest {

  @Test
  public void testReadlineWithSmallBuffer() throws Exception {
    String myMessage = "{this is my message}\r\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() / 3);

    String msg = r.readline();
    assertEquals(msg, myMessage.trim());
  }

  @Test
  public void testReadlineWithBigBuffer() throws Exception {
    String myMessage = "{this is my message}\r\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() * 3);

    String msg = r.readline();
    assertEquals(msg, myMessage.trim());
  }

  @Test
  public void testReadlineMultipleSmallBuffer() throws Exception {
    String myMessage = "{msg1}\r\n";
    String myMessage2 = "{this is my message}\r\n";
    byte[] bytes = (myMessage + myMessage2).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() - 1);

    String msg1 = r.readline();
    String msg2 = r.readline();

    assertEquals(msg1, myMessage.trim());
    assertEquals(msg2, myMessage2.trim());
  }

  @Test
  public void testReadlineMultipleHugeBuffer() throws Exception {
    String myMessage = "{msg1}\r\n";
    String myMessage2 = "{this is my message}\r\n";
    byte[] bytes = (myMessage + myMessage2).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() * 10);

    String msg1 = r.readline();
    String msg2 = r.readline();

    assertEquals(msg1, myMessage.trim());
    assertEquals(msg2, myMessage2.trim());
  }

  @Test
  public void testEmptyReadline() throws Exception {
    InputStreamReader reader = mock(InputStreamReader.class);

    when(reader.read(any(char[].class), anyInt(), anyInt()))
            .thenReturn(-1);

    CharacterStreamReader r = new CharacterStreamReader(reader, 10);
    try {
      r.readline();
      fail();
    } catch (IOException e) {
      // expected
    }
  }

  @Test
  public void testIncompleteReadline() {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);
    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length());

    try {
      r.readline();
      fail();
    } catch (IOException e) {
      // expected
    }
  }


  @Test
  public void testReadWithSmallBuffer() throws Exception {
    String myMessage = "{this is my message}\r\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() / 3);

    String msg = r.read(myMessage.length());
    assertEquals(msg, myMessage);
  }

  @Test
  public void testReadWithBigBuffer() throws Exception {
    String myMessage = "{this is my message}\r\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() * 3);

    String msg = r.read(myMessage.length());
    assertEquals(msg, myMessage);
  }

  @Test
  public void testReadMultipleSmallBuffer() throws Exception {
    String myMessage = "{msg1}\r\n";
    String myMessage2 = "{this is my message}\r\n";
    byte[] bytes = (myMessage + myMessage2).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() - 1);

    String msg1 = r.read(myMessage.length());
    String msg2 = r.read(myMessage2.length());

    assertEquals(msg1, myMessage);
    assertEquals(msg2, myMessage2);
  }

  @Test
  public void testReadMultipleHugeBuffer() throws Exception {
    String myMessage = "{msg1}\r\n";
    String myMessage2 = "{this is my message}\r\n";
    byte[] bytes = (myMessage + myMessage2).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() * 10);

    String msg1 = r.readline();
    String msg2 = r.readline();

    assertEquals(msg1, myMessage.trim());
    assertEquals(msg2, myMessage2.trim());
  }

  @Test
  public void testEmptyRead() throws Exception {
    InputStreamReader reader = mock(InputStreamReader.class);

    when(reader.read(any(char[].class), anyInt(), anyInt()))
            .thenReturn(-1);

    CharacterStreamReader r = new CharacterStreamReader(reader, 10);
    try {
      r.read(10);
      fail();
    } catch (IOException e) {
      // expected
    }
  }

  @Test
  public void testReadRemainder() throws Exception {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);

    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length() / 3);

    String partial = r.read(myMessage.length()/2);
    assertEquals(partial, myMessage.substring(0, myMessage.length()/2));

    String remainder = r.read(myMessage.length() - myMessage.length()/2);
    assertEquals(remainder, myMessage.substring(myMessage.length()/2, myMessage.length()));
  }

  @Test
  public void testIncompleteRead() {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);
    CharacterStreamReader r = new CharacterStreamReader(reader, myMessage.length());

    try {
      r.read(myMessage.length() * 2);
      fail();
    } catch (IOException e) {
      // expected
    }
  }
}
