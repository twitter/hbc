package com.twitter.hbc.common;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Test helper that allows us to test multiple calls to read()
 */
public class SplitInputStream extends InputStream {

  private final Iterator<InputStream> streams;
  private InputStream currentStream;

  public SplitInputStream(List<InputStream> streams) {
    this.streams = Lists.newArrayList(streams).iterator();
  }


  @Override
  public int read() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read(byte[] bytes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read(byte[] bytes, int i, int length) throws java.io.IOException {
    if (currentStream == null && !getNextStream()) {
      return -1;
    }
    int bytesRead = currentStream.read(bytes, i, length);
    while (bytesRead < 0) {
      if (getNextStream()) {
        bytesRead = currentStream.read(bytes, i, length);
      } else {
        return -1;
      }
    }
    return bytesRead;
  }

  private boolean getNextStream() {
    if (streams.hasNext()) {
      currentStream = streams.next();
      return true;
    } else {
      return false;
    }
  }
}
