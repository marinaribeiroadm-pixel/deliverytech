package com.deliverytech.delivery_api.model;
 
import com.deliverytech.delivery_api.controller.enums.StatusPedido;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
 
@Data
@Entity
public class Pedido {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numeroPedido;
    private LocalDateTime dataPedido;
    private Boolean entrega;
    private BigDecimal subtotal;
    private BigDecimal taxaEntrega;
    private BigDecimal valorTotal;

    // salva o nome do status (ex: "PENDENTE") no banco em vez do número
    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    // TO-DO: add campo observação do pedido

/*
 * implementação de campo de forma de pagamento
 * (ENUM: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX, OUTROS)
 */

// Relacionamento com o Cliente
@ManyToOne
@JoinColumn(name = "cliente_id")
private Cliente cliente;

// Relacionamento com o Restaurante
@ManyToOne
@JoinColumn(name = "restaurante_id")
private Restaurante restaurante;

// Itens do pedido; @JsonIgnore evita LazyInitialization ao serializar
@JsonIgnore
@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
private List<ItemPedido> itens;

}
