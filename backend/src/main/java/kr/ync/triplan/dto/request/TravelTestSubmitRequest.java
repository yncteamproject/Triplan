package kr.ync.triplan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import kr.ync.triplan.domain.TravelType;

import java.util.List;

public record TravelTestSubmitRequest(@NotEmpty List<TravelType> selectedTypes) {}