package com.WebClient.service;

import com.WebClient.dto.UserDto;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class UserService {
    private final WebClient client;

    public UserService(WebClient client){
        this.client = client;
    }

    private boolean isTransient(Throwable t){
        return t instanceof WebClientResponseException.ServiceUnavailable
                || t instanceof WebClientResponseException.InternalServerError
                || t instanceof ReadTimeoutException;
    }

    public Flux<UserDto> list() {
       return client.get()
               .uri("/users")
               .retrieve()
               .onStatus(HttpStatusCode::is4xxClientError, res ->
                       res.bodyToMono(String.class)
                               .defaultIfEmpty("client error")
                               .flatMap(msg -> Mono.error(new IllegalArgumentException(msg)))
               )
               .onStatus(HttpStatusCode::is5xxServerError, res ->
                       res.createException().flatMap(Mono::error)
               )
               .bodyToFlux(UserDto.class)
               .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                       .filter(this::isTransient))
               .timeout(Duration.ofSeconds(3));
    }
    public Mono<UserDto> getById(Long id){
        return client.get()
                .uri("/users/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.createException().flatMap(Mono::error))
                .bodyToMono(UserDto.class);
    }

    public Mono<UserDto> create(UserDto newUserDto){
        return client.post()
                .uri("/users")
                .bodyValue(newUserDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.createException().flatMap(Mono::error))
                .bodyToMono(UserDto.class);
    }

    public Mono<UserDto> update(Long id, UserDto userDto){
        return client.put()
                .uri("/users/{id}", id)
                .bodyValue(userDto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.createException().flatMap(Mono::error))
                .bodyToMono(UserDto.class);
    }

    public Mono<Void> delete(Long id){
        return client.delete()
                .uri("/users/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.createException().flatMap(Mono::error))
                .bodyToMono(Void.class);
    }

}
