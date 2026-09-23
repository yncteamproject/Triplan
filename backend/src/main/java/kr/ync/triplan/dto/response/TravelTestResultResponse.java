package kr.ync.triplan.dto.response;

import kr.ync.triplan.domain.TravelType;

public record TravelTestResultResponse(
        TravelType travelType,
        String displayName,
        String description
) {}