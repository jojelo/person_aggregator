package com.jojelo.api_person_aggregator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "person_aggregator")
public class PersonAggregatorEntity {
    @Id
    private String idMongo;

    @Field("identifier")
    private Long identifier;

    @Field("email")
    private String email;

    @Field("documentNumber")
    private Integer documentNumber;
}
