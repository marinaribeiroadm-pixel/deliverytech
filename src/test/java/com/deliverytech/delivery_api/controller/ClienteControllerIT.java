package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.config.TestDataConfiguration;
import com.deliverytech.delivery_api.model.Cliente;
import com.deliverytech.delivery_api.repository.ClienteRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@Import(TestDataConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Testes de Integração do ClienteController")
class ClienteControllerIT {

    // Mantenha igual ao @RequestMapping do ClienteController.
    private static final String BASE_URL = "/clientes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    // Cliente criado pelo TestDataConfiguration antes de cada teste.
    private Cliente clienteExistente() {
        return clienteRepository.findAll().get(0);
    }

    @Test
    @DisplayName("Deve criar o cliente com sucesso com dados válidos")
    void should_CreateCliente_When_ValidData() throws Exception {
        // Given: payload no formato do ClienteReqDTO (não a entidade JPA)
        Map<String, Object> payload = Map.of(
                "nome", "Maria Silva",
                "email", "maria@email.com",
                "telefone", "11999999999",
                "endereco", "Rua das Flores, 123 - Centro"
        );

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Maria Silva")))
                .andExpect(jsonPath("$.email", is("maria@email.com")))
                .andExpect(jsonPath("$.telefone", is("11999999999")));
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando dados inválidos")
    void should_ReturnBadRequest_When_InvalidData() throws Exception {
        // Given: nome vazio e email em formato inválido
        Map<String, Object> payload = Map.of(
                "nome", "",
                "email", "email-invalido"
        );

        // When & Then: o GlobalExceptionHandler devolve os erros por campo em "details"
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.nome").exists())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    @DisplayName("Deve buscar cliente por ID existente")
    void should_ReturnCliente_When_IdExists() throws Exception {
        // Given
        Cliente cliente = clienteExistente();

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cliente.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(cliente.getNome())))
                .andExpect(jsonPath("$.email", is(cliente.getEmail())));
    }

    @Test
    @DisplayName("Deve retornar 404 quando cliente não existe")
    void should_ReturnNotFound_When_ClienteNotExists() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado")));
    }

    @Test
    @DisplayName("Deve listar os clientes ativos")
    void should_ReturnClientesAtivos_When_Listing() throws Exception {
        // When & Then: o endpoint retorna uma lista simples (não paginada)
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].email", is("cliente.test@gmail.com")));
    }

    @Test
    @DisplayName("Deve atualizar cliente existente")
    void should_UpdateCliente_When_ClienteExists() throws Exception {
        // Given
        Cliente cliente = clienteExistente();
        Map<String, Object> payload = Map.of(
                "nome", "Nome Atualizado",
                "email", cliente.getEmail(),
                "telefone", "11777777777",
                "endereco", "Av. Paulista, 1000 - Bela Vista"
        );

        // When & Then
        mockMvc.perform(put(BASE_URL + "/{id}", cliente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cliente.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Nome Atualizado")))
                .andExpect(jsonPath("$.telefone", is("11777777777")))
                .andExpect(jsonPath("$.endereco", is("Av. Paulista, 1000 - Bela Vista")));
    }

    @Test
    @DisplayName("Deve desativar cliente ativo (soft delete)")
    void should_DeactivateCliente_When_ClienteIsActive() throws Exception {
        // Given
        Cliente cliente = clienteExistente();

        // When & Then: PATCH alterna o status; o cliente de teste nasce ativo
        mockMvc.perform(patch(BASE_URL + "/{id}/status", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo", is(false)));

        // E não aparece mais na listagem de ativos
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(cliente.getId().intValue()))));
    }

}