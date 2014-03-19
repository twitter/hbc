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
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DelimitedStreamReaderTest {

  @Test
  public void testReadlineWithSmallBuffer() throws Exception {
    String myMessage = "{this is my message}\r\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() / 3);

    String msg = r.readLine();
    assertEquals(msg, myMessage.trim());
  }

  @Test
  public void testReadlineWithBigBuffer() throws Exception {
    String myMessage = "{this is my message}\r\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() * 3);

    String msg = r.readLine();
    assertEquals(msg, myMessage.trim());
  }

  @Test
  public void testReadlineMultipleSmallBuffer() throws Exception {
    String myMessage = "{msg1}\r\n";
    String myMessage2 = "{this is my message}\n";
    String myMessage3 = "{this is my message2}\r\n";
    byte[] bytes = (myMessage + myMessage2 + myMessage3).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length());

    String msg1 = r.readLine();
    String msg2 = r.readLine();
    String msg3 = r.readLine();

    assertEquals(msg1, myMessage.trim());
    assertEquals(msg2, myMessage2.trim());
    assertEquals(msg3, myMessage3.trim());
  }

  @Test
  public void testReadlineMultipleHugeBuffer() throws Exception {
    String myMessage = "{msg1}\r\n";
    String myMessage2 = "{this is my message}\r\n";
    byte[] bytes = (myMessage + myMessage2).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() * 10 );

    String msg1 = r.readLine();
    String msg2 = r.readLine();

    assertEquals(msg1, myMessage.trim());
    assertEquals(msg2, myMessage2.trim());
  }

  @Test
  public void testEmptyReadline() throws Exception {
    InputStream stream = mock(InputStream.class);

    when(stream.read(any(byte[].class), anyInt(), anyInt()))
            .thenReturn(-1);

    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, 10);
    try {
      r.readLine();
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
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length());

    try {
      r.readLine();
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
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() / 3);

    String msg = r.read(myMessage.length());
    assertEquals(msg, myMessage);
  }

  @Test
  public void testReadWithBigBuffer() throws Exception {
    String myMessage = "{this is my message}\r\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() * 3);

    String msg = r.read(myMessage.length());
    assertEquals(msg, myMessage);
  }

  @Test
  public void testReadMultipleSmallBuffer() throws Exception {
    String myMessage = "{msg1}\r\n";
    String myMessage2 = "{this is my message}\r\n";
    byte[] bytes = (myMessage + myMessage2).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() - 1);

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
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() * 10);

    String msg1 = r.read(myMessage.length());
    String msg2 = r.read(myMessage2.length());

    assertEquals(msg1, myMessage);
    assertEquals(msg2, myMessage2);
  }

  @Test
  public void testEmptyRead() throws Exception {
    InputStream stream = mock(InputStream.class);

    when(stream.read(any(byte[].class), anyInt(), anyInt()))
            .thenReturn(-1);

    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, 10);
    try {
      r.read(10);
      fail();
    } catch (IOException e) {
      // expected
    }
  }

  @Test
  public void testReadRemainder() throws Exception {
    String myMessage = "{this is my message}\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() / 3);


    assertTrue(stream.read(new byte[myMessage.length()/2], 0, myMessage.length()/2) > 0);

    String remainder = r.read(myMessage.length() - myMessage.length()/2);
    assertEquals(remainder, myMessage.substring(myMessage.length()/2, myMessage.length()));
  }

  @Test
  public void testIncompleteRead() {
    String myMessage = "{this is my message}";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length());

    try {
      r.read(myMessage.length() * 2);
      fail();
    } catch (IOException e) {
      // expected
    }
  }

  @Test
  public void testLenientRead() throws Exception {
    String myMessage = "{this is my message}\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() / 3);


    // read less bytes than the actual message, but we're lenient so we'll read up to the newline
    String msg = r.read(myMessage.length()/2);

    assertEquals(msg, myMessage);
  }

  @Test
  public void testCombo() throws Exception {
    String myMessage = "{this is my message}\n";
    int length = myMessage.length();
    byte[] bytes = (length + "\n" + myMessage).getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() / 3);

    // read less bytes than the actual message, but we're lenient so we'll read up to the newline
    String line = r.readLine();
    String msg = r.read(Integer.parseInt(line));
    assertEquals(msg, myMessage);
  }

  @Test
  public void testMultibyteCharacters() throws Exception {
    String myMessage = "{this is my message: héÿ}\n";
    byte[] bytes = myMessage.getBytes(Charsets.UTF_8);

    InputStream stream = new ByteArrayInputStream(bytes);
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() / 3);

    // read less bytes than the actual message, but we're lenient so we'll read up to the newline
    String msg = r.read(myMessage.length());
    assertEquals(msg, myMessage);
  }

  /**
   * This tests the case where we have to call multiple Inputstream.read()s to consume the entire message, as we might
   * have to in real life
   */
  @Test
  public void testMultipleStreamReads() throws Exception {
    String myMessage = "{this is my message: héÿ}\n";
    byte[] bytes = myMessage.substring(0, myMessage.length()/2).getBytes(Charsets.UTF_8);
    byte[] bytes2 = myMessage.substring(myMessage.length()/2, myMessage.length()).getBytes(Charsets.UTF_8);

    InputStream miniStream = new ByteArrayInputStream(bytes);
    InputStream miniStream2 = new ByteArrayInputStream(bytes2);

    InputStream stream = new SplitInputStream(Lists.newArrayList(miniStream, miniStream2));
    DelimitedStreamReader r = new DelimitedStreamReader(stream, Charsets.UTF_8, myMessage.length() / 3);

    // read less bytes than the actual message, but we're lenient so we'll read up to the newline
    String msg = r.read(myMessage.length());
    assertEquals(msg, myMessage);
  }
}
