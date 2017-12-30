package rs.nicktrave.statsd.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;
import rs.nicktrave.statsd.client.Collector;
import rs.nicktrave.statsd.client.StatsdClient;
import rs.nicktrave.statsd.client.Transport;
import rs.nicktrave.statsd.client.UnbufferedCollector;
import rs.nicktrave.statsd.client.netty.NettyUdpTransport;
import rs.nicktrave.statsd.client.nio.NioUdpTransport;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

public class ExampleClient {

  private static final int RUN_TIME_SECONDS = 30;
  private static final int METRICS_PER_SECOND = 500;
  private static final Random random = new Random();

  private void run() throws IOException, InterruptedException {
    System.out.println("Starting server ...");
    Transport t = new NettyUdpTransport(new InetSocketAddress(InetAddress.getLocalHost(), 8125));
    //Transport t = new NioUdpTransport(new InetSocketAddress(InetAddress.getLocalHost(), 8125));
    Collector c = new UnbufferedCollector(t);
    StatsdClient client = StatsdClient.newBuilder()
        .withTransport(t)
        .withCollector(c)
        .build();

    System.out.println(String.format("Sending a rate of %d metrics per second for %d seconds",
        METRICS_PER_SECOND, RUN_TIME_SECONDS));

    Thread thread1 = newSendThread(client);
    thread1.start();

    //Thread thread2 = newSendThread(client);
    //thread2.start();

    thread1.join();
    //thread2.join();

    System.out.println("Shutting down ...");
    client.close();
  }

  public static void main(String ...args) throws IOException, InterruptedException {
    new ExampleClient().run();
  }

  private static Thread newSendThread(StatsdClient client) throws InterruptedException {
    return new Thread(() -> {
      Metric[] metrics;
      for (int i = 0; i < RUN_TIME_SECONDS; i++) {
        metrics = generateMetrics(METRICS_PER_SECOND);
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
      //metrics[i] = new Counter("foo", random.nextInt(100));
      metrics[i] = new Counter("foo", 1);
    }
    return metrics;
  }
}
