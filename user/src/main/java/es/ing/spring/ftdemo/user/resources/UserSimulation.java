package es.ing.spring.ftdemo.user.resources;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.web.client.RestClient;

public class UserSimulation {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private RestClient client;
  private final String user;

  private long requestDuration;
  private String error;
  private int portfolioSize;
  private Integer portfolioValue;

  public UserSimulation(String user) {
    this.user = user;
    this.client = RestClient.create();
    executor.submit(this::update);
  }

  public synchronized User toUser() {
    Duration d = Duration.ofMillis(requestDuration);
    String duration = d.toMillis() + " ms";
    return new User(user, duration, error, portfolioSize, portfolioValue);
  }

  public void destroy() {
    executor.shutdown();
  }

  private void update() {
    long start = System.currentTimeMillis();
    Portfolio portfolio = null;
    Exception exception = null;
    try {
      portfolio =
          client
              .get()
              .uri("http://localhost:7070/portfolio/{user}", user)
              .accept(APPLICATION_JSON)
              .retrieve()
              .body(Portfolio.class);
    } catch (Exception e) {
      exception = e;
    }
    long end = System.currentTimeMillis();

    synchronized (this) {
      requestDuration = end - start;
      error = exception == null ? null : exception.getMessage();
      portfolioSize = portfolio == null ? -1 : portfolio.size;
      portfolioValue = portfolio == null ? null : portfolio.totalPrice;
    }

    executor.schedule(this::update, 200, TimeUnit.MILLISECONDS);
  }
}
