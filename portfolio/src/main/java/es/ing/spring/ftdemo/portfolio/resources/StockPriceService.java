package es.ing.spring.ftdemo.portfolio.resources;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

  //  @TimeLimiter(name = "timelimiter", fallbackMethod = "getPriceFallback")
    @CircuitBreaker(name = "circuit")
  //    @Bulkhead(name = "bulk")
  //  @Retry(name = "retry")
  public StockPrice getPrice(String ticker) {
    StockPrice result = stockPriceClientExchange.findById(ticker);
    cache.put(ticker, result);
    stats.recordNormal();
    stats.setCacheSize(cache.size());
    return result;
  }

  public StockPrice getPriceFallback(String ticker, Exception e) {
    stats.recordCached();
    return cache.getOrDefault(ticker, new StockPrice(ticker, null));
  }
}
