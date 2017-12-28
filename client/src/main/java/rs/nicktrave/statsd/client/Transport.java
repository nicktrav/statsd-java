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
import rs.nicktrave.statsd.common.Metric;

/**
 * A conduit for sending {@link Metric}s to a statsd server over an underlying transport protocol
 * (e.g. TCP or UDP).
 */
public interface Transport extends Closeable {

  /**
   * Attempts to write a collection of metrics to the underlying transport.
   *
   * @param metrics the metrics to write
   * @throws IOException if one or more metrics could not be written
   */
  void write(Metric ...metrics) throws IOException;

  /**
   * Attempts to close the transport within the specified time interval. If the close does not
   * complete within the interval, any metrics that have yet to be written are discarded.
   *
   * @param timeout the amount of time
   * @param timeUnit the unit of time
   */
  void close(long timeout, TimeUnit timeUnit);
}
