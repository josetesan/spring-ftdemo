package es.ing.spring.ftdemo.portfolio;

import es.ing.spring.ftdemo.portfolio.resources.BrokerClientExchange;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class PortfolioApplication {

  public static void main(String[] args) {
    SpringApplication.run(PortfolioApplication.class, args);
  }

  @Bean
  BrokerClientExchange stockPriceClientExchange(
      @Value("${spring.http.clients.broker.url}") String url,
      ObservationRegistry observationRegistry) {
    WebClient webClient =
        WebClient.builder().baseUrl(url).observationRegistry(observationRegistry).build();
    HttpServiceProxyFactory factory =
        HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
    return factory.createClient(BrokerClientExchange.class);
  }
}
