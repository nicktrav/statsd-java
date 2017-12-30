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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Gauge;
import rs.nicktrave.statsd.common.Metric;
import rs.nicktrave.statsd.common.Timing;

/**
 * Decodes a {@link DatagramPacket} containing a raw statsd metric string and parses it into a
 * {@link Metric}.
 */
public class DatagramToMetricDecoder extends MessageToMessageDecoder<DatagramPacket> {

  /**
   * The current node of the statsd parsing state-machine. The progression is as follows:
   *
   * <pre>
   * NAME -> VALUE -> TYPE -> SAMPLE
   *                   |        |
   *                   +--------+-----> END
   * </pre>
   */
  private enum Node {
    NAME, VALUE, TYPE, SAMPLE
  }

  @Override protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out)
      throws Exception {
    ByteBuf byteBuf = msg.content();

    ByteBuf nameBuffer = ctx.alloc().buffer();
    ByteBuf valueBuffer = ctx.alloc().buffer();
    ByteBuf typeBuffer = ctx.alloc().buffer();
    ByteBuf sampleBuffer = ctx.alloc().buffer();

    Node node = Node.NAME;
    ByteBuf currentBuffer = nameBuffer;
    while (byteBuf.isReadable()) {
      char c = (char) byteBuf.readByte();
      switch (c) {
        case ':':
          currentBuffer = valueBuffer;
          node = Node.VALUE;
          break;
        case '|':
          if (Node.VALUE == node) {
            currentBuffer = typeBuffer;
            node = Node.TYPE;
          } else {
            currentBuffer = sampleBuffer;
            node = Node.SAMPLE;
          }
          break;
        case '@':
          break;
        default:
          currentBuffer.writeByte(c);
      }
    }

    String nameStr = nameBuffer.toString(StandardCharsets.US_ASCII);
    String valueStr = valueBuffer.toString(StandardCharsets.US_ASCII);
    String typeStr = typeBuffer.toString(StandardCharsets.US_ASCII);
    String sampleStr = sampleBuffer.toString(StandardCharsets.US_ASCII);

    Metric metric;
    // TODO(nickt): Make the types constants on the defined Metric classes.
    switch (typeStr) {
      case "c":
        metric = newCounter(nameStr, valueStr, sampleStr);
        break;
      case "g":
        metric = newGauge(nameStr, valueStr);
        break;
      case "ms":
        metric = newTiming(nameStr, valueStr, sampleStr);
        break;
      default:
        throw new IllegalStateException("Unexpected metric type: " + typeStr);
    }

    nameBuffer.release();
    valueBuffer.release();
    typeBuffer.release();
    sampleBuffer.release();

    out.add(metric);
  }

  /**
   * Returns a new counter instance with an optional sample rate.
   */
  private static Counter newCounter(String name, String value, String sample) {
    return new Counter(name, Long.valueOf(value), sample.isEmpty() ? 1.0 : Double.valueOf(sample));
  }

  /**
   * Returns a new gauge instance.
   */
  private static Gauge newGauge(String name, String value) {
    if (value.charAt(0) == '+' || value.charAt(0) == '-') {
      return new Gauge(name, Long.valueOf(value), Gauge.Type.UPDATE);
    }
    return new Gauge(name, Long.valueOf(value));
  }

  /**
   * Returns a new timing instance with an optional sample rate.
   */
  private static Timing newTiming(String name, String value, String sample) {
    return new Timing(name, Long.valueOf(value), sample.isEmpty() ? 1.0 : Double.valueOf(sample));
  }
}
