package kr.ync.tripplan.traveltest.dto;

import jakarta.validation.constraints.NotEmpty;
import kr.ync.tripplan.traveltest.TravelType;

import java.util.List;

public record TravelTestSubmitRequest(@NotEmpty List<TravelType> selectedTypes) {}