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
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Gauge;
import rs.nicktrave.statsd.common.Metric;
import rs.nicktrave.statsd.common.Timing;

import static org.assertj.core.api.Assertions.assertThat;

public class DatagramToMetricDecoderTest {

  private static final String METRIC_NAME = "foo";
  private static final long METRIC_VALUE = 42;
  private static final double METRIC_SAMPLE_RATE = 0.42;

  private EmbeddedChannel channel;

  @Before public void setup() {
    channel = new EmbeddedChannel(new DatagramToMetricDecoder());
  }

  @Test public void testCounter() throws InterruptedException {
    channel.writeInbound(newDatagramPacket(newCounterString(METRIC_VALUE, false)));

    Metric metric = channel.readInbound();

    assertCounterEquals(METRIC_NAME, METRIC_VALUE, 1, metric);
  }

  @Test public void testCounter_negative() throws InterruptedException {
    channel.writeInbound(newDatagramPacket(newCounterString(-METRIC_VALUE, false)));

    Metric metric = channel.readInbound();

    assertCounterEquals(METRIC_NAME, -METRIC_VALUE, 1, metric);
  }

  @Test public void testCounter_sample() throws InterruptedException {
    channel.writeInbound(newDatagramPacket(newCounterString(METRIC_VALUE, true)));

    Metric metric = channel.readInbound();

    assertCounterEquals(METRIC_NAME, METRIC_VALUE, METRIC_SAMPLE_RATE, metric);
  }

  @Test public void testCounter_positiveSign() throws InterruptedException {
    String metricString = String.format("%s:+%d|c", METRIC_NAME, METRIC_VALUE);
    channel.writeInbound(newDatagramPacket(metricString));

    Metric metric = channel.readInbound();

    assertCounterEquals(METRIC_NAME, METRIC_VALUE, 1, metric);
  }

  @Test public void testGauge_setValue() {
    channel.writeInbound(newDatagramPacket(newGaugeString(null)));

    Metric metric = channel.readInbound();

    assertGaugeEquals(METRIC_NAME, METRIC_VALUE, Gauge.Type.SET, metric);
  }

  @Test public void testGauge_updateValue_positive() {
    channel.writeInbound(newDatagramPacket(newGaugeString("+")));

    Metric metric = channel.readInbound();

    assertGaugeEquals(METRIC_NAME, METRIC_VALUE, Gauge.Type.UPDATE, metric);
  }

  @Test public void testGauge_updateValue_negative() {
    channel.writeInbound(newDatagramPacket(newGaugeString("-")));

    Metric metric = channel.readInbound();

    assertGaugeEquals(METRIC_NAME, -METRIC_VALUE, Gauge.Type.UPDATE, metric);
  }

  @Test public void testTiming() throws InterruptedException {
    channel.writeInbound(newDatagramPacket(newTimingString(METRIC_VALUE, false)));

    Metric metric = channel.readInbound();

    assertTimingEquals(METRIC_NAME, METRIC_VALUE, 1, metric);
  }

  @Test public void testTiming_negative() throws InterruptedException {
    channel.writeInbound(newDatagramPacket(newTimingString(-METRIC_VALUE, false)));

    Metric metric = channel.readInbound();

    assertTimingEquals(METRIC_NAME, -METRIC_VALUE, 1, metric);
  }

  @Test public void testTiming_sample() throws InterruptedException {
    channel.writeInbound(newDatagramPacket(newTimingString(METRIC_VALUE, true)));

    Metric metric = channel.readInbound();

    assertTimingEquals(METRIC_NAME, METRIC_VALUE, METRIC_SAMPLE_RATE, metric);
  }

  @Test public void testTiming_positiveSign() throws InterruptedException {
    String metricString = String.format("%s:+%d|ms", METRIC_NAME, METRIC_VALUE);
    channel.writeInbound(newDatagramPacket(metricString));

    Metric metric = channel.readInbound();

    assertTimingEquals(METRIC_NAME, METRIC_VALUE, 1, metric);
  }

  private static DatagramPacket newDatagramPacket(String statsdMetricString) {
    ByteBuf input = Unpooled.buffer();
    input.writeCharSequence(statsdMetricString, StandardCharsets.US_ASCII);
    return new DatagramPacket(input, new InetSocketAddress(42));
  }

  private static String newCounterString(long value, boolean withSampleRate) {
    if (withSampleRate) {
      return String.format("%s:%d|c|@%f", METRIC_NAME, value, METRIC_SAMPLE_RATE);
    }
    return String.format("%s:%d|c", METRIC_NAME, value);
  }

  private static String newGaugeString(@Nullable String sign) {
    if (sign != null) {
      return String.format("%s:%s%d|g", METRIC_NAME, sign, METRIC_VALUE);
    }
    return String.format("%s:%d|g", METRIC_NAME, METRIC_VALUE);
  }

  private static String newTimingString(long value, boolean withSampleRate) {
    if (withSampleRate) {
      return String.format("%s:%d|ms|@%f", METRIC_NAME, value, METRIC_SAMPLE_RATE);
    }
    return String.format("%s:%d|ms", METRIC_NAME, value);
  }

  private static void assertCounterEquals(String name, long value, double sampleRate,
      Metric metric) {
    assertThat(metric).isInstanceOf(Counter.class);
    Counter counter = (Counter) metric;

    assertThat(counter.name).isEqualTo(name);
    assertThat(counter.value).isEqualTo(value);
    assertThat(counter.getSampleRate()).isEqualTo(sampleRate);
  }

  private static void assertGaugeEquals(String name, long value, Gauge.Type type, Metric metric) {
    assertThat(metric).isInstanceOf(Gauge.class);
    Gauge gauge = (Gauge) metric;

    assertThat(gauge.name).isEqualTo(name);
    assertThat(gauge.value).isEqualTo(value);
    assertThat(gauge.getType()).isEqualTo(type);
  }

  private static void assertTimingEquals(String name, long value, double sampleRate,
      Metric metric) {
    assertThat(metric).isInstanceOf(Timing.class);
    Timing timing = (Timing) metric;

    assertThat(timing.name).isEqualTo(name);
    assertThat(timing.value).isEqualTo(value);
    assertThat(timing.getSampleRate()).isEqualTo(sampleRate);
  }
}
