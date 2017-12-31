package rs.nicktrave.statsd.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import rs.nicktrave.statsd.client.Collector;
import rs.nicktrave.statsd.client.StatsdClient;
import rs.nicktrave.statsd.client.Transport;
import rs.nicktrave.statsd.client.UnbufferedCollector;
import rs.nicktrave.statsd.client.netty.NettyUdpTransport;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

public class ExampleClient {

  private static final int PORT = 8125;

  private void run(String hostname, int runTimeSeconds, int metricsPerSecond)
      throws IOException, InterruptedException {

    System.out.println("Starting server ...");
    Transport t = new NettyUdpTransport(new InetSocketAddress(hostname, PORT));
    Collector c = new UnbufferedCollector(t);
    StatsdClient client = StatsdClient.newBuilder()
        .withTransport(t)
        .withCollector(c)
        .build();

    Thread thread1 = newSendThread(client, runTimeSeconds, metricsPerSecond);
    thread1.start();
    thread1.join();

    System.out.println("Shutting down ...");
    client.close();
  }

  public static void main(String ...args) throws IOException, InterruptedException {
    // Parse command line args
    String hostname = args[0];
    int metricsPerSecond = Integer.valueOf(args[1]);
    int runTimeSeconds = Integer.valueOf(args[2]);

    System.out.println(String.format("Sending a rate of %d metrics per second for %d seconds",
        metricsPerSecond, runTimeSeconds));

    new ExampleClient().run(hostname, runTimeSeconds, metricsPerSecond);
  }

  private static Thread newSendThread(StatsdClient client, int runTime, int metricsPerSecond)
      throws InterruptedException {
    return new Thread(() -> {
      Metric[] metrics;
      for (int i = 0; i < runTime; i++) {
        metrics = generateMetrics(metricsPerSecond);
        client.send(metrics);

        try {
          Thread.sleep(1_000);
        } catch (InterruptedException ignored) {
        }
      }
    });
  }

  private static Metric[] generateMetrics(int size) {
    Metric[] metrics = new Metric[size];
    for (int i = 0; i < size; i++) {
      metrics[i] = new Counter("foo", 1);
    }
    return metrics;
  }
}
