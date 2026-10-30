package com.deliverytech.delivery_api.service;
 
import com.deliverytech.delivery_api.dto.req.RestauranteReqDTO;
import com.deliverytech.delivery_api.dto.res.RestauranteResDTO;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
import java.math.BigDecimal;
import java.util.List;
 
/*
 Contrato da camada de serviço de Restaurante.
 A implementação fica em service.impl.RestauranteServiceImpl
*/
public interface RestauranteService {
 
    // Cadastra um novo restaurante a partir do DTO recebido
    RestauranteResDTO cadastrar(RestauranteReqDTO dto);
 
    // Lista restaurantes com filtros opcionais (categoria/ativo) e paginação
    Page<RestauranteResDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable);
 
    // Busca um restaurante pelo id, lançando exceção caso não exista
    RestauranteResDTO buscarRestaurantePorId(Long id);
 
    // Atualiza os dados de um restaurante existente
    RestauranteResDTO atualizarRestaurante(Long id, RestauranteReqDTO dto);
 
    // Alterna o status ativo/inativo do restaurante
    RestauranteResDTO alterarStatusRestaurante(Long id);
 
    // Busca restaurantes ativos por categoria
    List<RestauranteResDTO> buscarRestaurantesPorCategoria(String categoria);
 
    // Calcula a taxa de entrega de um restaurante para um CEP específico
    BigDecimal calcularTaxaEntrega(Long id, String cep);
 
    // Busca restaurantes próximos a um CEP dentro de um raio (km)
    List<RestauranteResDTO> buscarRestaurantesProximos(String cep, Integer raio);
 
}