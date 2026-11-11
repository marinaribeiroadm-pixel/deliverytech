package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.config.ModelMapperConfig;
import com.deliverytech.delivery_api.dto.req.ItemPedidoReqDTO;
import com.deliverytech.delivery_api.dto.req.PedidoReqDTO;
import com.deliverytech.delivery_api.dto.res.PedidoResDTO;
import com.deliverytech.delivery_api.enums.StatusPedido;
import com.deliverytech.delivery_api.exeption.BusinessException;
import com.deliverytech.delivery_api.exeption.EntityNotFoundException;
import com.deliverytech.delivery_api.model.Cliente;
import com.deliverytech.delivery_api.model.ItemPedido;
import com.deliverytech.delivery_api.model.Pedido;
import com.deliverytech.delivery_api.model.Produto;
import com.deliverytech.delivery_api.model.Restaurante;
import com.deliverytech.delivery_api.repository.ClienteRepository;
import com.deliverytech.delivery_api.repository.PedidoRepository;
import com.deliverytech.delivery_api.repository.ProdutoRepository;
import com.deliverytech.delivery_api.repository.RestauranteRepository;
import com.deliverytech.delivery_api.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Teste do Pedido Service")
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    // ModelMapper real com a MESMA configuração de produção (STRICT),
    // para validar de verdade o mapeamento cliente.id -> clienteId etc.
    @Spy
    private ModelMapper modelMapper = new ModelMapperConfig().modelMapper();

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Cliente cliente;
    private Restaurante restaurante;
    private Produto produto;

    @BeforeEach
    void setup() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setAtivo(true);

        restaurante = new Restaurante();
        restaurante.setId(10L);
        restaurante.setNome("Pizza Nona");
        restaurante.setTaxaEntrega(new BigDecimal("5.00"));
        restaurante.setAtivo(true);

        produto = new Produto();
        produto.setId(100L);
        produto.setNome("Pizza Margherita");
        produto.setPreco(new BigDecimal("45.00"));
        produto.setDisponivel(true);
        produto.setRestaurante(restaurante);
    }

    private ItemPedidoReqDTO criarItemDto(Long produtoId, int quantidade) {
        ItemPedidoReqDTO item = new ItemPedidoReqDTO();
        item.setProdutoId(produtoId);
        item.setQuantidade(quantidade);
        return item;
    }

    private PedidoReqDTO criarDto(int quantidade) {
        PedidoReqDTO dto = new PedidoReqDTO();
        dto.setClienteId(1L);
        dto.setRestauranteId(10L);
        dto.setEnderecoEntrega("Rua A, 100");
        dto.setItens(List.of(criarItemDto(100L, quantidade)));
        return dto;
    }

    private Pedido criarPedido(StatusPedido status) {
        Pedido pedido = new Pedido();
        pedido.setId(50L);
        pedido.setNumeroPedido("PED-TESTE");
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatusPedido(status);
        return pedido;
    }

    // Stubs comuns do caminho feliz do criarPedido
    private void stubCriacaoValida() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));
    }

    // ---------- criarPedido ----------

    @Test
    @DisplayName("Deve criar pedido com dados válidos calculando subtotal e total")
    void should_CreatePedido_When_ValidData() {
        // Given
        stubCriacaoValida();
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });

        // When
        PedidoResDTO resultado = pedidoService.criarPedido(criarDto(2));

        // Then: 2 x 45.00 = 90.00 + taxa 5.00 = 95.00
        assertNotNull(resultado);
        assertEquals(50L, resultado.getId());
        assertEquals(StatusPedido.PENDENTE, resultado.getStatusPedido());
        assertEquals(1L, resultado.getClienteId());
        assertEquals(10L, resultado.getRestauranteId());
        assertNotNull(resultado.getNumeroPedido());
        assertEquals(0, new BigDecimal("90.00").compareTo(resultado.getSubtotal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(resultado.getTaxaEntrega()));
        assertEquals(0, new BigDecimal("95.00").compareTo(resultado.getValorTotal()));

        // E os itens foram montados e vinculados ao pedido
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        Pedido salvo = captor.getValue();
        assertEquals("Rua A, 100", salvo.getEnderecoEntrega());
        assertEquals(1, salvo.getItens().size());

        ItemPedido item = salvo.getItens().get(0);
        assertSame(salvo, item.getPedido());
        assertSame(produto, item.getProduto());
        assertEquals(2, item.getQuantidade());
        assertEquals(0, new BigDecimal("45.00").compareTo(item.getPrecoUnitario()));
        assertEquals(0, new BigDecimal("90.00").compareTo(item.getPrecoTotal()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não existe")
    void should_ThrowException_When_ClienteNotExists() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> pedidoService.criarPedido(criarDto(1)));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente está inativo")
    void should_ThrowException_When_ClienteIsInactive() {
        // Given
        cliente.setAtivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> pedidoService.criarPedido(criarDto(1)));

        assertEquals("Cliente inativo não pode fazer pedidos", exception.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando restaurante não existe")
    void should_ThrowException_When_RestauranteNotExists() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> pedidoService.criarPedido(criarDto(1)));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando restaurante está inativo")
    void should_ThrowException_When_RestauranteIsInactive() {
        // Given
        restaurante.setAtivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));

        // When & Then
        assertThrows(BusinessException.class,
            () -> pedidoService.criarPedido(criarDto(1)));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void should_ThrowException_When_ProdutoNotExists() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));
        when(produtoRepository.findById(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> pedidoService.criarPedido(criarDto(1)));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto está indisponível")
    void should_ThrowException_When_ProdutoIsUnavailable() {
        // Given
        produto.setDisponivel(false);
        stubCriacaoValida();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> pedidoService.criarPedido(criarDto(1)));

        assertEquals("Produto indisponível: Pizza Margherita", exception.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto é de outro restaurante")
    void should_ThrowException_When_ProdutoBelongsToAnotherRestaurante() {
        // Given
        Restaurante outro = new Restaurante();
        outro.setId(99L);
        produto.setRestaurante(outro);
        stubCriacaoValida();

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> pedidoService.criarPedido(criarDto(1)));

        assertEquals("Produto não pertence ao restaurante selecionado", exception.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    // ---------- buscas ----------

    @Test
    @DisplayName("Deve buscar pedido por ID existente")
    void should_ReturnPedido_When_IdExists() {
        // Given
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(criarPedido(StatusPedido.PENDENTE)));

        // When
        PedidoResDTO resultado = pedidoService.buscarPedidoPorId(50L);

        // Then
        assertNotNull(resultado);
        assertEquals(50L, resultado.getId());
        assertEquals("PED-TESTE", resultado.getNumeroPedido());
        assertEquals(1L, resultado.getClienteId());
        verify(pedidoRepository).findById(50L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existe")
    void should_ThrowException_When_PedidoNotExists() {
        // Given
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> pedidoService.buscarPedidoPorId(999L));

        assertEquals("Pedido não encontrado com ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar os pedidos de um cliente")
    void should_ReturnPedidos_When_ListingByCliente() {
        // Given
        when(pedidoRepository.findByClienteIdComItens(1L))
            .thenReturn(List.of(criarPedido(StatusPedido.PENDENTE)));

        // When
        List<PedidoResDTO> resultado = pedidoService.buscarPedidosPorCliente(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getClienteId());
        verify(pedidoRepository).findByClienteIdComItens(1L);
    }

    @Test
    @DisplayName("Deve listar os pedidos do usuário autenticado com paginação")
    void should_ReturnMeusPedidos_When_ClienteExists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> pagina = new PageImpl<>(List.of(criarPedido(StatusPedido.PENDENTE)), pageable, 1);
        when(clienteRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findByClienteIdOrderByDataPedidoDesc(1L, pageable)).thenReturn(pagina);

        // When
        Page<PedidoResDTO> resultado = pedidoService.listarMeusPedidos("joao@email.com", pageable);

        // Then
        assertEquals(1, resultado.getTotalElements());
        assertEquals(50L, resultado.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar meus pedidos sem cliente vinculado ao email")
    void should_ThrowException_When_UsuarioHasNoCliente() {
        // Given
        when(clienteRepository.findByEmail("sem.cliente@email.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> pedidoService.listarMeusPedidos("sem.cliente@email.com", PageRequest.of(0, 10)));
        verify(pedidoRepository, never()).findByClienteIdOrderByDataPedidoDesc(any(), any());
    }

    // ---------- atualizarStatusPedido ----------

    @Test
    @DisplayName("Deve atualizar status em transição permitida")
    void should_UpdateStatus_When_TransitionIsAllowed() {
        // Given
        Pedido pedido = criarPedido(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        PedidoResDTO resultado = pedidoService.atualizarStatusPedido(50L, StatusPedido.CONFIRMADO);

        // Then
        assertEquals(StatusPedido.CONFIRMADO, resultado.getStatusPedido());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("Deve rejeitar transição de status inválida")
    void should_ThrowException_When_TransitionIsNotAllowed() {
        // Given: PENDENTE -> ENTREGUE pula etapas
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(criarPedido(StatusPedido.PENDENTE)));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> pedidoService.atualizarStatusPedido(50L, StatusPedido.ENTREGUE));

        assertEquals("Transição de status inválida: PENDENTE -> ENTREGUE", exception.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar status de pedido inexistente")
    void should_ThrowException_When_UpdatingStatusOfNonExistentPedido() {
        // Given
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> pedidoService.atualizarStatusPedido(999L, StatusPedido.CONFIRMADO));
        verify(pedidoRepository, never()).save(any());
    }

    // ---------- cancelarPedido ----------

    @Test
    @DisplayName("Deve cancelar pedido PENDENTE")
    void should_CancelPedido_When_StatusIsPendente() {
        // Given
        Pedido pedido = criarPedido(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        // When
        pedidoService.cancelarPedido(50L);

        // Then
        assertEquals(StatusPedido.CANCELADO, pedido.getStatusPedido());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("Deve rejeitar cancelamento de pedido em PREPARANDO")
    void should_ThrowException_When_CancelNotAllowed() {
        // Given
        Pedido pedido = criarPedido(StatusPedido.PREPARANDO);
        when(pedidoRepository.findById(50L)).thenReturn(Optional.of(pedido));

        // When & Then
        assertThrows(BusinessException.class, () -> pedidoService.cancelarPedido(50L));
        assertEquals(StatusPedido.PREPARANDO, pedido.getStatusPedido());
        verify(pedidoRepository, never()).save(any());
    }

    // ---------- calcularTotalPedido ----------

    @Test
    @DisplayName("Deve calcular o total dos itens sem a taxa de entrega")
    void should_CalculateTotal_When_ValidItens() {
        // Given
        Produto outro = new Produto();
        outro.setId(200L);
        outro.setPreco(new BigDecimal("10.50"));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));
        when(produtoRepository.findById(200L)).thenReturn(Optional.of(outro));

        // When: 2 x 45.00 + 3 x 10.50 = 121.50
        BigDecimal total = pedidoService.calcularTotalPedido(
            List.of(criarItemDto(100L, 2), criarItemDto(200L, 3)));

        // Then
        assertEquals(0, new BigDecimal("121.50").compareTo(total));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao calcular com produto inexistente")
    void should_ThrowException_When_CalculatingWithNonExistentProduto() {
        // Given
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
            () -> pedidoService.calcularTotalPedido(List.of(criarItemDto(999L, 1))));
    }
}
