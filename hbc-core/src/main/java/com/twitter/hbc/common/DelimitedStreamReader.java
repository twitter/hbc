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
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Only for charsets whose byte representations of \n and \r are 10 and 13 (Ascii compatible encodings)
 */
public class DelimitedStreamReader {

  private final InputStream inputStream;

  private final byte[] buffer;
  private byte[] strBuffer;
  private int strBufferIndex;
  private final Charset charset;

  private int offset;
  private int end; // first invalid byte

  private static int DEFAULT_READ_COUNT = 64;
  private final static int MAX_ALLOWABLE_BUFFER_SIZE = 500000;

  private byte CR = 13;
  private byte LF = 10;

  public DelimitedStreamReader(InputStream stream, Charset charset, int bufferSize) {
    this.inputStream = stream;
    this.charset = charset;

    this.strBuffer = new byte[bufferSize * 2];

    buffer = new byte[bufferSize];
    offset = 0;
    end = 0;
  }

  public String readLine() throws IOException {
    return readLine(true);
  }

  /**
   * Reads a line from the input stream, where a line is terminated by \r, \n, or \r\n
   * @param trim whether to trim trailing \r and \ns
   */
  private String readLine(boolean trim) throws IOException {
    boolean done = false;
    boolean sawCarriage = false;
    // bytes to trim (the \r and the \n)
    int removalBytes = 0;
    while (!done) {
      if (isReadBufferEmpty()) {
        offset = 0;
        end = 0;
        int bytesRead = inputStream.read(buffer, end, Math.min(DEFAULT_READ_COUNT, buffer.length - end));
        if (bytesRead < 0) {
          // we failed to read anything more...
          if (sawCarriage) {
            // if we last saw a carriage return, then just return everything from the last read
            done = true;
            if (trim) {
              // don't want the /r
              removalBytes = -1;
            }
          } else {
            // otherwise we reached the end of the stream with no new line
            throw new IOException("Reached the end of the stream");
          }
        } else {
          end += bytesRead;
        }
      }

      int originalOffset = offset;
      for (; !done && offset < end; offset++) {
        if (buffer[offset] == LF || sawCarriage) {
          int cpLength = offset - originalOffset + 1;
          if (trim) {
            int length = 0;
            if (buffer[offset] == LF) {
              length ++;
              if (sawCarriage) {
                length++;
              }
            } else {
              // We only saw /r and this is not an \n. Need to be careful to "unread" this byte
              offset--;
              length += 2;
            }
            cpLength -= length;
          }

          if (cpLength > 0) {
            copyToStrBuffer(buffer, originalOffset, cpLength);
          } else {
            // negative length means we need to trim a \r from strBuffer
            removalBytes = cpLength;
          }
          done = true;
        } else {
          // did not see newline:
          sawCarriage = buffer[offset] == CR;
        }
      }

      if (!done) {
        copyToStrBuffer(buffer, originalOffset, end - originalOffset);
        offset = end;
      }
    }
    int strLength = strBufferIndex + removalBytes;
    strBufferIndex = 0;
    return new String(strBuffer, 0, strLength, charset);
  }

  /**
   * Copies from buffer to our internal strBufferIndex, expanding the internal buffer if necessary
   * @param offset offset in the buffer to start copying from
   * @param length length to copy
   */
  private void copyToStrBuffer(byte[] buffer, int offset, int length) {
    if (strBuffer.length - strBufferIndex < length) {
      // cannot fit, expanding buffer
      expandStrBuffer(length);
    }
    System.arraycopy(
      buffer, offset, strBuffer, strBufferIndex, Math.min(length, MAX_ALLOWABLE_BUFFER_SIZE - strBufferIndex));
    strBufferIndex += length;
  }

  private void expandStrBuffer(int minLength) {
    byte[] oldBuffer = strBuffer;
    int newLength = Math.min (
      Math.max(oldBuffer.length * 2, minLength),
      MAX_ALLOWABLE_BUFFER_SIZE
    );

    if (newLength > oldBuffer.length) {
      strBuffer = new byte[newLength];
      System.arraycopy(oldBuffer, 0, strBuffer, 0, strBufferIndex);
    }
  }

  /**
   * Reads numBytes bytes, and returns the corresponding string
   * @throws IOException
   */
  public String read(int numBytes) throws IOException {
    assert numBytes < MAX_ALLOWABLE_BUFFER_SIZE;
    int numBytesRemaining = numBytes;
    // first read whatever we need from our buffer
    if (!isReadBufferEmpty()) {
      int length = Math.min(end - offset, numBytesRemaining);
      copyToStrBuffer(buffer, offset, length);
      offset += length;
      numBytesRemaining -= length;
    }

    // next read the remaining chars directly into our strBuffer
    if (numBytesRemaining > 0) {
      readSetAmount(numBytesRemaining);
    }

    if (strBufferIndex > 0 && strBuffer[strBufferIndex - 1] != LF) {
      // the last byte doesn't correspond to lf
      return readLine(false);
    }

    int strBufferLength = strBufferIndex;
    strBufferIndex = 0;
    return new String(strBuffer, 0, strBufferLength, charset);
  }

  private void readSetAmount(int length) throws IOException {
    int actualOffset = strBufferIndex;
    int remainingBytes = length;
    while (remainingBytes > 0) {
      int bytesRead = readIntoStrBuffer(actualOffset, remainingBytes);
      remainingBytes -= bytesRead;
      actualOffset += bytesRead;
    }
    strBufferIndex = actualOffset;
  }

  private int readIntoStrBuffer(int offset, int length) throws IOException {
    if (length > strBuffer.length - offset) {
      expandStrBuffer(strBuffer.length - offset);
    }
    int bytesRead = inputStream.read(strBuffer, strBufferIndex, Math.min(length, strBuffer.length - offset));
    if (bytesRead < 0) {
      throw new IOException("Reached end of stream.");
    }
    return bytesRead;
  }

  private boolean isReadBufferEmpty() {
    return offset >= buffer.length || end - offset <= 0;
  }
}
