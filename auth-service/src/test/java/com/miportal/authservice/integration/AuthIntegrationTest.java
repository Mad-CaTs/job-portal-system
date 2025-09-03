package com.miportal.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthIntegrationTest {

    @Container
    static final MSSQLServerContainer<?> sqlserver =
            new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-latest"))
                    .acceptLicense()
                    .withPassword("DckSql2025!");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlserver::getJdbcUrl);
        registry.add("spring.datasource.username", sqlserver::getUsername);
        registry.add("spring.datasource.password", sqlserver::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registroYLoginDeberianFuncionar() throws Exception {
        mockMvc.perform(post("/api/auth/registro/postulante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "username":"junitUser",
                              "email":"junit@test.com",
                              "password":"1234",
                              "rol":"POSTULANTE"
                            }
                            """))
                .andExpect(status().isOk());

        // Capturamos el resultado del login
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email":"junit@test.com",
                              "password":"1234"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        // Verificamos que Set-Cookie contiene el refreshToken
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("refreshToken=");
    }

    @Test
    void refreshTokenDebeRetornarNuevoAccessToken() throws Exception {
        mockMvc.perform(post("/api/auth/registro/postulante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "username":"pepito",
                              "email":"pepito@gmail.com",
                              "password":"1234",
                              "rol":"POSTULANTE"
                            }
                            """))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email":"pepito@gmail.com",
                              "password":"1234"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }
}
