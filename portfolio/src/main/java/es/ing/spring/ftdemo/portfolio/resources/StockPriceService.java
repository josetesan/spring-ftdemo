package es.ing.spring.ftdemo.portfolio.resources;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StockPriceService {
  RestClient client;
  StatsResource stats;

  private static final Logger LOGGER = LoggerFactory.getLogger(StockPriceService.class);

  public StockPriceService(StatsResource stats) {
    this.client = RestClient.create();
    this.stats = stats;
  }

  private final ConcurrentMap<String, StockPrice> cache = new ConcurrentHashMap<>();

  @TimeLimiter(name = "timelimiter", fallbackMethod = "getPriceFallback")
  @CircuitBreaker(name = "circuit")
  @Bulkhead(name = "bulk")
  @Retry(name = "retry")
  public StockPrice getPrice(String ticker) {
    StockPrice result =
        client
            .get()
            .uri("http://localhost:5050/stocks/stock/{ticker}", ticker)
            .accept(APPLICATION_JSON)
            .retrieve()
            .body(StockPrice.class);
    cache.put(ticker, result);
    stats.recordNormal();
    stats.setCacheSize(cache.size());
    return result;
  }

  private StockPrice getPriceFallback(String ticker) {
    LOGGER.warn("Using fallBack for {} price", ticker);
    stats.recordCached();
    return cache.getOrDefault(ticker, new StockPrice(ticker, null));
  }
}
