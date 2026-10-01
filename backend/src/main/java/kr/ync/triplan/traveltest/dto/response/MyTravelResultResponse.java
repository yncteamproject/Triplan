package kr.ync.triplan.traveltest.dto.response;

import kr.ync.triplan.traveltest.domain.TravelPreferenceResult;
import kr.ync.triplan.traveltest.domain.TravelType;

import java.time.LocalDateTime;

public record MyTravelResultResponse(
        TravelType travelType,
        String displayName,
        String description,
        LocalDateTime testedAt
) {
    public static MyTravelResultResponse from(TravelPreferenceResult result){
        TravelType type = result.getTravelType();
        return new MyTravelResultResponse(
                type,
                type.getDisplayName(),
                type.getDescription(),
                result.getTestedAt()
        );
    }
}
