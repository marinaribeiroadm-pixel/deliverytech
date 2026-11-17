package com.deliverytech.delivery_api.config.monitoring.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicrometerConfig {

  @Bean
  /*
  Criando classe publica `MicrometerConfig` que configura o registro de métricas do Micrometer.
  O método `metricsCommonTags` retorna um `MeterRegistryCustomizer` que adiciona tags
    comuns a todas as métricas registradas, como o nome da aplicação, o ambiente e a versão.
  */
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry ->
      registry.config()
      .commonTags("application", "delivery-api")
      .commonTags("environment", "production")
      .commonTags("version", "1.0.0")
      /*
      Adicione um `MeterFilter` para impedir a coleta de métricas referentes aos endpoints do Actuator. 
      Isso é útil para evitar a poluição das métricas com dados de monitoramento interno e focar em 
      métricas específicas da aplicação.
      */
      .meterFilter(MeterFilter.deny(id -> {
        String uri = id.getTag("uri");
        return uri != null && uri.startsWith("/actuator");
      }));

  }
}
