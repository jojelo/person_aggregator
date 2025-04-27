package com.jojelo.api_person_aggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jojelo.api_person_aggregator.dto.PersonAggregatorRequest;
import com.jojelo.api_person_aggregator.dto.PersonaRedisResponse;
import com.jojelo.api_person_aggregator.entity.PersonAggregatorEntity;
import com.jojelo.api_person_aggregator.repository.PersonAggregatorRepository;
import com.jojelo.api_person_aggregator.webclient.ApiDataCaller;
import com.jojelo.api_person_aggregator.webclient.dto.PersonDocumentResponse;
import com.jojelo.api_person_aggregator.webclient.dto.PersonEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonAggregatorServiceImpl implements PersonAggregatorService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ApiDataCaller apiDataCaller;
    private final PersonAggregatorRepository personAggregatorRepository;

    private static final String PERSON_REDIS_KEY = "persona:";

    @Override
    public Mono<Integer> getAllPerson(List<String> uuids) {
        return Flux.fromIterable(uuids)
                .flatMap(this::getPersonFromRedis)
                .filter(redisAgeResponse -> redisAgeResponse.getAge() > 18)
                .flatMap(personFromRedis -> {
                    Integer id = personFromRedis.getId();

                    Mono<PersonDocumentResponse> personDocumentMono = apiDataCaller.getPersonDocumentById(id);
                    Mono<PersonEmailResponse> personEmailMono = apiDataCaller.getPersonEmailById(id);

                    return Mono.zip(personDocumentMono, personEmailMono)
                            .map(tuple -> {
                                PersonDocumentResponse personDocument = tuple.getT1();
                                PersonEmailResponse personEmail = tuple.getT2();

                                return PersonAggregatorRequest.builder()
                                        .identifier(personDocument.getId())
                                        .email(personEmail.getEmail())
                                        .documentNumber(personDocument.getDocument())
                                        .build();
                            });

                })
                .map(personAggregatorRequest -> PersonAggregatorEntity.builder()
                        .identifier(personAggregatorRequest.getIdentifier())
                        .email(personAggregatorRequest.getEmail())
                        .documentNumber(personAggregatorRequest.getDocumentNumber())
                        .build()
                )
                .collectList()
                .flatMap(entityList -> personAggregatorRepository.saveAll(entityList)
                        .collectList()
                .map(savedEntities -> {
                            log.info("Se guardaron {} entidades en la base de datos", savedEntities.size());
                            return savedEntities.size();
                        })
                );

    }

    private Mono<PersonaRedisResponse> getPersonFromRedis(String uuid) {
        String key = PERSON_REDIS_KEY + uuid;
        return reactiveRedisTemplate.opsForValue().get(key)
                .flatMap(jsonString -> {
                    try {
                        return Mono.just(objectMapper.readValue(jsonString, PersonaRedisResponse.class));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error al deserializar JSON", e));
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Persona no encontrada en Redis con UUID: " + uuid)));
    }
}
