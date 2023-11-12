package es.ing.spring.ftdemo.user.resources;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.web.client.HttpServerErrorException;

public class UserSimulation {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private final PortfolioClientExchange portfolioClientExchange;
  private final String user;

  private long requestDuration;
  private String error;
  private int portfolioSize;
  private Integer portfolioValue;

  public UserSimulation(PortfolioClientExchange portfolioClientExchange, String user) {
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
    String message = null;
    try {
      portfolio = portfolioClientExchange.findByUser(user);
    } catch (HttpServerErrorException s) {
      message = parseException(s);
    } catch (Throwable t) {
      message = t.getMessage();
    }
    long end = System.currentTimeMillis();

    synchronized (this) {
      requestDuration = end - start;
      error = message;
      portfolioSize = portfolio == null ? -1 : portfolio.size;
      portfolioValue = portfolio == null ? null : portfolio.totalPrice;
    }

    executor.schedule(this::update, 200, TimeUnit.MILLISECONDS);
  }

  private static String parseException(HttpServerErrorException s) {
    String message = null;
    try {
      message =
          ((Map<String, String>) s.getResponseBodyAs(Map.class))
              .getOrDefault("message", "Internal Server Error");
    } catch (Exception e) {
      message = s.getMessage();
    }
    return message;
  }
}
