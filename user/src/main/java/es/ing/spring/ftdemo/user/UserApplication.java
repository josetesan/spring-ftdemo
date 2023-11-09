package es.ing.spring.ftdemo.user;

import es.ing.spring.ftdemo.user.resources.PortfolioClientExchange;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class UserApplication {

  public static void main(String[] args) {
    SpringApplication.run(UserApplication.class, args);
  }


  @Bean
  PortfolioClientExchange portfolioClientExchange(
      @Value("${spring.http.clients.portfolio.url}") String url,
      ObservationRegistry observationRegistry) {
    RestClient restClient =
        RestClient.builder().baseUrl(url).observationRegistry(observationRegistry).build();
    HttpServiceProxyFactory factory =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    return factory.createClient(PortfolioClientExchange.class);
  }
}
