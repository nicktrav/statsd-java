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
 * A metric representing a count.
 */
public class Counter extends Metric {

  private final double sampleRate;

  /**
   * A new counter with a given name and value, representing a 100% sample rate.
   */
  public Counter(String name, long value) {
    this(name, value, 1.0);
  }

  /**
   * A new counter with a given name, value and sample rate in the range [0, 1].
   *
   * @throws IllegalArgumentException if the sample rate is not in the range [0, 1].
   */
  public Counter(String name, long value, double sampleRate) {
    super(name, value);
    if (sampleRate < 0 || sampleRate > 1) {
      throw new IllegalArgumentException(
          "Sample rate " + sampleRate + " is not in the range [0, 1]");
    }

    this.sampleRate = sampleRate;
  }

  /**
   * @return the sample rate of this counter
   */
  public double getSampleRate() {
    return sampleRate;
  }

  /**
   * Returns the statsd representation of this counter. For example:
   *
   * <pre>{@code
   * gorets:1|c
   * gorets:1|c|@0.1
   * }</pre>
   */
  @Override public String toString() {
    StringBuilder builder = new StringBuilder(name);
    builder.append(":");
    builder.append(value);
    builder.append("|c");

    if (sampleRate < 1) {
      builder.append("|@");
      builder.append(sampleRate);
    }

    return builder.toString();
  }
}
