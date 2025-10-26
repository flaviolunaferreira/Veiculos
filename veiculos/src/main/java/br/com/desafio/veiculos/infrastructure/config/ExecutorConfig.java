package br.com.desafio.veiculos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean(name = "analysisTaskExecutor")
    public ExecutorService analysisTaskExecutor() {
        // Pool de threads para execução paralela das chamadas aos fornecedores
        // Em produção, considerar ThreadPoolTaskExecutor do Spring para melhor gerenciamento
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "springTaskExecutor")
    public ThreadPoolTaskExecutor springTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }
}
