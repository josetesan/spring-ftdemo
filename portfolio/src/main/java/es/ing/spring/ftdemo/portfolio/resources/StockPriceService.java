package es.ing.spring.ftdemo.portfolio.resources;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StockPriceService {
  StockPriceClientExchange stockPriceClientExchange;
  StatsResource stats;



  private static final Logger LOGGER = LoggerFactory.getLogger(StockPriceService.class);

  public StockPriceService(StockPriceClientExchange stockPriceClientExchange, StatsResource stats) {
    this.stockPriceClientExchange = stockPriceClientExchange;
    this.stats = stats;
  }

  private final ConcurrentMap<String, StockPrice> cache = new ConcurrentHashMap<>();

  @Bulkhead(name = "bulk", type = Bulkhead.Type.SEMAPHORE)
//        @CircuitBreaker(name = "circuit")
  //  @Retry(name = "retry", fallbackMethod = "getPriceFallback")
  StockPrice getPrice(String ticker) {
    StockPrice result = stockPriceClientExchange.findById(ticker);
    cache.put(ticker, result);
    stats.recordNormal();
    stats.setCacheSize(cache.size());
    return result;
  }

  public StockPrice getPriceFallback(String ticker, Throwable throwable) {
    stats.recordCached();
    return cache.getOrDefault(ticker, new StockPrice(ticker, null));
  }

  void cleanCache() {
    cache.clear();
  }
}
