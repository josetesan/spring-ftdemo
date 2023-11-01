package es.ing.spring.ftdemo.user.resources;

import com.maciejwalkowiak.spring.http.annotation.HttpClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

@HttpClient("portfolio-client")
public interface PortfolioClient {
  @GetExchange("/portfolio/{user}")
  Portfolio get(@PathVariable("user") String user);
}
