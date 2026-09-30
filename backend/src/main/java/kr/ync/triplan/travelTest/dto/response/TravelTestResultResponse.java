package kr.ync.triplan.travelTest.dto.response;

import kr.ync.triplan.travelTest.domain.TravelType;

public record TravelTestResultResponse(
        TravelType travelType,
        String displayName,
        String description
) {}