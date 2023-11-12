package es.ing.spring.ftdemo.portfolio.resources;

import io.github.resilience4j.retry.annotation.Retry;
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

  //  @Bulkhead(name = "bulk", type = Bulkhead.Type.THREADPOOL)
  //  @CircuitBreaker(name = "circuit")
  //      @Retry(name = "retry", fallbackMethod = "getPriceFallback")
  @Retry(name = "retry")
  //    @TimeLimiter(name = "limiter", fallbackMethod = "getPriceFallback")
  //  @TimeLimiter(name = "limiter")
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
