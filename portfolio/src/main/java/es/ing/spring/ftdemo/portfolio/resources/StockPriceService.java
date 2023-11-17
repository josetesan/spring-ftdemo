package es.ing.spring.ftdemo.portfolio.resources;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class StockPriceService {
  ExecutorService scheduler;
  StockPriceClientExchange stockPriceClientExchange;
  StatsResource stats;

  public StockPriceService(StockPriceClientExchange stockPriceClientExchange, StatsResource stats) {
    this.stockPriceClientExchange = stockPriceClientExchange;
    this.stats = stats;
    this.scheduler = Executors.newCachedThreadPool();
  }

  private final ConcurrentMap<String, StockPrice> cache = new ConcurrentHashMap<>();

  // Retry ( CircuitBreaker ( RateLimiter ( TimeLimiter ( Bulkhead ( Function
  @Retry(name = "retry")
  @TimeLimiter(name = "limiter")
  @CircuitBreaker(name = "circuitbreaker")
  @Bulkhead(name = "bulkhead", type = Bulkhead.Type.THREADPOOL)
  public CompletableFuture<StockPrice> getPrice(String ticker) {
    return CompletableFuture.supplyAsync(
        () -> {
          StockPrice result = stockPriceClientExchange.findById(ticker);
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

  void cleanCache() {
    cache.clear();
  }
}
