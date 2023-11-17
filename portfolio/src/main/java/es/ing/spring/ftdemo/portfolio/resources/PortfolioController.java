package es.ing.spring.ftdemo.portfolio.resources;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {
  private final ConcurrentMap<String, List<String>> portfolios =
      new ConcurrentHashMap<>(); // tickers by user

  BrokerService service;

  StatsController stats;

  public PortfolioController(BrokerService service, StatsController stats) {
    this.service = service;
    this.stats = stats;
  }

  @GetMapping("/{user}")
  public CompletableFuture<Portfolio> get(@PathVariable("user") String user) {
    return getOrCreatePortfolioContent(user)
        .thenApply(this::getStockPrices)
        .thenApply(this::fillPortfolio);
  }

  private CompletableFuture<List<String>> getOrCreatePortfolioContent(final String user) {
    final int portfolioSize = ThreadLocalRandom.current().nextInt(5, 10);
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return portfolios.computeIfAbsent(
                user,
                ignored ->
                    IntStream.range(0, portfolioSize)
                        .mapToObj(_ignored -> generateTicker())
                        .toList());
          } finally {
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

    List<StockPrice> data = portfolioData.stream().map(CompletableFuture::join).toList();
    Integer totalPrice =
        data.stream().map(StockPrice::price).filter(Objects::nonNull).reduce(0, (Integer::sum));
    return new Portfolio(portfolioData.size(), totalPrice, data);
  }

  private String generateTicker() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      char c = (char) ('A' + ThreadLocalRandom.current().nextInt(0, 26));
      result.append(c);
    }
    return result.toString();
  }
}
