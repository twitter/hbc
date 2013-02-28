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

import java.io.IOException;
import java.io.Reader;

/**
 * A single-threaded character stream reader that buffers minimally. This avoids the problem where low velocity streams
 * would not see messages coming in due to it being buffered by the BufferedReader/InputStreamReader combo.
 */
public class CharacterStreamReader {

  private final Reader reader;

  private final char[] buffer;
  private final StringBuilder sb;

  private int offset;
  private int end; // first invalid byte

  private static int DEFAULT_READ_COUNT = 64;

  public CharacterStreamReader(Reader reader, int bufferSize) {
    this.reader = reader;
    buffer = new char[bufferSize];
    sb = new StringBuilder();
    offset = 0;
    end = 0;
  }

  public String readline() throws IOException {
    String str = null;
    boolean done = false;
    boolean sawCarriage = false;

    while (!done) {
      // detect if we're at the end of our buffer:
      if (offset >= buffer.length || end - offset <= 0) {
        offset = 0;
        end = 0;
        // We don't want DEFAULT_READ_COUNT to be too big: InputStreamReader will block until the amount is read
        readAmount(DEFAULT_READ_COUNT);
      }

      int curIndex = offset;
      for (; !done && curIndex < end; curIndex++) {
        if (buffer[curIndex] == '\n') {
          // this string doesn't include the \n: the actual length with the \n is curIndex - offset + 1
          sb.append(buffer, offset, curIndex - offset);

          // we don't want \r either
          int count = 0;
          if (sawCarriage) {
            count += 1;
          }
          str = sb.substring(0, Math.max(0, sb.length() - count));

          offset = curIndex + 1;
          done = true;
        } else {
          sawCarriage = buffer[curIndex] == '\r';
        }
      }

      if (!done) {
        sb.append(buffer, offset, end - offset);
        offset = end;
      }
    }

    sb.setLength(0);
    return str;
  }

  public String read(int numBytes) throws IOException {
    char[] strBuffer = new char[numBytes];
    int strBufferIndex = 0;
    int numBytesRemaining = numBytes;

    // first read whatever we need from our buffer
    if (end - offset > 0) {
      int length = Math.min(end - offset,numBytesRemaining);
      System.arraycopy(buffer, offset, strBuffer, strBufferIndex, length);

      strBufferIndex += length;
      offset += length;
      numBytesRemaining -= length;
    }

    // next read the remaining chars directly into our strBuffer
    if (numBytesRemaining > 0) {
      IOUtils.readFully(reader, strBuffer, strBufferIndex);
    }

    return new String(strBuffer);
  }

  private void readAmount(int numChars) throws IOException {
    int bytesRead = reader.read(buffer, end, Math.min(numChars, buffer.length - end));
    if (bytesRead < 0) {
      // EOF
      throw new IOException("Reached end of stream.");
    }
    end = end + bytesRead;
  }
}
