package es.ing.spring.ftdemo.portfolio.resources;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
  "allTickersCount",
  "cacheSize",
  "timestamps",
  "totalCounters",
  "cachedCounters"
})
public class Stats {
  public int allTickersCount;
  public int cacheSize;
  public Long[] timestamps;
  public Integer[] totalCounters;
  public Integer[] cachedCounters;

  public Stats(
      int allTickersCount,
      int cacheSize,
      Long[] timestamps,
      Integer[] totalCounters,
      Integer[] cachedCounters) {
    this.allTickersCount = allTickersCount;
    this.cacheSize = cacheSize;
    this.timestamps = timestamps;
    this.totalCounters = totalCounters;
    this.cachedCounters = cachedCounters;
  }
}
