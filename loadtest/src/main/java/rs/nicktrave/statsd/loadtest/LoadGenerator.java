package rs.nicktrave.statsd.loadtest;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import rs.nicktrave.statsd.client.Collector;
import rs.nicktrave.statsd.client.StatsdClient;
import rs.nicktrave.statsd.client.Transport;
import rs.nicktrave.statsd.client.UnbufferedCollector;
import rs.nicktrave.statsd.client.netty.NettyUdpTransport;
import rs.nicktrave.statsd.common.Counter;
import rs.nicktrave.statsd.common.Metric;

/**
 * A tool used to generate {@link Metric}s which are sent to a server.
 */
// TODO(nickt): Proper logging
public class LoadGenerator {

  private static final class Args {

    @Parameter(
        names = "-serverHostname",
        description ="The hostname of the server the client will connect to")
    private String serverHostname = "localhost";

    @Parameter(
        names = "-port",
        description ="The port of the server the client will connect to")
    private int serverPort = 8125;

    @Parameter(
        names = "-numClients",
        description = "The number of distinct clients that will be used to send metrics")
    private int numClients = 1;

    @Parameter(
        names = "-batchSize",
        description = "The number of metrics to send per client in each batch")
    private int batchSize = 1;

    @Parameter(
        names = "-iterations",
        description = "The number of iterations through the send loop")
    private int iterations = 5;

    @Parameter(
        names = "-delay",
        description = "The amount of time (in milliseconds) between each send loop run")
    private int delay = 1_000;


    @Parameter(names = "--help", help = true)
    private boolean help;
  }

  private void run(Args args) throws InterruptedException, IOException {
    System.out.println("Starting server ...");

    // Generate the collection of clients
    List<StatsdClient> clients = new ArrayList<>();
    for (int i = 0; i < args.numClients; i++) {
      clients.add(newClient(args.serverHostname, args.serverPort));
    }

    AtomicInteger count = new AtomicInteger();
    AtomicBoolean isFinished = new AtomicBoolean(false);
    CountDownLatch doneLatch = new CountDownLatch(1);

    Metric[] metrics = generateMetrics(args.batchSize);
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    service.scheduleWithFixedDelay(() -> {
          if (isFinished.get()) {
            return;
          }

          if (count.getAndIncrement() == args.iterations) {
            isFinished.set(true);
            doneLatch.countDown();
            return;
          }

          System.out.println("Iteration " + count.get() + " of " + args.iterations);
          clients.forEach(c -> c.send(metrics));
        }, 1_000, args.delay, TimeUnit.MILLISECONDS);


    System.out.println("Awaiting shutdown ...");
    doneLatch.await();

    System.out.println("Shutting down ...");
    service.shutdown();
    for (StatsdClient client : clients) {
      client.close();
    }
  }

  public static void main(String ...args) throws IOException, InterruptedException {
    Args cliArgs = new Args();
    JCommander jCommander = JCommander.newBuilder()
        .addObject(cliArgs)
        .build();
    jCommander.parse(args);

    if (cliArgs.help) {
      jCommander.usage();
      return;
    }

    System.out.println("Starting load test:");
    System.out.println("\tServer: " + cliArgs.serverHostname + ":" + cliArgs.serverPort);
    System.out.println("\tClients: " + cliArgs.numClients);
    System.out.println("\tBatch size: " + cliArgs.batchSize);
    System.out.println("\tIterations: " + cliArgs.iterations);
    System.out.println("\tDelay: " + cliArgs.delay + "ms");

    new LoadGenerator().run(cliArgs);
  }

  private static StatsdClient newClient(String hostname, int port) throws InterruptedException {
    Transport t = new NettyUdpTransport(new InetSocketAddress(hostname, port));
    Collector c = new UnbufferedCollector(t);
    return StatsdClient.newBuilder()
        .withTransport(t)
        .withCollector(c)
        .build();
  }

  private static Metric[] generateMetrics(int size) {
    Metric[] metrics = new Metric[size];
    for (int i = 0; i < size; i++) {
      metrics[i] = new Counter("foo", 1);
    }
    return metrics;
  }
}
