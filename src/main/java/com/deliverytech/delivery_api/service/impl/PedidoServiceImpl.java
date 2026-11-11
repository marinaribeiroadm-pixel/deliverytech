package com.deliverytech.delivery_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.deliverytech.delivery_api.service.PedidoService;

/*
 * Exceções:
 * - EntityNotFoundException (do pacote exeption, NÃO a do jakarta.persistence) -> 404
 * - BusinessException (regra de negócio violada) -> 400
 * O GlobalExceptionHandler só conhece essas duas; qualquer outra vira 500.
 */
@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PedidoResDTO criarPedido(PedidoReqDTO dto) {
        // 1. Validar cliente existe e está ativo
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
            .orElseThrow(() -> new EntityNotFoundException("Cliente", dto.getClienteId()));

        if (!cliente.isAtivo()) {
            throw new BusinessException("Cliente inativo não pode fazer pedidos");
        }

        // 2. Validar restaurante existe e está ativo
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
            .orElseThrow(() -> new EntityNotFoundException("Restaurante", dto.getRestauranteId()));

        if (!restaurante.isAtivo()) {
            throw new BusinessException("Restaurante não está disponível");
        }

        Pedido pedido = new Pedido();

        // 3. Validar produtos e montar os itens com o preço vigente do produto
        List<ItemPedido> itensPedido = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemPedidoReqDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto", itemDTO.getProdutoId()));

            if (!produto.isDisponivel()) {
                throw new BusinessException("Produto indisponível: " + produto.getNome());
            }

            if (produto.getRestaurante() == null
                    || !produto.getRestaurante().getId().equals(restaurante.getId())) {
                throw new BusinessException("Produto não pertence ao restaurante selecionado");
            }

            BigDecimal precoTotal = produto.getPreco()
                .multiply(BigDecimal.valueOf(itemDTO.getQuantidade()));

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setPrecoTotal(precoTotal);
            itensPedido.add(item);

            subtotal = subtotal.add(precoTotal);
        }

        // 4. Calcular total do pedido (taxa nula é tratada como zero)
        BigDecimal taxaEntrega = restaurante.getTaxaEntrega() != null
            ? restaurante.getTaxaEntrega()
            : BigDecimal.ZERO;

        // 5. Salvar pedido; os itens são persistidos pelo cascade de Pedido.itens
        pedido.setNumeroPedido(gerarNumeroPedido());
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());
        pedido.setItens(itensPedido);
        pedido.setSubtotal(subtotal);
        pedido.setTaxaEntrega(taxaEntrega);
        pedido.setValorTotal(subtotal.add(taxaEntrega));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        return modelMapper.map(pedidoSalvo, PedidoResDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResDTO buscarPedidoPorId(Long id) {
        return modelMapper.map(buscarPedido(id), PedidoResDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResDTO> buscarPedidosPorCliente(Long clienteId) {
        // Consulta filtrada no banco (antes era findAll() + filtro em memória)
        return pedidoRepository.findByClienteIdComItens(clienteId).stream()
            .map(pedido -> modelMapper.map(pedido, PedidoResDTO.class))
            .toList();
    }

    @Override
    public PedidoResDTO atualizarStatusPedido(Long id, StatusPedido novoStatus) {
        Pedido pedido = buscarPedido(id);

        StatusPedido statusAtual = pedido.getStatusPedido();
        if (!isTransicaoValida(statusAtual, novoStatus)) {
            throw new BusinessException("Transição de status inválida: " + statusAtual + " -> " + novoStatus);
        }

        pedido.setStatusPedido(novoStatus);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        return modelMapper.map(pedidoAtualizado, PedidoResDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPedido(List<ItemPedidoReqDTO> itens) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoReqDTO item : itens) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto", item.getProdutoId()));

            total = total.add(produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        return total;
    }

    @Override
    public void cancelarPedido(Long id) {
        Pedido pedido = buscarPedido(id);

        StatusPedido statusAtual = pedido.getStatusPedido();
        if (!podeSerCancelado(statusAtual)) {
            throw new BusinessException("Pedido não pode ser cancelado no status: " + statusAtual);
        }

        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResDTO> listarPorCliente(Long clienteId, Pageable pageable) {
        return pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId, pageable)
            .map(p -> modelMapper.map(p, PedidoResDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResDTO> listarMeusPedidos(String emailUsuario, Pageable pageable) {
        Cliente cliente = clienteRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado para o usuário autenticado"));
        return listarPorCliente(cliente.getId(), pageable);
    }

    // ---------------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------------

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
    }

    // Número legível e único o bastante para identificar o pedido (ex.: PED-1A2B3C4D)
    private String gerarNumeroPedido() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isTransicaoValida(StatusPedido statusAtual, StatusPedido novoStatus) {
        if (statusAtual == null || novoStatus == null) {
            return false;
        }
        return switch (statusAtual) {
            case PENDENTE -> novoStatus == StatusPedido.CONFIRMADO || novoStatus == StatusPedido.CANCELADO;
            case CONFIRMADO -> novoStatus == StatusPedido.PREPARANDO || novoStatus == StatusPedido.CANCELADO;
            case PREPARANDO -> novoStatus == StatusPedido.SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA -> novoStatus == StatusPedido.ENTREGUE;
            default -> false;
        };
    }

    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }
}
