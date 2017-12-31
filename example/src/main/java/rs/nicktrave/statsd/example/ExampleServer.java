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
package rs.nicktrave.statsd.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import rs.nicktrave.statsd.common.Metric;
import rs.nicktrave.statsd.server.MetricProcessor;
import rs.nicktrave.statsd.server.netty.NettyUdpServer;

public class ExampleServer {

  private final CountDownLatch shutdownLatch;

  private ExampleServer() {
    shutdownLatch = new CountDownLatch(1);
  }

  private void run() throws IOException, InterruptedException {
    InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 8125);
    Thread shutdownThread = new Thread(shutdownLatch::countDown);
    shutdownThread.setDaemon(true);
    Runtime.getRuntime().addShutdownHook(shutdownThread);

    System.out.println("Starting up server");
    TestProcessor processor = new TestProcessor();
    NettyUdpServer server = new NettyUdpServer(address, processor);
    server.start();

    System.out.println("Waiting for shutdown");
    shutdownLatch.await();

    System.out.println("Finished having processed " + processor.eventCount + " metrics");
    System.out.println("Shutting down");
    server.shutdown();

    Thread.sleep(1_000);
  }

  public static void main(String ...args) throws InterruptedException, IOException {
    new ExampleServer().run();
  }

  private static class TestProcessor implements MetricProcessor {

    private long eventCount;

    @Override public void process(Metric metric) {
      eventCount++;
    }
  }
}
