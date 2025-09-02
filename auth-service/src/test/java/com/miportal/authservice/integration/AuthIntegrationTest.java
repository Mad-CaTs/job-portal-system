package com.miportal.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MSSQLServerContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthIntegrationTest {

    @Container
    static MSSQLServerContainer<?> sqlserver = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2019-CU18")
            .acceptLicense()
            .withPassword("dckSql");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registroYLoginDeberianFuncionar() throws Exception {
        // Registro de un postulante
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

        // Login con ese usuario
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email":"junit@test.com",
                              "password":"1234"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refreshTokenDebeRetornarNuevoAccessToken() throws Exception {
        // Login primero con usuario válido
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email":"pepito@gmail.com",
                          "password":"1234"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Parsear el refreshToken del JSON con Jackson
        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        String refreshToken = jsonNode.get("refreshToken").asText();

        // Llamar a /api/auth/refresh con la cookie refreshToken
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
