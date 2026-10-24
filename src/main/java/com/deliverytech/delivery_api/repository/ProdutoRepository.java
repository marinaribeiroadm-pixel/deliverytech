package com.deliverytech.delivery_api.repository;
 
import com.deliverytech.delivery_api.model.Produto;
 
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.math.BigDecimal;
 
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
 
    // Produtos por restaurante
    List<Produto> findByRestauranteId(Long restauranteId);
 
    // Apenas produtos disponíveis
    List<Produto> findByDisponivelTrue();
 
    // Produtos por categoria
    List<Produto> findByCategoria(String categoria);
 
    // Por faixa de preço (menor ou igual)
    List<Produto> findByPrecoLessThanEqual(BigDecimal preco);
 
}