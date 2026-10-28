package com.deliverytech.delivery_api.service;

import java.math.BigDecimal;
import java.util.List;

import com.deliverytech.delivery_api.dto.req.ItemPedidoReqDTO;
import com.deliverytech.delivery_api.dto.req.PedidoReqDTO;
import com.deliverytech.delivery_api.dto.res.PedidoResDTO;
import com.deliverytech.delivery_api.enums.StatusPedido;

/*
 * Contrato da camada de serviço de Pedido.
 *
 * Precisa ser uma INTERFACE (e não uma classe), porque o PedidoServiceImpl
 * faz "implements PedidoService". Anotações como @Service e @Transactional
 * ficam na implementação, não aqui.
 */
public interface PedidoService {

    PedidoResDTO criarPedido(PedidoReqDTO dto);

    PedidoResDTO buscarPedidoPorId(Long id);

    List<PedidoResDTO> buscarPedidosPorCliente(Long clienteId);

    PedidoResDTO atualizarStatusPedido(Long id, StatusPedido novoStatus);

    BigDecimal calcularTotalPedido(List<ItemPedidoReqDTO> itens);

    void cancelarPedido(Long id);

}