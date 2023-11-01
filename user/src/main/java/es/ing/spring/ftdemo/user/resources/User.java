package es.ing.spring.ftdemo.user.resources;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"user", "requestDuration", "error", "portfolioSize", "portfolioValue"})
public class User {
  public String user;
  public String requestDuration;
  public String error;
  public int portfolioSize;
  public Integer portfolioValue;

  public User(
      String user,
      String requestDuration,
      String error,
      int portfolioSize,
      Integer portfolioValue) {
    this.user = user;
    this.requestDuration = requestDuration;
    this.error = error;
    this.portfolioSize = portfolioSize;
    this.portfolioValue = portfolioValue;
  }
}
