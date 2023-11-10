package es.ing.spring.ftdemo.user.resources;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface PortfolioClientExchange {

  @GetExchange("portfolio/{user}")
  Portfolio findByUser(@PathVariable String user);
}
