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

/**
 * Enum mapping for the partitioned streaming endpoints.
 */
public enum StreamingProduct {

  DECAHOSE   ("sample10"  , 2),
  FIREHOSE   ("firehose"  , 20),
  MENTIONS   ("mentions"  , 8),
  COMPLIANCE ("compliance", 8);

  private final int partitions;
  private final String name;

  /**
   * Default constructor
   * @param name - the name of the product as defined by the GNIP v2.0 streaming API
   * @param partitions - the number of partitions that comprise the product.
   */
  StreamingProduct(String name, int partitions) {
    this.name = name;
    this.partitions = partitions;
  }

  public int getPartitions() {
    return this.partitions;
  }

  public String getName() {
    return this.name;
  }

}
