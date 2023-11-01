package es.ing.spring.ftdemo.stockprice.resources;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
public class StockPriceResource {
  private static final int FAST = 10;
  private static final int SLOW = 20;

  private final ConcurrentMap<String, Integer> prices;
  private final StatsResource stats;

  public StockPriceResource(StatsResource stats) {
    this.stats = stats;
    this.prices = new ConcurrentHashMap<>();
  }

  @GetMapping("/stock/{ticker}")
  public StockPrice get(@PathVariable("ticker") String ticker) {
    return stats.request(
        currentInFlightRequests -> {
          if (currentInFlightRequests <= FAST) {
            stats.recordOk();
            randomSleep(5, 15);
            return priceOf(ticker);
          } else if (currentInFlightRequests <= SLOW) {
            stats.recordOk();
            randomSleep(100, 1000);
            return priceOf(ticker);
          } else {
            int randomOutcome = ThreadLocalRandom.current().nextInt(10);
            if (randomOutcome < 3) {
              stats.recordOk();
              randomSleep(500, 5000);
              return priceOf(ticker);
            } else if (randomOutcome < 6) {
              stats.recordKo();
              randomSleep(0, 5000);
              throw new InternalServerErrorException();
            } else if (randomOutcome < 7) {
              stats.recordKo();
              randomSleep(0, 5000);
              throw new BadRequestException();
            } else {
              stats.recordKo();
              randomSleep(0, 5000);
              throw new WebApplicationException(
                  Response.ok("invalid JSON", MediaType.APPLICATION_JSON_TYPE).build());
            }
          }
        });
  }

  private void randomSleep(long low, long high) {
    long sleepInMillis = ThreadLocalRandom.current().nextLong(low, high + 1);
    try {
      Thread.sleep(sleepInMillis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private StockPrice priceOf(String ticker) {
    int price =
        prices.merge(
            ticker,
            ThreadLocalRandom.current().nextInt(1000),
            (currentPrice, ignored) ->
                nonnegative(currentPrice + ThreadLocalRandom.current().nextInt(-10, 11)));
    return new StockPrice(ticker, price);
  }

  private static int nonnegative(int n) {
    return Math.max(0, n);
  }
}
