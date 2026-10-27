package com.deliverytech.delivery_api.model;

import com.deliverytech.delivery_api.enums.StatusPedido;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
public class Pedido {

    /*
    através das anotações @id e @GeneratedValue(strategy = GenerationType.IDENTITY)
    nós definimos que o campo id do dado é a nossa Chave Primária (PK) da nossa
    tabela Cliente.
    */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numeroPedido;
    private LocalDateTime dataPedido;
    private Boolean entrega;
    private BigDecimal subtotal;
    private BigDecimal taxaEntrega;
    private BigDecimal valorTotal;

    // TO-DO: add campo observação do pedido

    /*
    implementação de campo de forma de pagamento
    (ENUM: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX, OUTROS)
    */

    //para armazenar o nome do enum no banco, usamos @Enumerated(EnumType.STRING)
    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    // Relacionamento com o Cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Relacionamento com Pedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens;

    // Relacionamento com o Restaurante
    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;


}