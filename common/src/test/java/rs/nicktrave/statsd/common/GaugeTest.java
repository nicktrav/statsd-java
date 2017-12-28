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

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GaugeTest {

  private static final String NAME = "foo";
  private static final long VALUE = 42;

  private Gauge gauge;

  @Before public void setup() {
    gauge = new Gauge(NAME, VALUE);
  }

  @Test public void testGetName() {
    assertThat(gauge.name).isEqualTo(NAME);
  }

  @Test public void testGetValue() {
    assertThat(gauge.value).isEqualTo(VALUE);
  }

  @Test public void testGetType_default() {
    Gauge gauge = new Gauge(NAME, VALUE);
    assertThat(gauge.getType()).isEqualTo(Gauge.Type.SET);
  }

  @Test public void testGetType_nonDefault() {
    Gauge.Type type = Gauge.Type.UPDATE;
    Gauge gauge = new Gauge(NAME, VALUE, type);
    assertThat(gauge.getType()).isEqualTo(type);
  }

  @Test public void testToString_defaultType() {
    assertThat(gauge.toString()).isEqualTo(String.format("%s:%d|g", NAME, VALUE));
  }

  @Test public void testToString_nonDefaultDefault_positive() {
    gauge = new Gauge(NAME, VALUE, Gauge.Type.UPDATE);
    assertThat(gauge.toString()).isEqualTo(String.format("%s:+%d|g", NAME, VALUE));
  }

  @Test public void testToString_nonDefaultDefault_negative() {
    gauge = new Gauge(NAME, -1 * VALUE, Gauge.Type.UPDATE);
    assertThat(gauge.toString()).isEqualTo(String.format("%s:-%d|g", NAME, VALUE));
  }
}
