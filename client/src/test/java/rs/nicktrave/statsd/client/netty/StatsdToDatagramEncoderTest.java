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
package rs.nicktrave.statsd.client.netty;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class StatsdToDatagramEncoderTest {

  private static final InetSocketAddress socketAddress = new InetSocketAddress("hostly", 123);
  private static final Metric METRIC_1 = new Counter("foo", 42);

  private StatsdToDatagramEncoder encoder;

  @Before public void setup() {
    encoder = new StatsdToDatagramEncoder(socketAddress);
  }

  @Test public void testEncode() {
    EmbeddedChannel channel = new EmbeddedChannel(encoder);
    assertTrue(channel.writeOutbound(METRIC_1));

    DatagramPacket packet = channel.readOutbound();
    assertThat(packet.recipient()).isEqualTo(socketAddress);
    assertThat(packet.content().toString(StandardCharsets.US_ASCII)).isEqualTo(METRIC_1.toString());
  }
}
