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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import rs.nicktrave.statsd.common.Metric;
import rs.nicktrave.statsd.server.MetricProcessor;

/**
 * Offloads a {@link Metric} to a {@link MetricProcessor}.
 */
public class MetricOffloadHandler extends SimpleChannelInboundHandler<Metric> {

  private final MetricProcessor processor;

  public MetricOffloadHandler(MetricProcessor processor) {
    this.processor = processor;
  }

  @Override protected void channelRead0(ChannelHandlerContext ctx, Metric msg) throws Exception {
    processor.process(msg);
  }
}
