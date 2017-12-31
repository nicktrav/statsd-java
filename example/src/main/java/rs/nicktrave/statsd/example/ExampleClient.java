package rs.nicktrave.statsd.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import rs.nicktrave.statsd.client.Collector;
import rs.nicktrave.statsd.client.StatsdClient;
import rs.nicktrave.statsd.client.Transport;
import rs.nicktrave.statsd.client.UnbufferedCollector;
import rs.nicktrave.statsd.client.netty.NettyUdpTransport;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

public class ExampleClient {

  private static final int PORT = 8125;

  private void run(String hostname, int runTimeSeconds, int metricsPerSecond, int numClients)
      throws IOException, InterruptedException {

    System.out.println("Starting server ...");

    List<StatsdClient> clients = new ArrayList<>();
    for (int i = 0; i < numClients; i++) {
      clients.add(newClient(hostname));
    }

    Thread thread1 = newSendThread(clients, runTimeSeconds, metricsPerSecond);
    thread1.start();
    thread1.join();

    System.out.println("Shutting down ...");
    for (int i = 0; i < numClients; i++) {
      clients.get(i).close();
    }
  }

  public static void main(String ...args) throws IOException, InterruptedException {
    // Parse command line args
    String hostname = args[0];
    int metricsPerSecond = Integer.valueOf(args[1]);
    int runTimeSeconds = Integer.valueOf(args[2]);
    int numClients = Integer.valueOf(args[3]);

    System.out.println(String.format("QPS: %d, Runtime: %ds, Clients: %d",
        metricsPerSecond, runTimeSeconds, numClients));

    new ExampleClient().run(hostname, runTimeSeconds, metricsPerSecond, numClients);
  }

  private static StatsdClient newClient(String hostname) throws InterruptedException {
    Transport t = new NettyUdpTransport(new InetSocketAddress(hostname, PORT));
    Collector c = new UnbufferedCollector(t);
    return StatsdClient.newBuilder()
        .withTransport(t)
        .withCollector(c)
        .build();
  }

  private static Thread newSendThread(List<StatsdClient> clients, int runTime, int metricsPerSecond)
      throws InterruptedException {
    return new Thread(() -> {
      Metric[] metrics;
      for (int i = 0; i < runTime; i++) {
        metrics = generateMetrics(metricsPerSecond);

        clients.forEach(c -> c.send(metrics));

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
