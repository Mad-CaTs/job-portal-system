package com.miportal.authservice.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miportal.authservice.adapter.in.rest.AuthController;
import com.miportal.authservice.adapter.out.security.JwtFilter;
import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.port.in.AuthService;
import com.miportal.authservice.config.TestSecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
})
@Import(TestSecurityConfig.class)
class AuthLoginControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginDebeRetornarTokens() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "1234");
        LoginResponse response = LoginResponse.builder()
                .accessToken("access_token")
                .username("test")
                .email("test@test.com")
                .rol("POSTULANTE")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        when(authService.getLastRefreshToken()).thenReturn("refresh_token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.rol").value("POSTULANTE"))
                .andExpect(header().stringValues("Set-Cookie", Matchers.hasItem(Matchers.containsString("refreshToken"))));
    }
}
