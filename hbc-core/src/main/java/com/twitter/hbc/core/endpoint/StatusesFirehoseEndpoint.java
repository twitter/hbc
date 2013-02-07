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

package com.twitter.hbc.core.endpoint;

import com.google.common.base.Joiner;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpConstants;

import java.util.Set;

public class StatusesFirehoseEndpoint extends DefaultStreamingEndpoint implements PartitionableEndpoint {

  public static final String PATH = "/statuses/firehose.json";

  public StatusesFirehoseEndpoint() {
    super(PATH, HttpConstants.HTTP_GET, true);
  }

  public void partitions(Set<Integer> partitions) {
    addQueryParameter(Constants.PARTITION_PARAM, Joiner.on(',').join(partitions));
  }
}