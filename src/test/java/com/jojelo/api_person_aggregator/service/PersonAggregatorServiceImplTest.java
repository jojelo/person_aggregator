package com.jojelo.api_person_aggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jojelo.api_person_aggregator.dto.PersonaRedisResponse;
import com.jojelo.api_person_aggregator.entity.PersonAggregatorEntity;
import com.jojelo.api_person_aggregator.repository.PersonAggregatorRepository;
import com.jojelo.api_person_aggregator.webclient.ApiDataCaller;
import com.jojelo.api_person_aggregator.webclient.dto.PersonDocumentResponse;
import com.jojelo.api_person_aggregator.webclient.dto.PersonEmailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonAggregatorServiceImplTest {

    @Mock
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ApiDataCaller apiDataCaller;
    @Mock
    private PersonAggregatorRepository personAggregatorRepository;

    @InjectMocks
    private PersonAggregatorServiceImpl personAggregatorService;

    @Test
    @DisplayName("Prueba exitosa de getAllPerson")
    void getAllPerson_ok() throws JsonProcessingException {
        List<String> uuids = List.of(
                "fed46a36-e4e4-42a8-a347-488c91750c18"
        );
        String jsonRedis = "{\"id\": 1,\"age\": 20}";
//        String jsonRedis2 = "{\"id\": 3,\"age\": 20}";
        PersonDocumentResponse personDocumentResponse = PersonDocumentResponse.builder()
                .id(1L)
                .document(12345678)
                .build();
        PersonEmailResponse personEmailResponse = PersonEmailResponse.builder()
                .id(1L)
                .email("demo@gmail.com")
                .build();
        PersonAggregatorEntity savedEntity = PersonAggregatorEntity.builder()
                .identifier(1L)
                .email("demo@gmail.com")
                .documentNumber(12345678)
                .build();
        ReactiveValueOperations<String, String> valueOperations = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(Mono.just(jsonRedis));

        when(objectMapper.readValue(anyString(), eq(PersonaRedisResponse.class)))
                .thenReturn(new PersonaRedisResponse(1, 20));
        when(apiDataCaller.getPersonDocumentById(anyInt())).thenReturn(Mono.just(personDocumentResponse));
        when(apiDataCaller.getPersonEmailById(anyInt())).thenReturn(Mono.just(personEmailResponse));
        when(personAggregatorRepository.saveAll(any(Iterable.class)))
                .thenReturn(Flux.just(savedEntity));

        Mono<Integer> result = personAggregatorService.getAllPerson(uuids);

        StepVerifier.create(result)
                .assertNext(rest -> {
                    assertEquals(1, rest);
                }).expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Debe manejar UUID no encontrado en Redis")
    void getAllPerson_uuidNotFound() {
        List<String> uuids = List.of("fed46a36-e4e4-42a8-a347-488c91750c20");

        ReactiveValueOperations<String, String> valueOperations = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(Mono.empty());

        Mono<Integer> result = personAggregatorService.getAllPerson(uuids);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Persona no encontrada en Redis"))
                .verify();
    }

    @Test
    @DisplayName("Debe manejar lista vacía de UUIDs")
    void getAllPerson_emptyList() {
        List<String> uuids = List.of();

        when(personAggregatorRepository.saveAll(any(Iterable.class)))
                .thenReturn(Flux.empty());

        Mono<Integer> result = personAggregatorService.getAllPerson(uuids);

        StepVerifier.create(result)
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error al deserializar JSON")
    void getAllPerson_jsonDeserializationError() throws JsonProcessingException {
        List<String> uuids = List.of("fed46a36-e4e4-42a8-a347-488c91750c20");

        ReactiveValueOperations<String, String> valueOperations = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(Mono.just(""));

        when(objectMapper.readValue(anyString(), eq(PersonaRedisResponse.class)))
                .thenThrow(new JsonProcessingException("Error de deserialización") {});

        Mono<Integer> result = personAggregatorService.getAllPerson(uuids);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Error al deserializar JSON"))
                .verify();
    }
}