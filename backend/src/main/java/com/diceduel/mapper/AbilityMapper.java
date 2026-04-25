package com.diceduel.mapper;

import com.diceduel.dto.AbilityResponse;
import com.diceduel.entity.AbilityEntity;
import org.springframework.stereotype.Component;

@Component
public class AbilityMapper {

    public AbilityResponse toResponse(AbilityEntity ability) {
        return new AbilityResponse(ability.getId(), ability.getName(), ability.getCost());
    }
}
