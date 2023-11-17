package es.ing.spring.ftdemo.portfolio.resources;

public record Stats(
    int allTickersCount,
    int cacheSize,
    Long[] timestamps,
    Integer[] totalCounters,
    Integer[] cachedCounters) {}
