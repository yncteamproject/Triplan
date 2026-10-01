package kr.ync.triplan.traveltest.controller;

import kr.ync.triplan.support.BaseController;
import kr.ync.triplan.member.dto.request.SignupRequest;
import kr.ync.triplan.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TravelPreferenceControllerTest extends BaseController {

    @Autowired
    private MemberService memberService;

    private static final String TEST_EMAIL = "traveler@example.com";

    @BeforeEach
    void createTestMember() {
        memberService.signup(new SignupRequest(TEST_EMAIL, "password123", "여행자"));
    }

    // 같은 유형을 3번 골라서 테스트 제출 → 그 유형이 결과로 저장됨
    private void submitTest(String type) throws Exception {
        String body = """
                { "selectedTypes": ["%s", "%s", "%s"] }
                """.formatted(type, type, type);

        mockMvc.perform(post("/api/travel-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("1. 테스트 결과가 있으면 내 결과를 조회할 수 있다")
    @WithMockUser(username = TEST_EMAIL)
    void getMyResult_success() throws Exception {
        submitTest("FREE_EXPLORER");

        mockMvc.perform(get("/api/travel-test/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelType").value("FREE_EXPLORER"))
                .andExpect(jsonPath("$.displayName").value("자유로운 탐험가"))
                .andExpect(jsonPath("$.testedAt").exists());
    }

    @Test
    @DisplayName("2. 테스트를 여러 번 했으면 가장 최근 결과가 나온다")
    @WithMockUser(username = TEST_EMAIL)
    void getMyResult_latest() throws Exception {
        submitTest("FREE_EXPLORER");
        submitTest("FOOD_EXPLORER");

        mockMvc.perform(get("/api/travel-test/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelType").value("FOOD_EXPLORER"));
    }

    @Test
    @DisplayName("3. 테스트를 한 적이 없으면 404")
    @WithMockUser(username = TEST_EMAIL)
    void getMyResult_notFound() throws Exception {
        mockMvc.perform(get("/api/travel-test/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("여행 성향 테스트 결과가 없습니다."));
    }

    @Test
    @DisplayName("4. 인증 없이 조회하면 401")
    void getMyResult_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/travel-test/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("5. 답변 배열에 null이 섞여 있으면 400")
    @WithMockUser(username = TEST_EMAIL)
    void submit_nullElement() throws Exception {
        mockMvc.perform(post("/api/travel-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "selectedTypes": ["FREE_EXPLORER", null] }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("선택하지 않은 답변이 있습니다"));
    }
}