package com.jojelo.api_person_aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonAggregatorRequest {
    private Long identifier;
    private String email;
    private Integer documentNumber;
}
