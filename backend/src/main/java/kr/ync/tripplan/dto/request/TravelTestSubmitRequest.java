package kr.ync.tripplan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import kr.ync.tripplan.domain.TravelType;

import java.util.List;

public record TravelTestSubmitRequest(@NotEmpty List<TravelType> selectedTypes) {}