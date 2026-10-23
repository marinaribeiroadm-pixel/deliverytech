package com.deliverytech.delivery_api.model;
 
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
 
    // TO-DO: add campo observação do pedido

/*
 * implementação de campo de forma de pagamento
 * (ENUM: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX, OUTROS)
 */

// Relacionamento com o Cliente
@ManyToOne
@JoinColumn(name = "cliente_id")
private Cliente cliente;

}
