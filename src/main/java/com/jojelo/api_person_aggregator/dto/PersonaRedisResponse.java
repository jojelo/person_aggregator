package com.jojelo.api_person_aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class PersonaRedisResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 6150397933943980231L;

    private Integer id;
}
