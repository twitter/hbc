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

import com.google.common.base.Preconditions;

public class Location {

  public static class Coordinate {

    private final double longitude;
    private final double latitude;

    public Coordinate(double longitude, double latitude) {
      this.longitude = longitude;
      this.latitude = latitude;
    }

    public double longitude() {
      return longitude;
    }

    public double latitude () {
      return latitude;
    }

    @Override
    public String toString() {
      return longitude + "," + latitude();
    }

  }

  private final Coordinate southwest, northeast;

  public Location(Coordinate southwest, Coordinate northeast) {
    this.southwest = Preconditions.checkNotNull(southwest);
    this.northeast = Preconditions.checkNotNull(northeast);
  }

  public Coordinate southwestCoordinate() {
    return southwest;
  }

  public Coordinate northeastCoordinate() {
    return northeast;
  }

  @Override
  public String toString() {
    return southwest + "," + northeast;
  }
}
