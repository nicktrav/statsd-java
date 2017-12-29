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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import rs.nicktrave.statsd.client.Transport;
import rs.nicktrave.statsd.common.Metric;

/**
 * A transport that writes {@link Metric}s as their statsd string representations to a UDP socket
 * with Netty.
 */
public class NettyUdpTransport implements Transport {

  private final EventLoopGroup group;
  private final Channel channel;

  /**
   * Create a new transport with a given destination address.
   *
   * @param address the intended address
   * @throws InterruptedException if the underlying transport could not be created
   */
  public NettyUdpTransport(InetSocketAddress address) throws InterruptedException {
    group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group)
        .channel(NioDatagramChannel.class)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override protected void initChannel(DatagramChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new StatsdToDatagramEncoder(address));
          }
        });

    // Bind to any available port
    channel = b.bind(0).sync().channel();
  }

  /**
   * Create a new transport with a given a event loop group and a channel.
   *
   * <p>Note this constructor is for testing.
   *
   * @param group the event loop group to use
   * @param channel the underlying channel
   */
  NettyUdpTransport(EventLoopGroup group, Channel channel) {
    this.group = group;
    this.channel = channel;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Writes each metric to the channel and the flushes.
   */
  @Override public void write(Metric... metrics) throws IOException {
    for (Metric metric : metrics) {
      channel.write(metric);
    }
    channel.flush();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Closes the underlying channel and shuts down the client event-loop group.
   */
  @Override public void close() throws IOException {
    try {
      channel.close().sync();
    } catch (InterruptedException ignored) {
    } finally {
      group.shutdownGracefully();
    }
  }
}
