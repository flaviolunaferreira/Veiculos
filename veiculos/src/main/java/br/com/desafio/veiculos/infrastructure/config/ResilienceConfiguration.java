package br.com.desafio.veiculos.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class ResilienceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfiguration.class);

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(400);
    public static final Duration F2_TIMEOUT = Duration.ofMillis(400);

    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(DEFAULT_TIMEOUT)
                .build();
    }

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(10)
                .slidingWindowSize(100)
                .build();
    }

    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100)) // Jitter será adicionado
                .build();
    }
    
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig f1Config = RateLimiterConfig.custom()
                .limitForPeriod(2)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ofMillis(50))
                .build();
                
        return RateLimiterRegistry.of(Map.of("F1", f1Config));
    }

    @Bean
    public RegistryEventConsumer<RateLimiter> rateLimiterLog() {
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<RateLimiter> e) {
                e.getAddedEntry().getEventPublisher().onFailure(event ->
                    log.warn("Rate Limiter Event: {} - {}", event.getRateLimiterName(), event.getEventType())
                );
            }
            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<RateLimiter> e) {}
            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<RateLimiter> e) {}
        };
    }
}

