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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import rs.nicktrave.statsd.common.Metric;

/**
 * Encodes a {@link Metric} into a {@link DatagramPacket}.
 */
@Sharable
class StatsdToDatagramEncoder extends MessageToMessageEncoder<Metric> {

  private static final Random random = new Random();

  private final InetSocketAddress address;

  StatsdToDatagramEncoder(InetSocketAddress address) {
    this.address = address;
  }

  @Override protected void encode(ChannelHandlerContext ctx, Metric msg, List<Object> out)
      throws Exception {
    ByteBuf byteBuf = ctx.alloc().buffer();
    byteBuf.writeCharSequence(msg.toString(), StandardCharsets.US_ASCII);

    InetSocketAddress sender = new InetSocketAddress(random.nextInt(100));

    out.add(new DatagramPacket(byteBuf, address, sender));
  }
}
