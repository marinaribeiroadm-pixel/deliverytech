package com.deliverytech.delivery_api.config.monitoring.health;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

  private final DataSource dataSource;

  public DatabaseHealthIndicator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Health health() {
    /*
        Tentar estabelcer a conexão com o banco de dados e verificar se a conexão é válida. 
        Se a conexão for válida, retorna um status "up".
        Caso contrário, retorna um status "down" com detalhes do erro.
        */
    try (Connection connection = dataSource.getConnection()) {
      if (connection.isValid(1)) {
        return Health.up()
          .withDetail("Database", "H2")
          .withDetail("Status", "Connection is valid")
          .withDetail("validationQuery", "SELECT 1")
          .build();
      }
      /*
      Captura a exceção SQLException caso ocorra algum erro ao 
      tentar estabelecer a conexão com o banco de dados.
      */
    } catch (SQLException e) {
      return Health.down()
        .withDetail("Database", "H2")
        /*
        Através do método getMessage() da exceção, é possível obter 
        uma mensagem de erro detalhada que descreve o motivo da falha na conexão 
        com o banco de dados.
        */
        .withDetail("Error", e.getMessage())
        .build();
    }
    /*
    Por fim retorna que a conexão não é válida, com status "down" 
    e detalhes do erro.
    */
    return Health.down()
      .withDetail("Database", "H2")
      .withDetail("Error", "Connection is not valid")
      .build();
  }
}
