package com.miportal.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class AuthIntegrationTest {

    @Container
    static final MSSQLServerContainer<?> sqlserver =
            new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-latest"))
                    .acceptLicense()
                    .withPassword("DckSql2025!")
                    .withEnv("MSSQL_PID", "Developer")
                    .withEnv("MSSQL_USER", "sa")
                    .withEnv("MSSQL_SA_PASSWORD", "DckSql2025!");

    // 🔑 Aquí inyectamos dinámicamente la config al contexto Spring
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlserver::getJdbcUrl);
        registry.add("spring.datasource.username", sqlserver::getUsername);
        registry.add("spring.datasource.password", sqlserver::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
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

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        String refreshToken = jsonNode.get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
