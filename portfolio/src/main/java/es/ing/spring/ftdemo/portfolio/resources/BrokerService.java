package es.ing.spring.ftdemo.portfolio.resources;

import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class BrokerService {
  ExecutorService scheduler;
  BrokerClientExchange brokerClientExchange;
  StatsController stats;

  public BrokerService(BrokerClientExchange brokerClientExchange, StatsController stats) {
    this.brokerClientExchange = brokerClientExchange;
    this.stats = stats;
    this.scheduler = Executors.newCachedThreadPool();
  }

  private final ConcurrentMap<String, StockPrice> cache = new ConcurrentHashMap<>();

  // Retry ( CircuitBreaker ( RateLimiter ( TimeLimiter ( Bulkhead ( Function
  @Retry(name = "retry", fallbackMethod = "getPriceFallback")
  @TimeLimiter(name = "limiter")
  // @CircuitBreaker(name = "circuitbreaker")
  // @Bulkhead(name = "bulkhead", type = Bulkhead.Type.THREADPOOL)
  public CompletableFuture<StockPrice> getPrice(String ticker) {
    return CompletableFuture.supplyAsync(
        () -> {
          StockPrice result = brokerClientExchange.findById(ticker);
          cache.put(ticker, result);
          stats.recordNormal();
          stats.setCacheSize(cache.size());
          return result;
        },
        scheduler);
  }

  public CompletableFuture<StockPrice> getPriceFallback(String ticker, Throwable t) {
    return CompletableFuture.supplyAsync(
        () -> {
          stats.recordCached();
          return cache.getOrDefault(ticker, new StockPrice(ticker, null));
        });
  }
}
