/**
 * Copyright 2016 Twitter, Inc.
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

package com.twitter.hbc.core.endpoint.v2;

import com.google.common.base.Preconditions;

/**
 * The endpoint used for consuming volume streams from GNIP v2.0 API.
 */
public class VolumeStreamingEndpoint extends EnterpriseStreamingEndpoint_v2 implements PartitionedEndpoint {

  private final int partition;

  public VolumeStreamingEndpoint(String account, StreamingProduct streamingProduct, String label, int partition, int backfillMins) {
    super(account, streamingProduct.getName(), label);

    Preconditions.checkArgument(streamingProduct.getPartitions() >= partition && partition >= 1,
        "partition must be between 1 and " + streamingProduct.getPartitions());
    this.partition = partition;
    Preconditions.checkArgument(5 >= backfillMins && backfillMins >= 0);
    addQueryParameter("partition", String.valueOf(partition));
    addQueryParameter("backfillMinutes", String.valueOf(backfillMins));
  }

  @Override
  public int getPartition() {
    return this.partition;
  }

  @Override
  public void setBackfillCount(int count) {
    //noop
  }

  @Override
  public void setApiVersion(String apiVersion) {
    //noop
  }

}
