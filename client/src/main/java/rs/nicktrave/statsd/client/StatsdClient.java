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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import rs.nicktrave.statsd.common.Metric;

/**
 * A client for sending {@link Metric}s via a transport to a statsd server.
 */
public class StatsdClient implements Closeable {

  private final Collector collector;
  private final Transport transport;

  private StatsdClient(Builder builder) {
    this.collector = builder.collector;
    this.transport = builder.transport;
  }

  /**
   * @return a new {@link Builder}
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Constructs {@link StatsdClient} instances.
   */
  public static class Builder {

    private Collector collector;
    private Transport transport;

    private Builder() {
    }

    /**
     * Use a the provided collector for this client.
     */
    public Builder withCollector(Collector collector) {
      this.collector = collector;
      return this;
    }

    /**
     * Use a the provided transport for this client.
     */
    public Builder withTransport(Transport transport) {
      this.transport = transport;
      return this;
    }

    /**
     * @return a new client instance
     */
    public StatsdClient build() {
      if (collector == null || transport == null) {
        throw new IllegalStateException("Collector and transport must be provided");
      }
      return new StatsdClient(this);
    }
  }

  /**
   * Attempts to send a metric to a statsd server.
   *
   * @param metric the metric to send
   */
  public void send(Metric metric) {
    collector.add(metric);
  }

  public void send(Metric ...metrics) {
    collector.add(metrics);
  }

  /**
   * Attempts to shut down the client within the specified time interval. If the close does not
   * complete within the interval, metrics that are yet to be written are discarded.
   *
   * @param timeout the amount of time
   * @param timeUnit the unit of time
   * @throws IOException if there was an I/O exception closing the client
   * @throws TimeoutException if the specified timeout was exceeded
   */
  // TODO(nickt): Add timeout logic
  public void close(long timeout, TimeUnit timeUnit) throws IOException, TimeoutException {
    close();
  }

  /**
   * {@inheritDoc}
   */
  @Override public void close() throws IOException {
    collector.flush();
    transport.close();
  }

  /**
   * @return the collector for this client
   */
  Collector getCollector() {
    return collector;
  }

  /**
   * @return the transport for this client
   */
  Transport getTransport() {
    return transport;
  }
}
