package com.deliverytech.delivery_api.dto.req;
 
import jakarta.validation.constraints.*;
 
public class ClienteReqDTO {
 
    /*
    Anotation NoteBlank para não permitir
    que o usuario passe algo como vazio
    */
   
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;
 
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "")
    private String email;
 
    @NotBlank(message = "Telefone é obrigatório")
    /*
    Regex Regular expression, para realizar buscar e permitir
    que apenas a validade de determinadas entradas acorram no sistema, podendo ser utilizado
    para busca
    */
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefone deve ter apenas Digitos e dever ser DD + Telefone")
    private String telefone;
 
    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 200, message = "Endereço deve cnoter no maximo 200 caracteres")
    private String endereco;
 
    // Getter e Setter
 
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
 