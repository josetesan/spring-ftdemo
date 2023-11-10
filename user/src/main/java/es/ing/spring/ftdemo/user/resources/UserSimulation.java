package es.ing.spring.ftdemo.user.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UserSimulation {
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final ObjectMapper objectMapper;

  private PortfolioClientExchange portfolioClientExchange;
  private final String user;

  private long requestDuration;
  private String error;
  private int portfolioSize;
  private Integer portfolioValue;

  public UserSimulation(
      ObjectMapper objectMapper, PortfolioClientExchange portfolioClientExchange, String user) {
    this.portfolioClientExchange = portfolioClientExchange;
    this.user = user;
    this.objectMapper = objectMapper;
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
    String errorMessage = null;
    try {
      portfolio = portfolioClientExchange.findByUser(user);
    } catch (Exception e) {
      errorMessage = parseException(e);
    }
    long end = System.currentTimeMillis();

    synchronized (this) {
      requestDuration = end - start;
      error = errorMessage == null ? null : errorMessage;
      portfolioSize = portfolio == null ? -1 : portfolio.size;
      portfolioValue = portfolio == null ? null : portfolio.totalPrice;
    }

    executor.schedule(this::update, 200, TimeUnit.MILLISECONDS);
  }

  private String parseException(Exception e) {
    try {
      var start =
          Arrays.stream(e.getMessage().split(":"))
              .skip(1)
              .collect(Collectors.joining(":"))
              .substring(2);
      var end = start.substring(0, start.length() - 1);
      return objectMapper.readTree(end).get("message").asText();
    } catch (Exception f) {
      return e.getMessage();
    }
  }
}
