/**
 * Copyright 2014 Twitter, Inc.
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


public class RealTimeEnterpriseStreamingEndpoint_v2 extends EnterpriseStreamingEndpoint_v2 {

  public RealTimeEnterpriseStreamingEndpoint_v2(String account, String product, String label) {
    super(account, product, label);
  }

  public RealTimeEnterpriseStreamingEndpoint_v2(String account, String product, String label, int clientId) {
    super(account, product, label, clientId);
  }
}
