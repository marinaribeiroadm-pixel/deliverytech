package com.deliverytech.delivery_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deliverytech.delivery_api.dto.req.ItemPedidoReqDTO;
import com.deliverytech.delivery_api.dto.req.PedidoReqDTO;
import com.deliverytech.delivery_api.dto.res.PedidoResDTO;
import com.deliverytech.delivery_api.enums.StatusPedido;
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

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.lang.reflect.Method;

@Service
@Transactional 
public class PedidoServiceImpl implements PedidoService  {
    
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
    @Transactional
    public PedidoResDTO criarPedido(PedidoReqDTO dto) {
        // 1. Validar cliente existe e está ativo
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
        /*
        .orElseThrow se ele não encontrar eke vai lançar um execessão
        através da entidade EntityNotFoundException
        informando a message, Cliente não encontrado
        */
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        /*
        Se eencontrar ele vai buscar dentro do nosso condicional o nosso if orElseThrow
        se esse cliente não está ativo lança uma execessão passando msg
        (!cliente está diferente de ativo)
        */
        if (!cliente.isAtivo()) {
            throw new IllegalArgumentException("Cliente inativo não pode fazer pedidos");
        }

        // 2. Validar restaurante existe e está ativo
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
            .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        if (!restaurante.isAtivo()) {
            throw new IllegalArgumentException("Restaurante não está disponível");
        }

        // 3. Validar todos os produtos existem e estão disponíveis
        List<ItemPedido> itensPedido = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemPedidoReqDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

            if (!produto.isDisponivel()) {
                throw new IllegalArgumentException("Produto indisponível: " + produto.getNome());
            }

            if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {
                throw new IllegalArgumentException("Produto não pertence ao restaurante selecionado");
            }
        
    }

        // 4. Calcular total do pedido
        BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
        BigDecimal valorTotal = subtotal.add(taxaEntrega);

        // 5. Salvar pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        setPedidoStatus(pedido, StatusPedido.PENDENTE);
        setProperty(pedido, "EnderecoEntrega", dto.getEnderecoEntrega());
        pedido.setSubtotal(subtotal);
        pedido.setTaxaEntrega(taxaEntrega);
        pedido.setValorTotal(valorTotal);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 6. Salvar itens do pedido
        for (ItemPedido item : itensPedido) {
            item.setPedido(pedidoSalvo);
        }
        pedidoSalvo.setItens(itensPedido);

        // 7. Atualizar estoque (se aplicável) - Simulação
        // Em um cenário real, aqui seria decrementado o estoque

        // 8. Retornar pedido criado
        return modelMapper.map(pedidoSalvo, PedidoResDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResDTO buscarPedidoPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));

        return modelMapper.map(pedido, PedidoResDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResDTO> buscarPedidosPorCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
            .filter(pedido -> pedido.getCliente() != null
                && clienteId.equals(pedido.getCliente().getId()))
            .collect(Collectors.toList());

        return pedidos.stream()
            .map(pedido -> modelMapper.map(pedido, PedidoResDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    public PedidoResDTO atualizarStatusPedido(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        // Validar transições de status permitidas
        StatusPedido statusAtual = getPedidoStatus(pedido);
        if (!isTransicaoValida(statusAtual, novoStatus)) {
            throw new IllegalArgumentException("Transição de status inválida: " +
            statusAtual + " -> " + novoStatus);
        }

        setPedidoStatus(pedido, novoStatus);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        return modelMapper.map(pedidoAtualizado, PedidoResDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPedido(List<ItemPedidoReqDTO> itens) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoReqDTO item : itens) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            BigDecimal subtotalItem = produto.getPreco()
                .multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotalItem);
        }

        return total;
    }

    @Override
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        StatusPedido statusAtual = getPedidoStatus(pedido);
        if (!podeSerCancelado(statusAtual)) {
            throw new IllegalArgumentException("Pedido não pode ser cancelado no status: " + statusAtual);
        }

        setPedidoStatus(pedido, StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    private StatusPedido getPedidoStatus(Pedido pedido) {
        Object status = getProperty(pedido, "Status");
        return status instanceof StatusPedido ? (StatusPedido) status : null;
    }

    private void setPedidoStatus(Pedido pedido, StatusPedido status) {
        setProperty(pedido, "Status", status);
    }

    private Object getProperty(Object target, String property) {
        try {
            Method method = target.getClass().getMethod("get" + property);
            return method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Propriedade não encontrada: " + property);
        }
    }

    private void setProperty(Object target, String property, Object value) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals("set" + property) && method.getParameterCount() == 1) {
                    method.invoke(target, value);
                    return;
                }
            }
            throw new NoSuchMethodException(property);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Propriedade não encontrada: " + property);
        }
    }

    private boolean isTransicaoValida(StatusPedido statusAtual, StatusPedido novoStatus) {
        // Implementar lógica de transições válidas
        switch (statusAtual) {
            case PENDENTE:
                return novoStatus == StatusPedido.CONFIRMADO || novoStatus == StatusPedido.CANCELADO;
            case CONFIRMADO:
                return novoStatus == StatusPedido.PREPARANDO || novoStatus == StatusPedido.CANCELADO;
            case PREPARANDO:
                return novoStatus == StatusPedido.SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA:
                return novoStatus == StatusPedido.ENTREGUE;
            default:
                return false;
        }
    }

    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }

}