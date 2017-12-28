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
 * A collector retains {@link Metric}s for a configurable amount of time before they are flushed to
 * a {@link Transport}.
 */
public interface Collector {

  /**
   * Adds {@link Metric}s to this collector.
   *
   * @param metrics the metrics to add
   */
  void add(Metric ...metrics);

  /**
   * Flushes all held {@link Metric}s to an underlying {@link Transport}.
   *
   * @throws IOException if flushing to the transport fails
   */
  void flush() throws IOException;
}
