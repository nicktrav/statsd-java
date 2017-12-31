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
package rs.nicktrave.statsd.loadtest;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.util.concurrent.CountDownLatch;
import rs.nicktrave.statsd.common.Metric;
import rs.nicktrave.statsd.server.MetricProcessor;
import rs.nicktrave.statsd.server.netty.NettyUdpServer;

/**
 * Runs a server instance, listening on a given address and port.
 */
// TODO(nickt): Proper logging
public class TestServer {

  private static final class Args {

    @Parameter(
        names = "-port",
        description ="The UDP port the server will listen on")
    private int port;

    @Parameter(
        names = "-numThreads",
        description = "The number of Netty threads to use. Defaults to Netty's default value")
    private int numThreads;

    @Parameter(
        names = "-useEpoll",
        description = "Whether to use the epoll transport. Only available on Linux")
    private boolean useEpoll = false;

    @Parameter(names = "--help", help = true)
    private boolean help;
  }

  private final NettyUdpServer server;
  private final CountingProcessor processor;
  private final CountDownLatch shutdownLatch;

  private TestServer(NettyUdpServer.Builder serverBuilder) {
    this.processor = new CountingProcessor();
    this.server = serverBuilder.withProcessor(processor).build();
    this.shutdownLatch = new CountDownLatch(1);
  }

  private static NettyUdpServer.Builder newServerBuilderFromArgs(Args args) {
    return NettyUdpServer.newBuilder()
        .withPort(args.port)
        .withThreads(args.numThreads)
        .withEpoll(args.useEpoll);
  }

  private void run() throws InterruptedException {
    Thread shutdownThread = new Thread(shutdownLatch::countDown);
    shutdownThread.setDaemon(true);
    Runtime.getRuntime().addShutdownHook(shutdownThread);

    System.out.println("Starting up server");
    server.start();

    System.out.println("Waiting for shutdown");
    shutdownLatch.await();

    System.out.println("Finished having processed " + processor.eventCount + " metrics");
    System.out.println("Shutting down");
    server.shutdown();
  }

  public static void main(String ...args) throws InterruptedException {
    Args cliArgs = new Args();
    JCommander jCommander = JCommander.newBuilder()
        .addObject(cliArgs)
        .build();
    jCommander.parse(args);

    if (cliArgs.help) {
      jCommander.usage();
      return;
    }

    try {
      new TestServer(newServerBuilderFromArgs(cliArgs)).run();
    } catch (Exception e) {
      System.err.println("Caught exception. Shutting down");
      e.printStackTrace();
    }
  }

  /**
   * Processes metrics by simply counting them.
   */
  private static class CountingProcessor implements MetricProcessor {

    private long eventCount;

    @Override public void process(Metric metric) {
      eventCount++;
    }
  }
}
