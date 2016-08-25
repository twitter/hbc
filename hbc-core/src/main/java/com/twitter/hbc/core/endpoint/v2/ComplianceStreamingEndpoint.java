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
 * The endpoint used for consuming the partitioned compliance firehose from GNIP.
 * The stream consists of 8 partitions of which each needs to be connected
 * via its own http client.
 */
public class ComplianceStreamingEndpoint extends EnterpriseStreamingEndpoint_v2 implements PartitionedEndpoint {

  private final int partition;

  public ComplianceStreamingEndpoint(String account, String label, int partition) {
    super(account, StreamingProduct.COMPLIANCE.getName(), label);

    Preconditions.checkArgument(StreamingProduct.COMPLIANCE.getPartitions() >= partition && partition >= 1);
    this.partition = partition;
    addQueryParameter("partition", String.valueOf(partition));
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
