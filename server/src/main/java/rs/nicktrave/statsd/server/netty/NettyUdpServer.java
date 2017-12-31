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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import rs.nicktrave.statsd.server.MetricProcessor;

/**
 * A Netty-based UDP server that decodes incoming statsd packets and offloads them to a {@link
 * MetricProcessor} instance.
 */
public class NettyUdpServer {

  private static final int DEFAULT_PORT = 8125;

  private final MetricProcessor processor;
  private final boolean epoll;
  private final EventLoopGroup group;
  private final Channel[] channels;
  private final InetSocketAddress address;
  private final AtomicBoolean started;

  // TODO(nickt): Add null checks and validation when constructing
  private NettyUdpServer(Builder builder) {
    processor = builder.processor;
    epoll = builder.epoll;

    int numChannels;
    int numThreads = builder.threads;
    if (epoll) {
      numChannels = numThreads;
      group = new EpollEventLoopGroup(numThreads);
    } else {
      numChannels = 1; // cannot reuse the port with the NIO transport, hence a single channel
      group = new NioEventLoopGroup(numThreads);
    }

    channels = new Channel[numChannels];
    address = new InetSocketAddress(builder.port > 0 ? builder.port : DEFAULT_PORT);
    started = new AtomicBoolean(false);
  }

  /**
   * Returns a new builder for a {@link NettyUdpServer} instance.
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * A builder for a {@link NettyUdpServer} instance.
   */
  public static class Builder {

    private MetricProcessor processor;
    private int threads;
    private int port;
    private boolean epoll = false;

    /**
     * Sets the {@link MetricProcessor} to use in this server instance.
     */
    public Builder withProcessor(MetricProcessor processor) {
      this.processor = processor;
      return this;
    }

    /**
     * Sets the number of threads to use in the Netty worker pool for this server instance.
     */
    public Builder withThreads(int threads) {
      this.threads = threads;
      return this;
    }

    /**
     * Sets the port that this server instance should bind to.
     */
    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    /**
     * Whether to use the epoll transport for this server.
     *
     * <p>Note that this is only available when running on Linux.
     */
    public Builder withEpoll(boolean value) {
      this.epoll = value;
      return this;
    }

    /**
     * Construct and return a new server instance.
     */
    public NettyUdpServer build() {
      return new NettyUdpServer(this);
    }
  }

  /**
   * Starts the server by constructing the Netty event loop and binding one more more channels to
   * the port.
   *
   * <p>In the case of an exception when binding channels to the port, this method can throw a
   * {@link IOException}.
   *
   * @throws InterruptedException if the server was interrupted when starting up
   */
  public synchronized void start() throws InterruptedException {
    if (started.get()) {
      return;
    }

    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override protected void initChannel(DatagramChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new DatagramToMetricDecoder());
            pipeline.addLast(new MetricOffloadHandler(processor));
          }
        });

    if (epoll) {
      bootstrap
          .channel(EpollDatagramChannel.class)
          .option(EpollChannelOption.SO_REUSEPORT, true);
    } else {
      bootstrap.channel(NioDatagramChannel.class);
    }

    try {
      for (int i = 0; i < channels.length; i++) {
        channels[i] = bootstrap.bind(address).sync().channel();
      }
    } catch (Exception e) {
      doShutdown();
      throw e;
    }

    started.set(true);
  }

  /**
   * Shuts down the server, closing all bound channels and shutting down the event loop.
   */
  public synchronized void shutdown() {
    if (!started.get()) {
      return;
    }

    doShutdown();

    started.set(false);
  }

  private void doShutdown() {
    try {
      for (Channel channel : channels) {
        if (channel == null) {
          continue;
        }
        channel.close().sync();
      }
    } catch (InterruptedException ignored) {
    } finally {
      group.shutdownGracefully();
    }
  }
}
