package es.ing.spring.ftdemo.user.resources;


import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserSimulation {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private PortfolioClientExchange portfolioClientExchange;
  private final String user;

  private long requestDuration;
  private String error;
  private int portfolioSize;
  private Integer portfolioValue;

  public UserSimulation(PortfolioClientExchange portfolioClientExchange,String user) {
    this.portfolioClientExchange = portfolioClientExchange;
    this.user = user;
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
      portfolio = portfolioClientExchange.findByUser(user);
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
