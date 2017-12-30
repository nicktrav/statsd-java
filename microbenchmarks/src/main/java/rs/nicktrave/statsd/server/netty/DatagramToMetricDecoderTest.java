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
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import rs.nicktrave.statsd.microbenchmarks.AbstractMicrobenchmarkBase;

@State(Scope.Benchmark)
public class DatagramToMetricDecoderTest extends AbstractMicrobenchmarkBase {

  private static final DatagramToMetricDecoder decoder = new DatagramToMetricDecoder();
  private static final byte[] content = "foo:1234|c".getBytes(StandardCharsets.US_ASCII);
  private static final ByteBuf buf = Unpooled.wrappedBuffer(content);
  private static final InetSocketAddress address = new InetSocketAddress(42);

  @Benchmark
  public void test() throws Exception {
    EmbeddedChannel embeddedChannel = new EmbeddedChannel(decoder);
    embeddedChannel.writeInbound(new DatagramPacket(buf.copy(), address));
  }
}
