package com.jojelo.api_person_aggregator.repository;

import com.jojelo.api_person_aggregator.entity.PersonAggregatorEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonAggregatorRepository extends ReactiveMongoRepository<PersonAggregatorEntity, String> {
}
