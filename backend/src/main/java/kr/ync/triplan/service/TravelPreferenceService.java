package kr.ync.triplan.service;

import kr.ync.triplan.domain.Member;
import kr.ync.triplan.dto.response.MyTravelResultResponse;
import kr.ync.triplan.exception.MemberNotFoundException;
import kr.ync.triplan.exception.TravelResultNotFoundException;
import kr.ync.triplan.repository.MemberRepository;
import kr.ync.triplan.dto.response.TravelTestResultResponse;
import kr.ync.triplan.dto.request.TravelTestSubmitRequest;
import kr.ync.triplan.repository.TravelPreferenceResultRepository;
import kr.ync.triplan.domain.TravelPreferenceResult;
import kr.ync.triplan.domain.TravelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelPreferenceService {

    private final TravelPreferenceResultRepository resultRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TravelTestResultResponse submit(String email, TravelTestSubmitRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        TravelType type = calculateType(request.selectedTypes());

        resultRepository.save(TravelPreferenceResult.builder()
                .member(member)
                .travelType(type)
                .build());

        return new TravelTestResultResponse(type, type.getDisplayName(), type.getDescription());
    }

    @Transactional(readOnly = true)
    public MyTravelResultResponse getMyResult(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        return resultRepository.findFirstByMemberOrderByIdDesc(member)
                .map(MyTravelResultResponse::from)
                .orElseThrow(TravelResultNotFoundException::new);
    }
    /**
     * 최다 득표 유형을 결과로 반환.
     * 동점일 경우, 마지막으로 선택한 문항 쪽의 유형을 우선시한다("마지막 선택이 결정타").
     */
    private TravelType calculateType(List<TravelType> selectedTypes) {
        if (selectedTypes.isEmpty()) {
            throw new IllegalArgumentException("답변이 없습니다.");
        }

        Map<TravelType, Long> counts = selectedTypes.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        long maxCount = counts.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElseThrow(() -> new IllegalStateException("집계 오류"));

        // 뒤에서부터 탐색하며 최다 득표 유형 중 가장 마지막에 선택된 것을 채택
        for (int i = selectedTypes.size() - 1; i >= 0; i--) {
            TravelType candidate = selectedTypes.get(i);
            if (counts.get(candidate) == maxCount) {
                return candidate;
            }
        }
        throw new IllegalStateException("결과를 계산할 수 없습니다.");
    }
}