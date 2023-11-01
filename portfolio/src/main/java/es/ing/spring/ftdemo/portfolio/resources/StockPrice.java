package es.ing.spring.ftdemo.portfolio.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"ticker", "price"})
public class StockPrice {
  public String ticker;
  public Integer price;

  @JsonCreator
  public StockPrice(@JsonProperty("ticker") String ticker, @JsonProperty("price") Integer price) {
    this.ticker = ticker;
    this.price = price;
  }
}
