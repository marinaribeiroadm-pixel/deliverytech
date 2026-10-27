package com.deliverytech.delivery_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
public class Cliente {

    /*
    através das anotações @id e @GeneratedValue(strategy = GenerationType.IDENTITY)
    nós definimos que o campo id do dado é a nossa Chave Primária (PK) da nossa
    tabela Cliente.
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String endereco;
    private boolean ativo;

    /*
    TO-DO: add Data em que o Cliente foi cadastrado no sistema
    private LocalDateTime dataCastrado; 
    VER UUIDV4, UUID V7, SNOWFLAKE...
    https://en.wikipedia.org/wiki/Universally_unique_identifier
    https://medium.com/@dinesharney/understanding-uuid-v4-uuid-v7-snowflake-id-and-nano-id-in-simple-terms-c50acf185b00
    */

    /*
    @JsonIgnore evita LazyInitialization ao seriealizar o Cliente em Json,
    ja que "pedidos" é uma coleção Lazy e a ssessao do hibernates já que está fechado
    quando o Json tenta serializar (Open-in-view=false)
    */
   @JsonIgnore
   @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
   private List<Pedido> pedidos;

}