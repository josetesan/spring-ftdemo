package es.ing.spring.ftdemo.portfolio.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio/stats")
public class StatsController {
  private final AtomicInteger allTickersCount;
  private final AtomicInteger cacheSize;
  private final List<Long> timestamps;
  private final List<Integer> totalCounters;
  private final List<Integer> cachedCounters;

  public StatsController() {
    cacheSize = new AtomicInteger(0);
    allTickersCount = new AtomicInteger(0);
    timestamps = new ArrayList<>(List.of(0L));
    totalCounters = new ArrayList<>(List.of(0));
    cachedCounters = new ArrayList<>(List.of(0));
  }

  private long lastFullSecond = System.currentTimeMillis();

  public void setAllTickersCount(int value) {
    allTickersCount.set(value);
  }

  public void setCacheSize(int value) {
    cacheSize.set(value);
  }

  public void recordNormal() {
    record(true, false);
  }

  public void recordCached() {
    record(true, true);
  }

  private synchronized void record(boolean incrementTotals, boolean incrementCached) {
    long now = System.currentTimeMillis();
    long diff = now - lastFullSecond;

    while (diff > 1000) {
      long previous = timestamps.get(timestamps.size() - 1);
      timestamps.add(previous + 1);

      totalCounters.add(0);
      cachedCounters.add(0);

      diff -= 1000;
      lastFullSecond += 1000;
    }

    if (incrementTotals) {
      int index = totalCounters.size() - 1;
      totalCounters.set(index, totalCounters.get(index) + 1);
    }

    if (incrementCached) {
      int index = totalCounters.size() - 1;
      cachedCounters.set(index, cachedCounters.get(index) + 1);
    }
  }

  private static <T> List<T> limit(List<T> list) {
    int n = 1000;
    int size = list.size();
    if (size <= n) {
      return list;
    }
    return list.subList(list.size() - n, list.size());
  }

  @GetMapping
  public synchronized Stats get() {
    record(false, false); // just to pretend that time is running

    return new Stats(
        allTickersCount.get(),
        cacheSize.get(),
        limit(timestamps).toArray(new Long[0]),
        limit(totalCounters).toArray(new Integer[0]),
        limit(cachedCounters).toArray(new Integer[0]));
  }

  @DeleteMapping
  public synchronized void reset() {
    timestamps.clear();
    timestamps.add(0L);
    totalCounters.clear();
    totalCounters.add(0);
    cachedCounters.clear();
    cachedCounters.add(0);
    lastFullSecond = System.currentTimeMillis();
  }
}
