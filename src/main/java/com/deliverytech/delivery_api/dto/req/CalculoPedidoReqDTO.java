package com.deliverytech.delivery_api.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CalculoPedidoReqDTO {

    @NotEmpty(message = "A lista de itens não pode estar vazia")
    @Valid
    private List itens;

    // Getters e Setters
    public List getItens() {
        return itens;
    }

    public void setItens(List itens) {
        this.itens = itens;
    }
}
