package com.diceduel.mapper;

import com.diceduel.dto.RoundResponse;
import com.diceduel.entity.RoundEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RoundMapper {

    public RoundResponse toResponse(RoundEntity round) {
        return new RoundResponse(
                round.getId(),
                round.getStatus(),
                new ArrayList<>(round.getDice()),
                new ArrayList<>(round.getLocked())
        );
    }
}
