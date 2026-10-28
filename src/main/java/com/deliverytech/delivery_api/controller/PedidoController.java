package com.deliverytech.delivery_api.controller;


import com.deliverytech.delivery_api.dto.req.ItemPedidoReqDTO;
import com.deliverytech.delivery_api.dto.req.PedidoReqDTO;
import com.deliverytech.delivery_api.dto.res.PedidoResDTO;
import com.deliverytech.delivery_api.dto.req.StatusPedidoReqDTO;
import com.deliverytech.delivery_api.model.Pedido;
import com.deliverytech.delivery_api.enums.StatusPedido;
import com.deliverytech.delivery_api.service.PedidoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Iniciando o controller REST para o Pedido
@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    // injetando o serviço de Pedido
    @Autowired
    private PedidoService pedidoService;

    /*
    Criar novo pedido
    através do verbo POST estamos criando o pedido, associando
    um cliente e um restaurante através dos parâmetros da requisição (@RequestParam)
    */
    @PostMapping
    public ResponseEntity<PedidoResDTO> criarPedido(@Valid @RequestBody PedidoReqDTO dto) {
        PedidoResDTO pedido = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    /*
    Buscar pedido por ID
    */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResDTO> buscarPorId(@PathVariable Long id) {
        PedidoResDTO pedido = pedidoService.buscarPedidoPorId(id);
        return ResponseEntity.ok(pedido);
    }

    /*
    Listar pedidos por cliente
    retorna o histórico de pedidos de um cliente específico,
    ordenado (no repository) do mais recente para o mais antigo
    */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResDTO>> buscarPorCliente(@PathVariable Long clienteId) {
        List<PedidoResDTO> pedidos = pedidoService.buscarPedidosPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    /*
    Atualizar status do pedido
    através do verbo PUT estamos alterando o status do pedido
    (ex: PENDENTE -> CONFIRMADO -> PREPARANDO -> SAIU_PARA_ENTREGA -> ENTREGUE)
    */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PedidoResDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusPedidoReqDTO statusDTO) {
        PedidoResDTO pedido = pedidoService.atualizarStatusPedido(id, statusDTO.getStatus());
        return ResponseEntity.ok(pedido);
    }

    /*
    Cancelar pedido
    através do verbo PUT estamos alterando o status do pedido para CANCELADO,
    com um motivo opcional informado via parâmetro da requisição
    */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calcular")
    public ResponseEntity<BigDecimal> calcularTotal(@Valid @RequestBody List<ItemPedidoReqDTO> itens) {
        BigDecimal total = pedidoService.calcularTotalPedido(itens);
        return ResponseEntity.ok(total);
    }

}