package com.deliverytech.delivery_api.dto.res;

public class LoginResDTO {

    /*
    O ideal, o cenario bom para segurança
     e ter um token de acess e outro token de utilização
     refresh token
    */
    private String token;
    private String tipo = "Bearer";
    private Long expiracao;
    private UserResDTO usuario;

    // Construtores
    public LoginResDTO() {}

    public LoginResDTO(String token, Long expiracao, UserResDTO usuario) {
        this.token = token;
        this.expiracao = expiracao;
        this.usuario = usuario;
    }

    // Getters e Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getExpiracao() { return expiracao; }
    public void setExpiracao(Long expiracao) { this.expiracao = expiracao; }

    public UserResDTO getUsuario() { return usuario; }
    public void setUsuario(UserResDTO usuario) { this.usuario = usuario; }

}
