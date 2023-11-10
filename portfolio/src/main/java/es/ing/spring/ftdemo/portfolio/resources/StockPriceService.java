package es.ing.spring.ftdemo.portfolio.resources;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class StockPriceService {
  StockPriceClientExchange stockPriceClientExchange;
  StatsResource stats;

  public StockPriceService(StockPriceClientExchange stockPriceClientExchange, StatsResource stats) {
    this.stockPriceClientExchange = stockPriceClientExchange;
    this.stats = stats;
  }

  private final ConcurrentMap<String, StockPrice> cache = new ConcurrentHashMap<>();

  //    @Bulkhead(name = "bulk", type = Bulkhead.Type.THREADPOOL)
  //          @CircuitBreaker(name = "circuit")
  //    @Retry(name = "retry", fallbackMethod = "getPriceFallback")
  @TimeLimiter(name = "limiter")
  CompletableFuture<StockPrice> getPrice(String ticker) {
    return CompletableFuture.supplyAsync(
        () -> {
          StockPrice result = stockPriceClientExchange.findById(ticker);
          cache.put(ticker, result);
          stats.recordNormal();
          stats.setCacheSize(cache.size());
          return result;
        });
  }

  public CompletableFuture<StockPrice> getPriceFallback(String ticker) {
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
