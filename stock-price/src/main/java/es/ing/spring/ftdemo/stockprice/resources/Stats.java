package es.ing.spring.ftdemo.stockprice.resources;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"currentInFlightRequests", "timestamps", "totalCounters", "failuresCounters"})
public class Stats {
  public int currentInFlightRequests;
  public Long[] timestamps;
  public Integer[] totalCounters;
  public Integer[] failuresCounters;

  public Stats(
      int currentInFlightRequests,
      Long[] timestamps,
      Integer[] totalCounters,
      Integer[] failuresCounters) {
    this.currentInFlightRequests = currentInFlightRequests;
    this.timestamps = timestamps;
    this.totalCounters = totalCounters;
    this.failuresCounters = failuresCounters;
  }
}
