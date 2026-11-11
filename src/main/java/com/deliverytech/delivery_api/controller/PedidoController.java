package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.req.ItemPedidoReqDTO;
import com.deliverytech.delivery_api.dto.req.PedidoReqDTO;
import com.deliverytech.delivery_api.dto.req.StatusPedidoReqDTO;
import com.deliverytech.delivery_api.dto.res.PedidoResDTO;
import com.deliverytech.delivery_api.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// Iniciando o controller REST para o Pedido
@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "Operações relacionadas ao gerenciamento de pedidos")
public class PedidoController {

    // injetando o serviço de Pedido
    @Autowired
    private PedidoService pedidoService;

    /*
    Listar meus pedidos
    através do verbo GET estamos listando os pedidos do cliente autenticado,
    com paginação, ordenado do mais recente para o mais antigo
    */
    @GetMapping("/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
        summary = "Listar meus pedidos",
        description = "Retorna os pedidos do cliente autenticado",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Pedidos"}
    )
    public ResponseEntity<Page<PedidoResDTO>> listarMeusPedidos(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Informações de paginação") Pageable pageable) {

        Page<PedidoResDTO> pedidos = pedidoService.listarMeusPedidos(authentication.getName(), pageable);
        return ResponseEntity.ok(pedidos);
    }

    /*
    Criar novo pedido
    através do verbo POST estamos criando o pedido, associando
    um cliente e um restaurante através do corpo da requisição (@RequestBody)
    */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
        summary = "Criar novo pedido",
        description = "Cria um novo pedido para o cliente autenticado",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Pedidos"}
    )
    public ResponseEntity<PedidoResDTO> criarPedido(@Valid @RequestBody PedidoReqDTO dto) {
        PedidoResDTO pedido = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    /*
    Buscar pedido por ID
    através do verbo GET retornamos os detalhes de um pedido específico
    */
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar pedido por ID",
        description = "Retorna os detalhes de um pedido específico pelo seu ID",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Pedidos"}
    )
    public ResponseEntity<PedidoResDTO> buscarPorId(
            @Parameter(description = "ID do pedido", example = "1") @PathVariable Long id) {
        PedidoResDTO pedido = pedidoService.buscarPedidoPorId(id);
        return ResponseEntity.ok(pedido);
    }

    /*
    Listar pedidos por cliente
    retorna o histórico de pedidos de um cliente específico,
    ordenado do mais recente para o mais antigo
    */
    @GetMapping("/cliente/{clienteId}")
    @Operation(
        summary = "Listar pedidos por cliente",
        description = "Retorna o histórico de pedidos de um cliente específico, do mais recente para o mais antigo",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Pedidos"}
    )
    public ResponseEntity<List<PedidoResDTO>> buscarPorCliente(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long clienteId) {
        List<PedidoResDTO> pedidos = pedidoService.buscarPedidosPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }

    /*
    Atualizar status do pedido
    através do verbo PATCH estamos alterando apenas o status do pedido
    (ex: PENDENTE -> CONFIRMADO -> PREPARANDO -> SAIU_PARA_ENTREGA -> ENTREGUE)
    */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANTE') or hasRole('ADMIN')")
    @Operation(
        summary = "Atualizar status do pedido",
        description = "Atualiza o status de um pedido, respeitando as transições permitidas",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Pedidos"}
    )
    public ResponseEntity<PedidoResDTO> atualizarStatus(
            @Parameter(description = "ID do pedido", example = "1") @PathVariable Long id,
            @Valid @RequestBody StatusPedidoReqDTO statusDTO) {
        PedidoResDTO pedido = pedidoService.atualizarStatusPedido(id, statusDTO.getStatus());
        return ResponseEntity.ok(pedido);
    }

    /*
    Cancelar pedido
    através do verbo DELETE alteramos o status do pedido para CANCELADO
    (o registro não é removido do banco); só é permitido nos status PENDENTE e CONFIRMADO
    */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancelar pedido",
        description = "Cancela um pedido nos status PENDENTE ou CONFIRMADO. O pedido não é excluído, apenas marcado como CANCELADO",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Pedidos"}
    )
    public ResponseEntity<Void> cancelarPedido(
            @Parameter(description = "ID do pedido", example = "1") @PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    /*
    Calcular total do pedido
    através do verbo POST simulamos o valor dos itens antes de criar o pedido,
    sem persistir nada (não inclui a taxa de entrega)
    */
    @PostMapping("/calcular")
    @Operation(
        summary = "Calcular total do pedido",
        description = "Simula o valor total dos itens informados, sem criar o pedido. Não inclui a taxa de entrega",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Pedidos"}
    )
    public ResponseEntity<BigDecimal> calcularTotal(@Valid @RequestBody List<ItemPedidoReqDTO> itens) {
        BigDecimal total = pedidoService.calcularTotalPedido(itens);
        return ResponseEntity.ok(total);
    }

}