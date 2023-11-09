package es.ing.spring.ftdemo.portfolio;

import es.ing.spring.ftdemo.portfolio.resources.StockPriceClientExchange;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class PortfolioApplication {

  public static void main(String[] args) {
    SpringApplication.run(PortfolioApplication.class, args);
  }

  @Bean
  StockPriceClientExchange stockPriceClientExchange(
      @Value("${spring.http.clients.stockservice.url}") String url,
      ObservationRegistry observationRegistry) {
    RestClient restClient =
        RestClient.builder().baseUrl(url).observationRegistry(observationRegistry).build();
    HttpServiceProxyFactory factory =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    return factory.createClient(StockPriceClientExchange.class);
  }
}
