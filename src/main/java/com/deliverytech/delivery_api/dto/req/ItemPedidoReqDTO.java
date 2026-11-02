package com.deliverytech.delivery_api.dto.req;

import jakarta.validation.constraints.*;

public class ItemPedidoReqDTO {

    /*
    @Anotaion NotNull para não permitir que o campo receba dados Nulos,
    Null não é vazio
    */
    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 10, message = "Quantidade máxima é 10")
    private Integer quantidade;

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

}
