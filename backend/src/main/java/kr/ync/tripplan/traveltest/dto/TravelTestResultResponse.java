package kr.ync.tripplan.traveltest.dto;

import kr.ync.tripplan.traveltest.TravelType;

public record TravelTestResultResponse(
        TravelType travelType,
        String displayName,
        String description
) {}