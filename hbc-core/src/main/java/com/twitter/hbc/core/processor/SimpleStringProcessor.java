package com.twitter.hbc.core.processor;

import com.twitter.hbc.common.DelimitedStreamReader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class SimpleStringProcessor extends AbstractProcessor<String> {
  private final static int DEFAULT_BUFFER_SIZE = 50000;
  private DelimitedStreamReader reader;

  public SimpleStringProcessor(BlockingQueue<String> queue) {
    super(queue);
  }

  public SimpleStringProcessor(BlockingQueue<String> queue, long offerTimeoutMillis) {
    super(queue, offerTimeoutMillis);
  }

  @Nullable
  @Override
  protected String processNextMessage() throws IOException {
    String line = reader.readLine();
    if (line == null) {
      throw new IOException("Unable to read new line from stream");
    } else if (line.isEmpty()) {
      return null;
    }
    return line;
  }

  @Override
  public void setup(InputStream input) {
    reader = new DelimitedStreamReader(input, StandardCharsets.UTF_8, DEFAULT_BUFFER_SIZE);
  }
}
