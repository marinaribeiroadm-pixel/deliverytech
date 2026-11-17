package com.deliverytech.delivery_api.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expõe um resumo das métricas do MeterRegistry para o dashboard.html.
 * Valores de contadores são acumulados desde o último start da aplicação.
 */
@RestController
@RequestMapping("/dashboard/api")
public class DashboardController {

    private final MeterRegistry meterRegistry;

    public DashboardController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        // Pedidos (desde o último restart)
        metrics.put("pedidos_total", sumCounters("delivery.pedidos.total"));
        metrics.put("pedidos_sucesso", sumCounters("delivery.pedidos.sucesso"));
        metrics.put("pedidos_erro", sumCounters("delivery.pedidos.erro"));
        metrics.put("receita_total", sumCounters("delivery.receita.total") / 100.0);

        // Performance (ms)
        metrics.put("tempo_medio_pedido", meanTimerMs("delivery.pedido.processamento.tempo"));
        metrics.put("tempo_medio_banco", meanTimerMs("delivery.database.consulta.tempo"));

        // Sistema: soma apenas os pools de heap
        metrics.put("memoria_usada", sumGauges("jvm.memory.used", "area", "heap"));
        metrics.put("memoria_max", sumGauges("jvm.memory.max", "area", "heap"));
        metrics.put("cpu_usage", sumGauges("system.cpu.usage"));

        // Estado
        metrics.put("usuarios_ativos", sumGauges("delivery.usuarios.ativos"));
        metrics.put("produtos_estoque", sumGauges("delivery.produtos.estoque"));

        return metrics;
    }

    /** Soma todos os contadores com o nome informado, independente das tags. */
    private double sumCounters(String name) {
        return meterRegistry.find(name).counters().stream()
                .mapToDouble(Counter::count)
                .sum();
    }

    /** Média ponderada (totalTime / count) considerando todos os timers com o nome. */
    private double meanTimerMs(String name) {
        Collection<Timer> timers = meterRegistry.find(name).timers();
        long count = timers.stream().mapToLong(Timer::count).sum();
        if (count == 0) {
            return 0.0;
        }
        double totalMs = timers.stream()
                .mapToDouble(t -> t.totalTime(TimeUnit.MILLISECONDS))
                .sum();
        return totalMs / count;
    }

    /**
     * Soma os gauges com o nome e tags informados (pares chave/valor),
     * ignorando valores indefinidos (NaN) ou negativos (ex.: max = -1).
     */
    private double sumGauges(String name, String... tags) {
        return meterRegistry.find(name).tags(tags).gauges().stream()
                .mapToDouble(Gauge::value)
                .filter(v -> !Double.isNaN(v) && v >= 0)
                .sum();
    }
}
