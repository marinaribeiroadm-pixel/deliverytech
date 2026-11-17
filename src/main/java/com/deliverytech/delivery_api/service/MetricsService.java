package com.deliverytech.delivery_api.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {
    
    /*
    Este serviço é responsável por gerenciar métricas relacionadas ao sistema de entrega.
    Ele utiliza o Micrometer para coletar e expor métricas como contadores, timers e gauges.
    */
    private final MeterRegistry meterRegistry;

    // Contadores
    private final Counter pedidosProcessados;
    private final Counter pedidosComSucesso;
    private final Counter pedidosComErro;
    private final Counter receitaTotal;

    // Timers
    private final Timer tempoProcessamentoPedido;
    private final Timer tempoConsultaBanco;

    // Gauges
    private final AtomicInteger usuariosAtivos = new AtomicInteger(0);
    private final AtomicLong produtosEmEstoque = new AtomicLong(1000);

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Inicializando contadores
        this.pedidosProcessados = meterRegistry.counter("pedidos.processados");
        this.pedidosComSucesso = meterRegistry.counter("pedidos.sucesso");
        this.pedidosComErro = meterRegistry.counter("pedidos.erro");
        this.receitaTotal = meterRegistry.counter("receita.total");

        // Inicializando timers
        this.tempoProcessamentoPedido = meterRegistry.timer("tempo.processamento.pedido");
        this.tempoConsultaBanco = meterRegistry.timer("tempo.consulta.banco");

        // Inicializando gauges
        /*
        Gauges são medidores de pressão que representam valores que pode subir ou descer,
        como o número de usuários ativos ou a quantidade de produtos em estoque.
        */
        Gauge.builder("usuarios.ativos", usuariosAtivos, AtomicInteger::get)
                .description("Número de usuários ativos")
                .register(meterRegistry);

        Gauge.builder("produtos.estoque", produtosEmEstoque, AtomicLong::get)
                .description("Número de produtos em estoque")
                .register(meterRegistry);

    }

    /*
    Classes publicas para expor métodos que incrementam contadores, iniciam e finalizam timers, e atualizam gauges.
    Estes métodos podem ser chamados em diferentes partes do sistema para registrar métricas relevantes.
    */
    public void incrementarPedidosProcessados() {
        pedidosProcessados.increment();
    }
    public void incrementarPedidosComSucesso() {
        pedidosComSucesso.increment();
    }

    public void incrementarPedidosComErro() {
        pedidosComErro.increment();
    }

    public void adicionarReceita(double valor) {
        receitaTotal.increment(valor * 100); // Converter para centavos
    }

    // Métodos para timers
    public Timer.Sample iniciarTimerPedido() {
        return Timer.start(meterRegistry);
    }

    public void finalizarTimerPedido(Timer.Sample sample) {
        sample.stop(tempoProcessamentoPedido);
    }

    // Métodos para gauges
    public void setUsuariosAtivos(int quantidade) {
        usuariosAtivos.set(quantidade);
    }

    public void setProdutosEmEstoque(long quantidade) {
        produtosEmEstoque.set(quantidade);
    }

    public void incrementarRestaurantesCadastradosComSucesso() {
        this.meterRegistry.counter("delivery_restaurantes_cadastrados_total", "resultado", "sucesso").increment();
    }
    
    public void incrementarRestaurantesCadastradosComErro() {
        this.meterRegistry.counter("delivery_restaurantes_cadastrados_total", "resultado", "erro").increment();
    }
}
