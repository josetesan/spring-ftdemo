package es.ing.spring.ftdemo.portfolio.resources;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class StockPriceService {
  StockPriceClient client;
  StatsResource stats;


  public StockPriceService(StockPriceClient client, StatsResource stats) {
    this.client = client;
    this.stats = stats;
  }

  private final ConcurrentMap<String, StockPrice> cache = new ConcurrentHashMap<>();

  //    @Fallback(fallbackMethod = "getPriceFallback")
  //    @Timeout
  //    @CircuitBreaker(skipOn = BulkheadException.class)
  //    @Bulkhead
  //    @Retry
  public StockPrice getPrice(String ticker) {
    StockPrice result = client.get(ticker);
    cache.put(ticker, result);
    stats.recordNormal();
    stats.setCacheSize(cache.size());
    return result;
  }

  private StockPrice getPriceFallback(String ticker) {
    stats.recordCached();
    return cache.getOrDefault(ticker, new StockPrice(ticker, null));
  }
}
