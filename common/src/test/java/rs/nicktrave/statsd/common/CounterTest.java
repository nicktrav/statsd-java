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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CounterTest {

  private static final String NAME = "foo";
  private static final long VALUE = 42;

  private Counter counter;

  @Before public void setup() throws IllegalAccessException {
    counter = new Counter(NAME, VALUE);
  }

  @Test public void testGetName() {
    assertThat(counter.name).isEqualTo(NAME);
  }

  @Test public void testGetValue() {
    assertThat(counter.value).isEqualTo(VALUE);
  }

  @Test public void testGetSampleRate() throws IllegalAccessException {
    double sampleRate = 0.5;
    counter = new Counter(NAME, VALUE, sampleRate);
    assertThat(counter.getSampleRate()).isEqualTo(sampleRate);
  }

  @Test public void testNew_invalidSampleRate() {
    assertThatThrownBy(() -> new Counter(NAME, VALUE, -1))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> new Counter(NAME, VALUE, 1.1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test public void testToString_defaultSampleRate() {
    assertThat(counter.toString()).isEqualTo(String.format("%s:%d|c", NAME, VALUE));
  }

  @Test public void testToString_nonDefaultSampleRate() {
    double sampleRate = 0.5;
    counter = new Counter(NAME, VALUE, sampleRate);
    assertThat(counter.toString())
        .isEqualTo(String.format("%s:%d|c|@%.1f", NAME, VALUE, sampleRate));
  }
}
