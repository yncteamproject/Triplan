package kr.ync.triplan.controller;

import kr.ync.triplan.dto.request.SignupRequest;
import kr.ync.triplan.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTest extends BaseController {

    @Autowired
    private MemberService memberService;

    private static final String TEST_EMAIL = "member@example.com";

    @BeforeEach
    void createTestMember() {
        memberService.signup(new SignupRequest(TEST_EMAIL, "password123", "원래닉네임"));
    }

    @Test
    @DisplayName("1. 로그인한 사용자는 내 정보를 조회할 수 있다")
    @WithMockUser(username = TEST_EMAIL)
    void getMyInfo_success() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.nickname").value("원래닉네임"));
    }

    @Test
    @DisplayName("2. 인증 없이 조회하면 403")
    void getMyInfo_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. 닉네임 수정 성공")
    @WithMockUser(username = TEST_EMAIL)
    void updateMyInfo_success() throws Exception {
        String body = """
                { "nickname": "새닉네임" }
                """;

        mockMvc.perform(put("/api/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("4. 비밀번호 8자 미만이면 400")
    @WithMockUser(username = TEST_EMAIL)
    void updateMyInfo_invalidPassword() throws Exception {
        String body = """
                { "password": "123" }
                """;

        mockMvc.perform(put("/api/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}