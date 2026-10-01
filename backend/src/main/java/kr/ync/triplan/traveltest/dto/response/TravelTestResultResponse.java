package kr.ync.triplan.traveltest.dto.response;

import kr.ync.triplan.traveltest.domain.TravelType;

public record TravelTestResultResponse(
        TravelType travelType,
        String displayName,
        String description
) {}