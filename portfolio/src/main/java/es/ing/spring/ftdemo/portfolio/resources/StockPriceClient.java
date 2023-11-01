package es.ing.spring.ftdemo.portfolio.resources;

import com.maciejwalkowiak.spring.http.annotation.HttpClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

@HttpClient("stock-price-client")
public interface StockPriceClient {
  @GetExchange("/stocks/stock/{ticker}")
  StockPrice get(@PathVariable("ticker") String ticker);
}
