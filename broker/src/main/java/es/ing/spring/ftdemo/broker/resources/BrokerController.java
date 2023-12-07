package es.ing.spring.ftdemo.broker.resources;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
public class BrokerController {
  private static final int FAST = 15;
  private static final int SLOW = 25;
  private static final int OK_THRESHOLD = 3;
  private static final int INTERNAL_ERROR_THRESHOLD = 6;
  private static final int BAD_REQUEST_THRESHOLD = 7;

  private final ConcurrentMap<String, Integer> prices;
  private final StatsController stats;

  public BrokerController(StatsController stats) {
    this.stats = stats;
    this.prices = new ConcurrentHashMap<>();
  }

  @GetMapping("/stock/{ticker}")
  public StockPrice get(@PathVariable String ticker) {
    return stats.request(
        currentInFlightRequests ->
            getResponseBasedOnFlightRequests(currentInFlightRequests, ticker));
  }

  private StockPrice getResponseBasedOnFlightRequests(int currentInFlightRequests, String ticker) {
    if (currentInFlightRequests <= FAST) {
      return getFastResponse(ticker);
    }

    if (currentInFlightRequests <= SLOW) {
      return getSlowResponse(ticker);
    }

    return getRandomizedResponse(ticker);
  }

  private StockPrice getFastResponse(String ticker) {
    recordAndSleepOk(5, 15);
    return priceOf(ticker);
  }

  private StockPrice getSlowResponse(String ticker) {
    recordAndSleepOk(100, 1000);
    return priceOf(ticker);
  }

  private StockPrice getRandomizedResponse(String ticker) {
    int randomOutcome = ThreadLocalRandom.current().nextInt(10);
    if (randomOutcome < OK_THRESHOLD) {
      recordAndSleepOk(500, 5000);
      return priceOf(ticker);
    }

    if (randomOutcome < INTERNAL_ERROR_THRESHOLD) {
      recordAndSleepKo(0, 5000);
      throw new InternalServerErrorException();
    }

    if (randomOutcome < BAD_REQUEST_THRESHOLD) {
      recordAndSleepKo(0, 5000);
      throw new BadRequestException();
    }

    recordAndSleepKo(0, 5000);
    throw new OkException();
  }

  private void recordAndSleepOk(int minSleepTime, int maxSleepTime) {
    stats.recordOk();
    randomSleep(minSleepTime, maxSleepTime);
  }

  private void recordAndSleepKo(int minSleepTime, int maxSleepTime) {
    stats.recordKo();
    randomSleep(minSleepTime, maxSleepTime);
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

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  static class BadRequestException extends RuntimeException {

    public BadRequestException() {
      super();
    }
  }

  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  static class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException() {
      super();
    }
  }

  @ResponseStatus(value = HttpStatus.OK)
  static class OkException extends RuntimeException {

    public OkException() {
      super();
    }
  }
}
