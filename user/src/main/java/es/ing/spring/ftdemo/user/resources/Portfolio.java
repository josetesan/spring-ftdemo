package es.ing.spring.ftdemo.user.resources;

import java.util.List;

public record Portfolio(int size, Integer totalPrice, List<StockPrice> portfolio) {}
