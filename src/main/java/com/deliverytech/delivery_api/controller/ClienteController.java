package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.model.Cliente;
 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.ArrayList;
import java.util.List;
 
/*
Definimos através da annotation de que esse arquivo trabalha com
requisições HTTP/HTTPS
*/
@RestController
public class ClienteController {
   
    /*Simular um banco de dados com uma lista de Cliente
    construir utilizando Spring Boot REST delivery_api
    através do nosso localhost:8080/cliente
    */
   @GetMapping("/cliente")
   public Cliente getCliente() {
    return new Cliente("joão","Silva");
   }
 
    /* Simulando um banco de dados com uma lista de cliente
        Retornando uma List
        https://localhost:8080/cliente
    */
    @GetMapping("/cliente")
    /*<List> forma como usamos para percorer algo em uma lista (Array) */
    public List<Cliente> getcliente() {
        List<Cliente> cliente = new ArrayList<>();
        cliente.add(new Cliente("João", "Silva"));
        cliente.add(new Cliente("Maria", "Souza"));
        cliente.add(new Cliente("Pedro", "Oliveira"));
        return cliente;
    }
 
    /* lembrando se for salvo com caracter especiao no caso o ã a forma de busca e diferente */
    // O valor na URL diretamente injetado no método
    // http://localhost:8080/cliente/Jo%C3%A3o/Silva
    @GetMapping("/cliente/{nome}/{sobrenome}")
    public Cliente getClienteByPathVariable(@PathVariable String nome, @PathVariable String sobrenome) {
        return new Cliente(nome, sobrenome);
    }
 
    // build onde os valores são passados como query params
    // URL: http://localhost:8080/cliente/query?nome=Jo%C3%A3o&sobrenome=Silva
    @GetMapping("/cliente/query")
    public Cliente getClienteByQueryParam(
        @RequestParam String nome, @RequestParam String sobrenome) {
        return new Cliente(nome, sobrenome);
    }
 
}