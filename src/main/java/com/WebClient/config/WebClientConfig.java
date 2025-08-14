package com.WebClient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient(
            WebClient.Builder builder,
            @Value("${remote.base-url}") String baseUrl,
            @Value("${client.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${client.response-timeout-ms}") int responseTimeoutMs,
            @Value("${client.rw-timeout-seconds}") int rwTimeoutSeconds
    ){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(rwTimeoutSeconds))
                        .addHandlerLast(new ReadTimeoutHandler(rwTimeoutSeconds)));

        ExchangeFilterFunction logRequest =
                ExchangeFilterFunction.ofRequestProcessor(req -> {
                    System.out.println(">>> " + req.method() + req.url());
                    return Mono.just(req);
                });

        ExchangeFilterFunction logResponse =
                ExchangeFilterFunction.ofResponseProcessor(res -> {
                   System.out.println("<<< status: " + res.statusCode());
                   return Mono.just(res);
                });

        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest)
                .filter(logResponse)
                .build();
    }
}
