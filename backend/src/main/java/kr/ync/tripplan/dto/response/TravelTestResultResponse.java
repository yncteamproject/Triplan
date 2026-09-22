package kr.ync.tripplan.dto.response;

import kr.ync.tripplan.domain.TravelType;

public record TravelTestResultResponse(
        TravelType travelType,
        String displayName,
        String description
) {}