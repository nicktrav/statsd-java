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
import rs.nicktrave.statsd.common.Metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StatsdClientTest {

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private Transport transport;
  @Mock private Collector collector;
  @Mock private Metric metric;
  private StatsdClient statsdClient;

  @Before public void setup() {
    statsdClient = StatsdClient.newBuilder()
        .withCollector(collector)
        .withTransport(transport)
        .build();
  }

  @Test public void testBuilder() {
    assertThat(statsdClient.getCollector()).isEqualTo(collector);
    assertThat(statsdClient.getTransport()).isEqualTo(transport);
  }

  @Test public void testBuilder_noCollector() {
    StatsdClient.Builder builder = StatsdClient.newBuilder().withTransport(transport);
    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Collector and transport must be provided");
  }
  
  @Test public void testBuilder_noTransport() {
    StatsdClient.Builder builder = StatsdClient.newBuilder().withCollector(collector);
    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Collector and transport must be provided");
  }

  @Test public void testSend() {
    statsdClient.send(new Metric[]{metric});
    verify(collector, times(1)).add(metric);
  }

  @Test public void testClose() throws IOException {
    statsdClient.close();
    verify(collector, times(1)).flush();
    verify(transport, times(1)).close();
  }
}
