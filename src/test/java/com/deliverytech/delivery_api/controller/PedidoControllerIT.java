package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.config.TestDataConfiguration;
import com.deliverytech.delivery_api.model.Cliente;
import com.deliverytech.delivery_api.model.Produto;
import com.deliverytech.delivery_api.model.Restaurante;
import com.deliverytech.delivery_api.repository.ClienteRepository;
import com.deliverytech.delivery_api.repository.ProdutoRepository;
import com.deliverytech.delivery_api.repository.RestauranteRepository;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@Import(TestDataConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Testes de Integração do PedidoController")
class PedidoControllerIT {

    // Mantenha igual ao @RequestMapping do PedidoController.
    private static final String BASE_URL = "/pedidos";

    // Deve ser o mesmo valor que authentication.getName() retorna para o cliente de teste.
    private static final String EMAIL_CLIENTE = "cliente.test@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    // Dados criados pelo TestDataConfiguration antes de cada teste.
    private Cliente clienteExistente() {
        return clienteRepository.findAll().get(0);
    }

    private Restaurante restauranteExistente() {
        return restauranteRepository.findAll().get(0);
    }

    // O TestDataConfiguration cria o produto vinculado ao restaurante acima.
    private Produto produtoExistente() {
        return produtoRepository.findAll().get(0);
    }

    // Sobrescreve o @WithMockUser(ADMIN) da classe apenas na requisição.
    private RequestPostProcessor comoCliente() {
        return user(EMAIL_CLIENTE).roles("CLIENTE");
    }

    // Payload no formato do PedidoReqDTO (não a entidade JPA)
    private Map<String, Object> payloadPedidoValido(int quantidade) {
        return Map.of(
                "clienteId", clienteExistente().getId(),
                "restauranteId", restauranteExistente().getId(),
                "enderecoEntrega", "Rua das Flores, 123 - Centro",
                "itens", List.of(Map.of(
                        "produtoId", produtoExistente().getId(),
                        "quantidade", quantidade
                ))
        );
    }

    // Cria um pedido via API (como CLIENTE) e devolve o ID gerado.
    private Long criarPedidoViaApi() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .with(comoCliente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadPedidoValido(2))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private void atualizarStatusViaApi(Long id, String novoStatus) throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", novoStatus))))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------------
    // POST /pedidos
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Deve criar o pedido com sucesso com dados válidos")
    void should_CreatePedido_When_ValidData() throws Exception {
        // Given
        Cliente cliente = clienteExistente();
        Restaurante restaurante = restauranteExistente();

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .with(comoCliente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadPedidoValido(2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.numeroPedido", notNullValue()))
                .andExpect(jsonPath("$.statusPedido", is("PENDENTE")))
                .andExpect(jsonPath("$.clienteId", is(cliente.getId().intValue())))
                .andExpect(jsonPath("$.restauranteId", is(restaurante.getId().intValue())))
                .andExpect(jsonPath("$.subtotal", notNullValue()))
                .andExpect(jsonPath("$.valorTotal", notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando dados do pedido inválidos")
    void should_ReturnBadRequest_When_InvalidPedidoData() throws Exception {
        // Given: sem cliente, sem restaurante, endereço vazio e lista de itens vazia
        Map<String, Object> payload = Map.of(
                "enderecoEntrega", "",
                "itens", List.of()
        );

        // When & Then: o GlobalExceptionHandler devolve os erros por campo em "details"
        mockMvc.perform(post(BASE_URL)
                        .with(comoCliente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.clienteId").exists())
                .andExpect(jsonPath("$.details.restauranteId").exists())
                .andExpect(jsonPath("$.details.enderecoEntrega").exists())
                .andExpect(jsonPath("$.details.itens").exists());
    }

    @Test
    @DisplayName("Deve retornar 403 quando usuário sem role CLIENTE tenta criar pedido")
    void should_ReturnForbidden_When_CreatingPedidoWithoutClienteRole() throws Exception {
        // When & Then: a classe roda como ADMIN, que não está no @PreAuthorize do POST
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadPedidoValido(1))))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------------
    // GET /pedidos/{id}
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Deve buscar pedido por ID existente")
    void should_ReturnPedido_When_IdExists() throws Exception {
        // Given
        Long pedidoId = criarPedidoViaApi();

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$.clienteId", is(clienteExistente().getId().intValue())))
                .andExpect(jsonPath("$.statusPedido", is("PENDENTE")));
    }

    @Test
    @DisplayName("Deve retornar 404 quando pedido não existe")
    void should_ReturnNotFound_When_PedidoNotExists() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Pedido não encontrado")));
    }

    // ---------------------------------------------------------------------
    // GET /pedidos/cliente/{clienteId} e GET /pedidos/meus
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Deve listar os pedidos de um cliente")
    void should_ReturnPedidos_When_ListingByCliente() throws Exception {
        // Given
        Long pedidoId = criarPedidoViaApi();
        Long clienteId = clienteExistente().getId();

        // When & Then: o endpoint retorna uma lista simples (não paginada)
        mockMvc.perform(get(BASE_URL + "/cliente/{clienteId}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[*].id", hasItem(pedidoId.intValue())))
                .andExpect(jsonPath("$[*].clienteId", everyItem(is(clienteId.intValue()))));
    }

    @Test
    @DisplayName("Deve listar os pedidos do cliente autenticado com paginação")
    void should_ReturnMeusPedidos_When_ClienteAuthenticated() throws Exception {
        // Given
        Long pedidoId = criarPedidoViaApi();

        // When & Then: retorno é um Page, então o conteúdo fica em "content"
        mockMvc.perform(get(BASE_URL + "/meus")
                        .with(comoCliente())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[*].id", hasItem(pedidoId.intValue())));
    }

    // ---------------------------------------------------------------------
    // PATCH /pedidos/{id}/status
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Deve atualizar status do pedido em transição permitida")
    void should_UpdateStatus_When_TransitionIsAllowed() throws Exception {
        // Given
        Long pedidoId = criarPedidoViaApi();

        // When & Then: PENDENTE -> CONFIRMADO (classe roda como ADMIN)
        mockMvc.perform(patch(BASE_URL + "/{id}/status", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CONFIRMADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$.statusPedido", is("CONFIRMADO")));
    }

    @Test
    @DisplayName("Deve rejeitar transição de status inválida")
    void should_ReturnBadRequest_When_TransitionIsNotAllowed() throws Exception {
        // Given
        Long pedidoId = criarPedidoViaApi();

        // When & Then: PENDENTE -> ENTREGUE pula etapas.
        // BusinessException -> 400 no GlobalExceptionHandler.
        mockMvc.perform(patch(BASE_URL + "/{id}/status", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ENTREGUE"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 403 quando CLIENTE tenta atualizar status")
    void should_ReturnForbidden_When_ClienteUpdatesStatus() throws Exception {
        // Given
        Long pedidoId = criarPedidoViaApi();

        // When & Then
        mockMvc.perform(patch(BASE_URL + "/{id}/status", pedidoId)
                        .with(comoCliente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CONFIRMADO"))))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------------
    // DELETE /pedidos/{id}
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Deve cancelar pedido PENDENTE (soft delete)")
    void should_CancelPedido_When_StatusIsPendente() throws Exception {
        // Given
        Long pedidoId = criarPedidoViaApi();

        // When
        mockMvc.perform(delete(BASE_URL + "/{id}", pedidoId))
                .andExpect(status().isNoContent());

        // Then: o pedido continua existindo, mas como CANCELADO
        mockMvc.perform(get(BASE_URL + "/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPedido", is("CANCELADO")));
    }

    @Test
    @DisplayName("Deve rejeitar cancelamento de pedido fora de PENDENTE/CONFIRMADO")
    void should_RejectCancel_When_StatusDoesNotAllow() throws Exception {
        // Given: PENDENTE -> CONFIRMADO -> PREPARANDO
        Long pedidoId = criarPedidoViaApi();
        atualizarStatusViaApi(pedidoId, "CONFIRMADO");
        atualizarStatusViaApi(pedidoId, "PREPARANDO");

        // When & Then: BusinessException -> 400
        mockMvc.perform(delete(BASE_URL + "/{id}", pedidoId))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------
    // POST /pedidos/calcular
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Deve calcular o total dos itens sem criar pedido")
    void should_CalculateTotal_When_ValidItens() throws Exception {
        // Given
        Produto produto = produtoExistente();
        int quantidade = 3;
        List<Map<String, Object>> itens = List.of(Map.of(
                "produtoId", produto.getId(),
                "quantidade", quantidade
        ));
        BigDecimal esperado = produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
        long pedidosAntes = mockMvcContarPedidosDoCliente();

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL + "/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itens)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: compareTo ignora diferença de escala (ex.: 30 vs 30.00)
        BigDecimal total = new BigDecimal(result.getResponse().getContentAsString());
        assertThat(total).isEqualByComparingTo(esperado);

        // E nenhum pedido foi persistido
        assertThat(mockMvcContarPedidosDoCliente()).isEqualTo(pedidosAntes);
    }

    @Test
    @DisplayName("Deve retornar 400 ao calcular com item inválido")
    void should_ReturnBadRequest_When_CalculatingWithInvalidItem() throws Exception {
        // Given: quantidade zero (premissa: ItemPedidoReqDTO tem @Positive/@Min(1))
        List<Map<String, Object>> itens = List.of(Map.of(
                "produtoId", produtoExistente().getId(),
                "quantidade", 0
        ));

        // When & Then
        mockMvc.perform(post(BASE_URL + "/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itens)))
                .andExpect(status().isBadRequest());
    }

    private long mockMvcContarPedidosDoCliente() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/cliente/{clienteId}", clienteExistente().getId()))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).size();
    }

}