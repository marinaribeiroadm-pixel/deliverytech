package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.model.Cliente;

import com.deliverytech.delivery_api.dto.req.ClienteReqDTO;
import com.deliverytech.delivery_api.dto.res.ClienteResDTO;
import com.deliverytech.delivery_api.service.ClienteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

/*
Definimos através da annotation de que esse arquivo trabalha com
requisições HTTP/HTTPS
*/
@RestController
@RequestMapping("/clientes")
/*
Permição de acesso para qualquer origem,
pois está utilizando a espressão "*"
*/
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClienteController {

    // injetando o serviço de cliente
    private final ClienteService clienteService;


    /*
    Utilizando verbo POST para cadastrar cliente
    */
    @PostMapping 

    /*
    Anotatio @Valid para validar o objeto cliente recebido no corpo da requisição
    através da anotação @RequestBody, que por sua vez indica que o obketo cliente será enviado no corpo da requisição
    */
    public ResponseEntity<ClienteResDTO>
    cadastrarCliente(@Valid 
        @RequestBody ClienteReqDTO dto) {
            ClienteResDTO cliente = clienteService.cadastrarCliente(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    /*
    Listar todos os clientes ativos
    */
    @GetMapping
    public ResponseEntity<List<ClienteResDTO>> listar() {
        List<ClienteResDTO> clientes = clienteService.listarClientesAtivos();
        return ResponseEntity.ok(clientes);
    }

    /*
    Buscar cliente por ID
    */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id,
        @Valid @RequestBody ClienteReqDTO dto
    ) {
        ClienteResDTO cliente = clienteService.atualizarCliente(id, dto);

        if (cliente != null) {
            return ResponseEntity.ok(cliente);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /*
    Atualizar cliente
    */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResDTO> atualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteReqDTO dto) {
        ClienteResDTO cliente = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(cliente);
    }

    /*
    ativa e desativar cliente (soft delete)
    */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ClienteResDTO> ativarDesativarCliente(
            @PathVariable Long id) {
        ClienteResDTO cliente = clienteService.ativarDesativarCliente(id);
        return ResponseEntity.ok(cliente);
    }

    /*
    Buscar cliente por email
    */
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResDTO> buscarPorEmail(@PathVariable String email) {
        ClienteResDTO cliente = clienteService.buscarClientePorEmail(email);
        return ResponseEntity.ok(cliente);
    }
}