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
 * A metric representing <i>either</i> the present value of a measurement or a increment/decrement
 * to an existing measurement.
 */
public class Gauge extends Metric {

  /**
   * The kind of gauge, being either the setting of the gauge to a given value, or the update to an
   * existing gauge via an increment or decrement.
   */
  public enum Type {
    SET, UPDATE
  }

  private final Type type;

  /**
   * Instantiate a new gauge being <i>set</i> at a given value.
   */
  public Gauge(String name, long value) {
    this(name, value, Type.SET);
  }

  /**
   * Instantiate a new gauge of a given type with a name and value.
   */
  public Gauge(String name, long value, Type type) {
    super(name, value);
    this.type = type;
  }

  /**
   * @return the type of gauge
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the statsd representation of this gauge. For example:
   *
   * <pre>{@code
   * gaugor:333|g
   * gaugor:-10|g
   * gaugor:+4|g
   * }</pre>
   */
  @Override public String toString() {
    StringBuilder builder = new StringBuilder(name);
    builder.append(":");

    if (Type.UPDATE == type && value > 0) {
      builder.append("+");
    }

    builder.append(value);
    builder.append("|g");

    return builder.toString();
  }
}
