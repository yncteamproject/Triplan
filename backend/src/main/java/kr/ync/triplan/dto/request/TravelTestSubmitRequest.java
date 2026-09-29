package kr.ync.triplan.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kr.ync.triplan.domain.TravelType;

import java.util.List;

public record TravelTestSubmitRequest(@NotEmpty(message = "답변을 선택해주세요")
                                      List<@NotNull(message = "선택하지 않은 답변이 있습니다") TravelType> selectedTypes) {}