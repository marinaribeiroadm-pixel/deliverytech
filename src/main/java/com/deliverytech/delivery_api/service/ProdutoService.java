package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.req.ProdutoReqDTO;
import com.deliverytech.delivery_api.dto.res.ProdutoResDTO;

import java.util.List;

/*
 Contrato da camada de serviço de Produto.
 A implementação fica em service.impl.ProdutoServiceImpl
*/
public interface ProdutoService {

    // Cadastra um novo produto vinculado a um restaurante
    ProdutoResDTO cadastrarProduto(ProdutoReqDTO dto);

    // Busca um produto pelo id, lançando exceção caso não exista
    ProdutoResDTO buscarProdutoPorId(Long id);

    // Atualiza os dados de um produto existente
    ProdutoResDTO atualizarProduto(Long id, ProdutoReqDTO dto);

    // Remove um produto do sistema
    void removerProduto(Long id);

    // Alterna a disponibilidade do produto
    ProdutoResDTO alterarDisponibilidade(Long id);

    // Busca produtos disponíveis por categoria
    List<ProdutoResDTO> buscarProdutosPorCategoria(String categoria);

    // Busca produtos pelo nome (busca parcial)
    List<ProdutoResDTO> buscarProdutosPorNome(String nome);

    // Lista os produtos de um restaurante, opcionalmente filtrando por disponibilidade
    List<ProdutoResDTO> buscarProdutosPorRestaurante(Long restauranteId, Boolean disponivel);

}
