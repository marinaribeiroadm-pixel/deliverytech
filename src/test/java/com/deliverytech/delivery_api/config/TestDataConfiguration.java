package com.deliverytech.delivery_api.config;

import com.deliverytech.delivery_api.model.Cliente;
import com.deliverytech.delivery_api.model.Produto;
import com.deliverytech.delivery_api.model.Restaurante;
import com.deliverytech.delivery_api.repository.ClienteRepository;
import com.deliverytech.delivery_api.repository.PedidoRepository;
import com.deliverytech.delivery_api.repository.ProdutoRepository;
import com.deliverytech.delivery_api.repository.RestauranteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.math.BigDecimal;

/*
 * Massa de dados dos testes de integração, recriada antes de cada teste.
 * Resultado: exatamente 1 cliente, 1 restaurante e 1 produto (desse restaurante),
 * para que findAll().get(0) nos testes seja determinístico.
 */
@TestConfiguration
public class TestDataConfiguration {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeTestMethod
    public void setupTestData() {
        // Limpar na ordem das FKs: pedido (e itens, via cascade) -> produto -> restaurante/cliente.
        // Remove também o que o data.sql inseriu.
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        clienteRepository.deleteAll();

        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente.test@gmail.com");
        cliente.setTelefone("11123456789");
        cliente.setEndereco("Rua Teste, 1 - Centro");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante Teste");
        restaurante.setCategoria("Categoria Teste");
        restaurante.setEndereco("Av. Teste, 100 - Centro");
        restaurante.setTelefone("1130001111");
        restaurante.setTaxaEntrega(BigDecimal.valueOf(5.00));
        restaurante.setAtivo(true);
        restauranteRepository.save(restaurante);

        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição do Produto Teste");
        produto.setCategoria("Categoria Teste");
        produto.setPreco(BigDecimal.valueOf(10.99));
        // boolean primitivo: sem isso o produto nasce indisponível
        produto.setDisponivel(true);
        produto.setRestaurante(restaurante);
        produtoRepository.save(produto);
    }

}
