/*
 * Copyright (C) 2017 Nick Travers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rs.nicktrave.statsd.common;

/**
 * A metric representing the timing of an event.
 */
public class Timing extends Metric {

  private final double sampleRate;

  /**
   * A new timing with a given name and value (in ms), representing a 100% sample rate.
   */
  public Timing(String name, long value) {
    this(name, value, 1.0);
  }

  /**
   * A new timing with a given name, value (in ms) and sample rate in the range [0, 1].
   *
   * @throws IllegalArgumentException if the sample rate is not in the range [0, 1].
   */
  public Timing(String name, long value, double sampleRate) {
    super(name, value);
    if (sampleRate < 0 || sampleRate > 1) {
      throw new IllegalArgumentException(
          "Sample rate " + sampleRate + " is not in the range [0, 1]");
    }

    this.sampleRate = sampleRate;
  }

  /**
   * @return the sample rate of this timing
   */
  public double getSampleRate() {
    return sampleRate;
  }

  /**
   * Returns the statsd representation of this timing. For example:
   *
   * <pre>{@code
   * glork:320|ms
   * glork:320|ms|@0.1
   * }</pre>
   */
  @Override public String toString() {
    StringBuilder builder = new StringBuilder(name);
    builder.append(":");
    builder.append(value);
    builder.append("|ms");

    if (sampleRate < 1) {
      builder.append("|@");
      builder.append(sampleRate);
    }

    return builder.toString();
  }
}
