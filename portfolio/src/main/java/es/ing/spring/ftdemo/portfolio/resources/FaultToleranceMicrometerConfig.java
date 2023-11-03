package es.ing.spring.ftdemo.portfolio.resources;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FaultToleranceMicrometerConfig {

  // this makes sure the Prometheus output from Micrometer is [nearly] identical to the one from MP
  // Metrics,
  // which ensures that the same dashboard can be used for both

  @Bean
  public MeterFilter renameAndEnableHistograms() {
    return new MeterFilter() {
      @Override
      public Meter.Id map(Meter.Id id) {
        if (id.getName().startsWith("ft.")) {
          return id.withName("base." + id.getName());
        }
        return id;
      }

      @Override
      public DistributionStatisticConfig configure(
          Meter.Id id, DistributionStatisticConfig config) {
        if (id.getName().startsWith("base.ft.")) {
          return DistributionStatisticConfig.builder()
              .percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999)
              .build()
              .merge(config);
        }
        return config;
      }
    };
  }
}
