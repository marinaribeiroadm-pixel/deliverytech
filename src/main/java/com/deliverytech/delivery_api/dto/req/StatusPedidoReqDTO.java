package com.deliverytech.delivery_api.dto.req;

import com.deliverytech.delivery_api.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public class StatusPedidoReqDTO {

    @NotNull(message = "Status é obrigatório")
    private StatusPedido status;

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    
}