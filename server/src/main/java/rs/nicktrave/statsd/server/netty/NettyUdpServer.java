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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueChannelOption;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueDatagramChannelConfig;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
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

  private static final int THREADS = 10;

  private final InetSocketAddress address;
  private final MetricProcessor processor;

  private EventLoopGroup group;
  //private Channel channel;
  private Channel[] channels;

  public NettyUdpServer(InetSocketAddress address, MetricProcessor processor) {
    this.address = address;
    this.processor = processor;
  }

  public void start() throws IOException, InterruptedException {
    group = new EpollEventLoopGroup(THREADS);
    //group = new KQueueEventLoopGroup(THREADS);
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
        //.channel(KQueueDatagramChannel.class)
        .channel(EpollDatagramChannel.class)
        //.option(KQueueChannelOption.SO_REUSEPORT, true)
        .option(EpollChannelOption.SO_REUSEPORT, true)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override protected void initChannel(DatagramChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new DatagramToMetricDecoder());
            pipeline.addLast(new MetricOffloadHandler(processor));
          }
        });

    //ChannelFuture future = bootstrap.bind(address);
    //try {
    //  future.await();
    //} catch (InterruptedException e) {
    //  Thread.currentThread().interrupt();
    //  throw new RuntimeException("Interrupted binding to port", e);
    //}
    //
    //if (!future.isSuccess()) {
    //  throw new IOException("Failed to bind to port");
    //}

    channels = new Channel[THREADS];
    for (int i = 0; i < THREADS; i++) {
      channels[i] = bootstrap.bind(address).sync().channel();
    }

    //channel = future.channel();
  }

  public void shutdown() {
    //channel.close().addListener((ChannelFutureListener) future -> group.shutdownGracefully());
    for (Channel channel : channels) {
      channel.close();
    }
    group.shutdownGracefully();
  }
}
