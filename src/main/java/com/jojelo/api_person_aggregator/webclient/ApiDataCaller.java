package com.jojelo.api_person_aggregator.webclient;

import com.jojelo.api_person_aggregator.webclient.dto.PersonDocumentResponse;
import com.jojelo.api_person_aggregator.webclient.dto.PersonEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiDataCaller {

    private final WebClient.Builder webClientBuilder;

    public Mono<PersonDocumentResponse> getPersonDocumentById(Integer id) {
        return webClientBuilder
                .baseUrl("http://localhost:8091/api/v1")
                .build()
                .get()
                .uri("/person-document/{id}", id)
                .retrieve()
                .bodyToMono(PersonDocumentResponse.class)
                .doOnSuccess(person -> log.info("Person document retrieved: {}", person))
                .doOnError(error -> log.error("Error retrieving person document", error))
                .flatMap(response -> {
                    if (response == null || response.getDocument() == null) {
                        log.error("Error retrieving person document: Document is null");
                        return Mono.error(new RuntimeException("Document is null"));
                    }
                    return Mono.just(response);
                });
    }

    public Mono<PersonEmailResponse> getPersonEmailById(Integer id) {
        return webClientBuilder
                .baseUrl("http://localhost:8091/api/v1")
                .build()
                .get()
                .uri("/person-email/{id}", id)
                .retrieve()
                .bodyToMono(PersonEmailResponse.class)
                .doOnSuccess(person -> log.info("Person email retrieved: {}", person))
                .doOnError(error -> log.error("Error retrieving person email", error))
                .onErrorResume(e -> {
                    log.error("Error al obtener el documento de la persona con ID {}: {}", id, e.getMessage());
                    return Mono.just(PersonEmailResponse.builder()
                            .id(id.longValue())
                            .email("N/A")
                            .build());
                });
    }
}
