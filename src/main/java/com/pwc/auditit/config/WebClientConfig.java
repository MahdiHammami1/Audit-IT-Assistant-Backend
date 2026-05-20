package com.pwc.auditit.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient used to communicate with external services like FastAPI
 */
@Configuration
public class WebClientConfig {
    
    private final FastApiProperties fastApiProperties;
    
    public WebClientConfig(FastApiProperties fastApiProperties) {
        this.fastApiProperties = fastApiProperties;
    }
    
    /**
     * Create a WebClient bean for FastAPI communication
     */
    @Bean(name = "fastApiWebClient")
    public WebClient fastApiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, fastApiProperties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(fastApiProperties.getReadTimeoutMs()))
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(fastApiProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(fastApiProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                );

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(fastApiProperties.getMaxInMemorySizeBytes()))
                .build();
        
        return WebClient.builder()
                .baseUrl(fastApiProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}

