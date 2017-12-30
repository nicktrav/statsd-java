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
package rs.nicktrave.statsd.server.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import rs.nicktrave.statsd.server.MetricProcessor;

/**
 * A Netty-based UDP server that decodes incoming statsd packets and offloads them to a {@link
 * MetricProcessor} instance.
 */
public class NettyUdpServer {

  private final InetSocketAddress address;
  private final MetricProcessor processor;

  private EventLoopGroup group;
  private Channel channel;

  public NettyUdpServer(InetSocketAddress address, MetricProcessor processor) {
    this.address = address;
    this.processor = processor;
  }

  public void start() throws IOException {
    group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
        .channel(NioDatagramChannel.class)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override protected void initChannel(DatagramChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new DatagramToMetricDecoder());
            pipeline.addLast(new MetricOffloadHandler(processor));
          }
        });

    ChannelFuture future = bootstrap.bind(address);
    try {
      future.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted binding to port", e);
    }

    if (!future.isSuccess()) {
      throw new IOException("Failed to bind to port");
    }

    channel = future.channel();
  }

  public void shutdown() {
    channel.close().addListener((ChannelFutureListener) future -> group.shutdownGracefully());
  }
}
