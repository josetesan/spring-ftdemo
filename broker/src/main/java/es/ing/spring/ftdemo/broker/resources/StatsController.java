package es.ing.spring.ftdemo.broker.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks/stats")
public class StatsController {
  private final AtomicInteger concurrentRequests;

  private final List<Long> timestamps;
  private final List<Integer> totalCounters;
  private final List<Integer> failuresCounters;

  private long lastFullSecond = System.currentTimeMillis();

  public StatsController() {
    concurrentRequests = new AtomicInteger(0);
    timestamps = new ArrayList<>(List.of(0L));
    totalCounters = new ArrayList<>(List.of(0));
    failuresCounters = new ArrayList<>(List.of(0));
  }

  public <T> T request(IntFunction<T> action) {
    int currentInFlightRequests = concurrentRequests.incrementAndGet();
    try {
      return action.apply(currentInFlightRequests);
    } finally {
      concurrentRequests.decrementAndGet();
    }
  }

  public void recordOk() {
    record(true, false);
  }

  public void recordKo() {
    record(true, true);
  }

  private synchronized void record(boolean incrementTotals, boolean incrementFailures) {
    long now = System.currentTimeMillis();
    long diff = now - lastFullSecond;

    while (diff > 1000) {
      long previous = timestamps.get(timestamps.size() - 1);
      timestamps.add(previous + 1);

      totalCounters.add(0);
      failuresCounters.add(0);

      diff -= 1000;
      lastFullSecond += 1000;
    }

    if (incrementTotals) {
      int index = totalCounters.size() - 1;
      totalCounters.set(index, totalCounters.get(index) + 1);
    }

    if (incrementFailures) {
      int index = totalCounters.size() - 1;
      failuresCounters.set(index, failuresCounters.get(index) + 1);
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
        concurrentRequests.get(),
        limit(timestamps).toArray(new Long[0]),
        limit(totalCounters).toArray(new Integer[0]),
        limit(failuresCounters).toArray(new Integer[0]));
  }

  @DeleteMapping
  public synchronized void reset() {
    timestamps.clear();
    timestamps.add(0L);
    totalCounters.clear();
    totalCounters.add(0);
    failuresCounters.clear();
    failuresCounters.add(0);
    lastFullSecond = System.currentTimeMillis();
  }
}
