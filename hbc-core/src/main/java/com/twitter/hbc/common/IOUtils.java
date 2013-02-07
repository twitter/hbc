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

public class IOUtils {
  /**
   * Reads enough chars from the reader to fill the remainder of the buffer (reads length - offset chars).
   * @throws IOException if an I/O error occurs, or if the stream ends before filling the entire buffer.
   */
  public static void readFully(Reader reader, char[] buffer, int offset) throws IOException {
    int originalOffset = offset;
    int expected = buffer.length - offset;
    while (offset < buffer.length) {
      int numRead = reader.read(buffer, offset, buffer.length - offset);
      if (numRead < 0) {
        throw new IOException(
                String.format("Reached end of stream earlier than expected. Expected to read %d bytes. Actual: %d",
                expected, offset - originalOffset));
      }
      offset += numRead;
    }
  }
}
