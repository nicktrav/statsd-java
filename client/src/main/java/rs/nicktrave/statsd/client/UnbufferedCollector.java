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
import rs.nicktrave.statsd.common.Metric;

/**
 * A {@link Collector} that blocks waiting for the metric(s) to be flushed to the underlying
 * transport.
 */
public class UnbufferedCollector implements Collector {

  private final Transport transport;
  private Metric[] metrics;

  /**
   * Creates a new {@link Collector} that will perform a blocking write to the given transport as
   * metrics are added.
   *
   * @param transport the transport to write to
   */
  public UnbufferedCollector(Transport transport) {
    this.transport = transport;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method performs a blocking write to the underlying transport.
   */
  @Override public synchronized void add(Metric... metrics) {
    this.metrics = metrics;
    try {
      flush();
    } catch (IOException e) {
      throw new RuntimeException("Could not flush metrics to transport", e);
    } finally {
      this.metrics = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public void flush() throws IOException {
    if (metrics == null) {
      return;
    }
    transport.write(metrics);
  }

  Metric[] getMetrics() {
    return metrics;
  }
}
