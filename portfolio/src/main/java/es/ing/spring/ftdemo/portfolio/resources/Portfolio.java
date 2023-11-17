package es.ing.spring.ftdemo.portfolio.resources;

import java.util.List;

public record Portfolio(int size, Integer totalPrice, List<StockPrice> portfolio) {}
