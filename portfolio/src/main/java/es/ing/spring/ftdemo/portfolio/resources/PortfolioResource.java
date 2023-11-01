package es.ing.spring.ftdemo.portfolio.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioResource {
  private final ConcurrentMap<String, List<String>> portfolios =
      new ConcurrentHashMap<>(); // tickers by user

  private final ExecutorService executor = Executors.newCachedThreadPool();

  StockPriceService service;

  StatsResource stats;

  @GetMapping("/{user}")
  public Portfolio get(@PathVariable("user") String user)
      throws ExecutionException, InterruptedException {
    List<String> tickers = getPortfolioContent(user);
    List<StockPrice> portfolioData = getStockPrices(tickers);
    return fillPortfolio(portfolioData);
  }

  private List<String> getPortfolioContent(String user) {
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

  private List<StockPrice> getStockPrices(List<String> tickers)
      throws InterruptedException, ExecutionException {
    List<Future<StockPrice>> futures = new ArrayList<>();
    for (String ticker : tickers) {
      futures.add(executor.submit(() -> service.getPrice(ticker)));
    }
    List<StockPrice> portfolioData = new ArrayList<>(futures.size());
    for (Future<StockPrice> future : futures) {
      portfolioData.add(future.get());
    }
    return portfolioData;
  }

  private Portfolio fillPortfolio(List<StockPrice> portfolioData) {
    Integer totalPrice = 0;
    for (StockPrice stockPrice : portfolioData) {
      if (stockPrice == null || stockPrice.price == null) {
        totalPrice = null;
        break;
      }
      totalPrice += stockPrice.price;
    }
    return new Portfolio(portfolioData.size(), totalPrice, portfolioData);
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
