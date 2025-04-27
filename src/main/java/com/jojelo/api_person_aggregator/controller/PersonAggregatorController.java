package com.jojelo.api_person_aggregator.controller;

import com.jojelo.api_person_aggregator.dto.UuidRequest;
import com.jojelo.api_person_aggregator.service.PersonAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/person-aggregator")
@RequiredArgsConstructor
public class PersonAggregatorController {

    private final PersonAggregatorService personAggregatorService;

    @PostMapping
    public Mono<Integer> getAllPerson(@RequestBody UuidRequest uuidRequestDto) {
        return personAggregatorService.getAllPerson(uuidRequestDto.getUuids());
    }
}
