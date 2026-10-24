package com.deliverytech.delivery_api.repository;
 
import com.deliverytech.delivery_api.model.Restaurante;
 
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.math.BigDecimal;
 
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
 
    // Buscar por categoria
    List<Restaurante> findByCategoria(String categoria);
 
    // Buscar restaurantes ativos
    List<Restaurante> findByAtivoTrue();
 
    // Por taxa de entrega menor ou igual
    List<Restaurante> findByTaxaEntregaLessThanEqual(BigDecimal taxa);
 
    // Top 5 restaurantes por nome (ordem alfabética)
    List<Restaurante> findTop5ByOrderByNomeAsc();
 
}