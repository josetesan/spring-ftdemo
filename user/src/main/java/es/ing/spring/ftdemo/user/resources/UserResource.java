package es.ing.spring.ftdemo.user.resources;

import com.github.javafaker.Faker;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserResource {
  private final ConcurrentMap<String, UserSimulation> users = new ConcurrentHashMap<>();

  PortfolioClientExchange portfolioClientExchange;

  public UserResource(PortfolioClientExchange portfolioClientExchange) {
    this.portfolioClientExchange = portfolioClientExchange;
  }

  @GetMapping
  public List<User> get() {
    return users.values().stream().map(UserSimulation::toUser).toList();
  }

  @PostMapping
  public void add() {
    String user = Faker.instance().harryPotter().character();
    UserSimulation simulation = new UserSimulation(portfolioClientExchange, user);
    users.put(user, simulation);
  }

  @DeleteMapping("/{user}")
  public void remove(@PathVariable("user") String user) {
    UserSimulation simulation = users.remove(user);
    if (simulation != null) {
      simulation.destroy();
    }
  }
}
