package com.jojelo.api_person_aggregator.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PersonAggregatorService {

    Mono<Integer> getAllPerson(List<String> uuids);
}
