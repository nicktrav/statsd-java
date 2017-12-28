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
package rs.nicktrave.statsd.client;

import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UnbufferedCollectorTest {

  private static final Metric METRIC_1 = new Counter("foo", 123);
  private static final Metric METRIC_2 = new Counter("bar", 456);

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private Transport transport;
  private UnbufferedCollector collector;

  @Before public void setup() {
    collector = new UnbufferedCollector(transport);
  }

  @Test public void testAdd() throws IOException {
    collector.add(METRIC_1, METRIC_2);

    verify(transport, times(1)).write(METRIC_1, METRIC_2);
  }

  @Test public void testAdd_transportThrows() throws IOException {
    doThrow(IOException.class).when(transport).write(any(Metric.class));
    assertThatThrownBy(() -> collector.add(METRIC_1, METRIC_2))
        .hasCauseExactlyInstanceOf(IOException.class)
        .hasMessage("Could not flush metrics to transport");
  }

  @Test public void testFlush() throws IOException {
    assertThatThrownBy(() -> collector.flush())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Can not call flush directly");
  }
}
