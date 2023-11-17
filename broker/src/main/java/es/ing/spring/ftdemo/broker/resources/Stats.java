package es.ing.spring.ftdemo.broker.resources;

public record Stats(
    int currentInFlightRequests,
    Long[] timestamps,
    Integer[] totalCounters,
    Integer[] failuresCounters) {}
