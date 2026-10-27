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

    // Buscar por faixa de preço
    List<Produto> findByPrecoBetweenAndDisponivelTrue(BigDecimal precoMin, BigDecimal precoMax);

    // Buscar produtos mais baratos que um valor
    List<Produto> findByPrecoLessThanEqualAndDisponivelTrue(BigDecimal preco);

    // Ordenar por preço
    List<Produto> findByDisponivelTrueOrderByPrecoAsc();
    List<Produto> findByDisponivelTrueOrderByPrecoDesc();

    // Query customizada - produtos mais vendidos
    @Query("""
        SELECT ip.produto 
        FROM ItemPedido ip 
        GROUP BY ip.produto 
        ORDER BY COUNT(ip) DESC
    """)
    List<Produto> findProdutosMaisVendidos();

    // Query customizada - produtos por restaurante e categoria
    @Query("SELECT p FROM Produto p WHERE p.restaurante.id = :restauranteId " +
           "AND p.categoria = :categoria AND p.disponivel = true")
    List<Produto> findByRestauranteAndCategoria(@Param("restauranteId") Long restauranteId,
                                                @Param("categoria") String categoria);

    // Contar produtos por restaurante
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.restaurante.id = :restauranteId")
    long countByRestauranteId(@Param("restauranteId") Long restauranteId);

    @Query(value = "SELECT c.nome, COUNT(p.id) as total_pedidos " +
               "FROM cliente c " +
               "LEFT JOIN pedido p ON c.id = p.cliente_id " +
               "GROUP BY c.id, c.nome " +
               "ORDER BY total_pedidos DESC " +
               "LIMIT 10", nativeQuery = true)
    List<Object[]> rankingClientesPorPedidos();
}