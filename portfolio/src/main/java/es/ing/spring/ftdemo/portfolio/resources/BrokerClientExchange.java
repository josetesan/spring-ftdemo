package es.ing.spring.ftdemo.portfolio.resources;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface BrokerClientExchange {

  @GetExchange("/stocks/stock/{ticker}")
  StockPrice findById(@PathVariable("ticker") String ticker);
}
