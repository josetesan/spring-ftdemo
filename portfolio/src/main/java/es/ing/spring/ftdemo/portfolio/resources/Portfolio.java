package es.ing.spring.ftdemo.portfolio.resources;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"size", "totalPrice", "portfolio"})
public class Portfolio {
  public int size;
  public Integer totalPrice;
  public List<StockPrice> portfolio;

  public Portfolio(int size, Integer totalPrice, List<StockPrice> portfolio) {
    this.size = size;
    this.totalPrice = totalPrice;
    this.portfolio = portfolio;
  }
}
