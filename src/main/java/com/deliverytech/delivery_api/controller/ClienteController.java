package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.model.Cliente;

import com.deliverytech.delivery_api.dto.req.ClienteReqDTO;
import com.deliverytech.delivery_api.dto.res.ClienteResDTO;
import com.deliverytech.delivery_api.service.ClienteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Clientes", description = " Operações relacionadas aos clientes")
public class ClienteController {

    /*
    Logger da classe. Os logs recebem automaticamente o correlationId
    (via CorrelationIdFilter) e o traceId (via Micrometer Tracing) pelo MDC.
    */
    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

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
   @Operation(summary = "Cadastras cliente", 
                description = "Cadastrar um novo cliente no sistema"
   )
   @ApiResponses({
       @ApiResponse(responseCode = "201", description = "Cliente Cadastrado com Sucesso"),
       @ApiResponse(responseCode = "400", description = "Dados inválidos")
   })
    public ResponseEntity<ClienteResDTO>
    cadastrarCliente(@Valid 
        @RequestBody ClienteReqDTO dto) {
            logger.info("Cadastro de cliente iniciado");

            ClienteResDTO cliente = clienteService.cadastrarCliente(dto);

            logger.info("Cliente cadastrado com ID {}", cliente.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    /*
    Listar todos os clientes ativos
    */
    @GetMapping
    public ResponseEntity<List<ClienteResDTO>> listar() {
        logger.debug("Listagem de clientes ativos solicitada");

        List<ClienteResDTO> clientes = clienteService.listarClientesAtivos();

        logger.debug("Listagem retornou {} clientes", clientes.size());
        return ResponseEntity.ok(clientes);
    }

    /*
    Buscar cliente por ID
    */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResDTO> buscarPorId(@PathVariable Long id) {
        logger.debug("Busca de cliente por ID {}", id);
        return ResponseEntity.ok(clienteService.buscarClientePorId(id));
    }

    /*
    Atualizar cliente
    */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResDTO> atualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteReqDTO dto) {
        logger.info("Atualização do cliente ID {} iniciada", id);

        ClienteResDTO cliente = clienteService.atualizarCliente(id, dto);

        logger.info("Cliente ID {} atualizado", id);
        return ResponseEntity.ok(cliente);
    }

    /*
    ativa e desativar cliente (soft delete)
    */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ClienteResDTO> ativarDesativarCliente(
            @PathVariable Long id) {
        logger.info("Alteração de status do cliente ID {} solicitada", id);

        ClienteResDTO cliente = clienteService.ativarDesativarCliente(id);

        logger.info("Status do cliente ID {} atualizado", id);
        return ResponseEntity.ok(cliente);
    }

    /*
    Buscar cliente por email
    */
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResDTO> buscarPorEmail(@PathVariable String email) {
        logger.debug("Busca de cliente por e-mail {}", mascararEmail(email));

        ClienteResDTO cliente = clienteService.buscarClientePorEmail(email);
        return ResponseEntity.ok(cliente);
    }

    /*
    Mascara o e-mail antes de registrar no log (ex.: jo***@dominio.com),
    pois e-mail é dado pessoal (LGPD) e os logs ficam salvos em arquivo
    */
    private String mascararEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int arroba = email.indexOf('@');
        String prefixo = email.substring(0, Math.min(2, arroba));
        return prefixo + "***" + email.substring(arroba);
    }

}