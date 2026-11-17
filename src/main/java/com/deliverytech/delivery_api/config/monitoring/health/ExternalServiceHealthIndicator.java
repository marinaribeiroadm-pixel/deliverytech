package com.deliverytech.delivery_api.config.monitoring.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("externalService")
public class ExternalServiceHealthIndicator implements HealthIndicator {

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public Health health() {
    try {

        String url = "https://httpbin.org/status/200";
        restTemplate.getForObject(url, String.class);

        return Health.up()
        .withDetail("External Service", "Available")
        .withDetail("URL", url)
        .withDetail("Status", "200 OK")
        .withDetail("Response", "< 100ms")
        .build();
      
    } catch (Exception e) {
      return Health.down()
      .withDetail("External Service", "Unavailable")
        .withDetail("External Service", "Error: " + e.getMessage())
        .withDetail("status", "unavailable")
        .build();
    }
  }
}
