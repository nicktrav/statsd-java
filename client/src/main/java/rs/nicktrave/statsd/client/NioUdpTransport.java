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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import rs.nicktrave.statsd.common.Metric;

/**
 * A transport that write {@link Metric}s as bytes to a to an NIO UDP channel.
 */
public class NioUdpTransport implements Transport {

  private final DatagramChannel channel;

  /**
   * A new UDP transport for a given address and port.
   *
   * @param address the destination address
   * @throws IOException if an IO error occurs opening the channel
   */
  public NioUdpTransport(SocketAddress address) throws IOException {
    this(address, DatagramChannel.open());
  }

  /**
   * A new UDP transport for a given address, port and socket.
   *
   * @param address the destination address
   * @param channel the UDP channel to write to
   * @throws IOException if an IO error occurs opening the channel
   */
  public NioUdpTransport(SocketAddress address, DatagramChannel channel) throws IOException {
    this.channel = channel;
    this.channel.connect(address);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Writes each event <i>synchronously</i> to the channel.
   */
  @Override public void write(Metric... metrics) throws IOException {
    for (Metric metric : metrics) {
      byte[] bytes = metric.toString().getBytes(StandardCharsets.US_ASCII);
      channel.write(ByteBuffer.wrap(bytes));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public void close(long timeout, TimeUnit timeUnit)
      throws IOException, TimeoutException {
    close();
  }

  /**
   * {@inheritDoc}
   */
  @Override public void close() throws IOException {
    channel.close();
  }
}
