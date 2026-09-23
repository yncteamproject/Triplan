package kr.ync.tripplan.controller;

import kr.ync.tripplan.dto.request.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseController{

    @Test
    @DisplayName("1. 정상 입력 시 회원가입 성공")
    void signup_success() throws Exception{
        SignupRequest request = new SignupRequest("test@example.com", "pass1234", "테스트 유저");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. 요청 본문 없이 보내면 400")
    void signup_emptyBody() throws Exception{
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3. 필수값(password) 누락 시 400")
    void signup_missingRequiredField() throws Exception {
        String invalidJson = """
                {
                  "email": "test3@example.com",
                  "password": "",
                  "nickname": "테스트유저3"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("4. 비정상적인 타입(배열)이 들어오면 400")
    void signup_invalidType() throws Exception {
        String invalidJson = """
                {
                  "email": "test4@example.com",
                  "password": ["1234", "5678"],
                  "nickname": "테스트유저4"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}