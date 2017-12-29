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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NettyUdpTransportTest {

  private static final Metric METRIC_1 = new Counter("foo", 1);
  private static final Metric METRIC_2 = new Counter("bar", 2);

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private EventLoopGroup group;
  @Mock private Channel channel;
  @Mock private ChannelFuture channelFuture;
  private NettyUdpTransport transport;

  @Before public void setup() throws InterruptedException {
    when(channel.close()).thenReturn(channelFuture);
    transport = new NettyUdpTransport(group, channel);
  }

  @Test public void testWrite() throws IOException {
    transport.write(METRIC_1, METRIC_2);
    verify(channel, times(1)).write(METRIC_1);
    verify(channel, times(1)).write(METRIC_2);
    verify(channel, times(1)).flush();
  }

  @Test public void testClose() throws IOException {
    transport.close();

    verify(channel, times(1)).close();
    verify(group, times(1)).shutdownGracefully();
  }

  @Test public void testClose_channelCloseThrows() throws IOException, InterruptedException {
    when(channelFuture.sync()).thenThrow(InterruptedException.class);

    transport.close();

    verify(group, times(1)).shutdownGracefully();
  }
}
