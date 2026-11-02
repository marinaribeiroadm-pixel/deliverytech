package com.deliverytech.delivery_api.dto.res;

import java.math.BigDecimal;

public class CalculoPedidoResDTO {
 
    private BigDecimal valorTotal;

    public CalculoPedidoResDTO() {}

    public CalculoPedidoResDTO(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    // Getters e Setters
    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

}
