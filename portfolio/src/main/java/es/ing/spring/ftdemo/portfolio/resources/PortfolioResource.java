package es.ing.spring.ftdemo.portfolio.resources;

import io.github.resilience4j.timelimiter.TimeLimiter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioResource {
  private final ConcurrentMap<String, List<String>> portfolios =
      new ConcurrentHashMap<>(); // tickers by user

  StockPriceService service;

  StatsResource stats;

  private final TimeLimiter timeLimiter;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

  public PortfolioResource(StockPriceService service, StatsResource stats) {
    this.service = service;
    this.stats = stats;
    this.timeLimiter = TimeLimiter.ofDefaults("limiter");
  }

  @GetMapping("/{user}")
  public CompletableFuture<Portfolio> get(@PathVariable("user") String user) {
    return getPortfolioContent(user).thenApply(this::getStockPrices).thenApply(this::fillPortfolio);
  }

  private CompletableFuture<List<String>> getPortfolioContent(final String user) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return portfolios.computeIfAbsent(
                user,
                ignored -> {
                  List<String> result = new ArrayList<>();
                  int portfolioSize = ThreadLocalRandom.current().nextInt(5, 10);
                  for (int i = 0; i < portfolioSize; i++) {
                    result.add(generateTicker());
                  }
                  return result;
                });
          } finally {

            //        Set<String> allTickers = new HashSet<>();
            //        for (List<String> portfolio : portfolios.values()) {
            //          allTickers.addAll(portfolio);
            //        }
            Set<String> allTickers =
                portfolios.values().stream().toList().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
            stats.setAllTickersCount(allTickers.size());
          }
        });
  }

  private List<CompletableFuture<StockPrice>> getStockPrices(List<String> tickets) {
    return tickets.stream().map(ticket -> service.getPrice(ticket)).toList();
  }

  private Portfolio fillPortfolio(List<CompletableFuture<StockPrice>> portfolioData) {

    Integer totalPrice =
        portfolioData.stream()
            .map(CompletableFuture::join)
            .map(stockPrice -> stockPrice.price)
            .filter(Objects::nonNull)
            .reduce(0, (Integer::sum));
    List<StockPrice> data = portfolioData.stream().map(CompletableFuture::join).toList();
    return new Portfolio(portfolioData.size(), totalPrice, data);
  }

  @DeleteMapping("/cache")
  public void deleteCache() {
    service.cleanCache();
  }

  /*
    private CompletionStage<List<String>> getPortfolioContent(String user) {
      try {
        return portfolios.computeIfAbsent(
            user,
            ignored -> {
              List<String> result = new ArrayList<>();
              int portfolioSize = ThreadLocalRandom.current().nextInt(5, 10);
              for (int i = 0; i < portfolioSize; i++) {
                result.add(generateTicker());
              }
              return result;
            });
      } finally {
        Set<String> allTickers = new HashSet<>();
        for (List<String> portfolio : portfolios.values()) {
          allTickers.addAll(portfolio);
        }
        stats.setAllTickersCount(allTickers.size());
      }
    }


    private CompletableFuture<List<String>> getPortfolioContent(String user) {
      try {
        return portfolios.computeIfAbsent(
            user,
            ignored -> {
              List<String> result = new ArrayList<>();
              int portfolioSize = ThreadLocalRandom.current().nextInt(5, 10);
              for (int i = 0; i < portfolioSize; i++) {
                result.add(generateTicker());
              }
              return result;
            });
      } finally {
        Set<String> allTickers = new HashSet<>();
        for (List<String> portfolio : portfolios.values()) {
          allTickers.addAll(portfolio);
        }
        stats.setAllTickersCount(allTickers.size());
      }
    }
    private CompletableFuture<List<StockPrice>> getStockPrices(List<String> tickers) {
      return tickers.stream()
          .map(ticker ->  service.getPrice(ticker));

    }

    private CompletableFuture<Portfolio> fillPortfolio(List<StockPrice> portfolioData) {
      Integer totalPrice = 0;
      for (StockPrice stockPrice : portfolioData) {
        if (stockPrice == null || stockPrice.price == null) {
          totalPrice = null;
          break;
        }
        totalPrice += stockPrice.price;
      }
      return CompletableFuture.completedFuture(new Portfolio(portfolioData.size(), totalPrice, portfolioData));
    }
  */
  private String generateTicker() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      char c = (char) ('A' + ThreadLocalRandom.current().nextInt(0, 26));
      result.append(c);
    }
    return result.toString();
  }
}
