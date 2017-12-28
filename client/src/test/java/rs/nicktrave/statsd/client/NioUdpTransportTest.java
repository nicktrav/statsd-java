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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NioUdpTransportTest {

  private static final Metric METRIC_1 = new Counter("foo", 42);
  private static final Metric METRIC_2 = new Counter("bar", 123);

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private SocketAddress address;
  @Mock private DatagramChannel channel;
  @Captor private ArgumentCaptor<ByteBuffer> packetCaptor;
  private Transport transport;

  @Before public void setup() throws IOException {
    transport = new NioUdpTransport(address, channel);
  }

  @Test public void testNew() throws IOException {
    verify(channel, times(1)).connect(address);
  }

  @Test public void testWrite() throws IOException {
    transport.write(METRIC_1, METRIC_2);

    verify(channel, times(2)).write(packetCaptor.capture());
    assertWrittenDataContains(METRIC_1, packetCaptor.getAllValues());
    assertWrittenDataContains(METRIC_2, packetCaptor.getAllValues());
  }

  /**
   * Assert that a given metric was contained in the data sent in one or more packets.
   */
  private void assertWrittenDataContains(Metric expected, Collection<ByteBuffer> buffers) {
    assertThat(buffers.stream().anyMatch(b ->
        Arrays.equals(expected.toString().getBytes(StandardCharsets.US_ASCII), b.array())))
        .isTrue();
  }
}
