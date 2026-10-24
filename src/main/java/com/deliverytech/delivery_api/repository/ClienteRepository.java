package com.deliverytech.delivery_api.repository;
 
import com.deliverytech.delivery_api.model.Cliente;
 
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface ClienteRepository  extends JpaRepository<Cliente, Long> {
 
    // buscar ccliente por emailç
    Optional<Cliente> findByEmail(String email);
 
    // buscar cliente está ativo
    boolean existsByEmailAndAtivoTrue(String email);
 
    // Busca por nome (parcial, ignorando case)
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
 
    // Verificar se email já existe
    boolean existsByEmail(String email);
 
}