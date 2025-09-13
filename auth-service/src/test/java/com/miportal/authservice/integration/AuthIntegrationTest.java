package com.miportal.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class AuthIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("test_auth_db")
                    .withUsername("test_user")
                    .withPassword("test_pass");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registroYLoginDeberianFuncionar() throws Exception {
        // REGISTRO
        mockMvc.perform(post("/api/auth/registro/postulante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "username": "junitUser",
                              "email": "junit@test.com",
                              "password": "1234",
                              "rol": "POSTULANTE"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("junit@test.com"))
                .andExpect(jsonPath("$.rol").value("POSTULANTE"));

        // LOGIN
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "junit@test.com",
                              "password": "1234"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.email").value("junit@test.com"))
                .andExpect(jsonPath("$.rol").value("POSTULANTE"))
                .andReturn();

        // VERIFICACIÓN DE COOKIE
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie)
                .isNotNull()
                .contains("refreshToken=")
                .contains("HttpOnly")
                .contains("Path=/");
    }

    @Test
    void refreshTokenDebeRetornarNuevoAccessToken() throws Exception {
        // REGISTRO INICIAL
        mockMvc.perform(post("/api/auth/registro/postulante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "username": "pepito",
                              "email": "pepito@gmail.com",
                              "password": "1234",
                              "rol": "POSTULANTE"
                            }
                            """))
                .andExpect(status().isOk());

        // LOGIN PARA OBTENER REFRESH TOKEN
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "pepito@gmail.com",
                              "password": "1234"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        // Extraer cookie del refresh token
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isNotBlank();

        // TEST DEL REFRESH TOKEN
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.message").value("Autenticación exitosa"));
    }

    @Test
    void logoutDebeEliminarRefreshToken() throws Exception {
        // Registro y login inicial
        mockMvc.perform(post("/api/auth/registro/empresa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "username": "empresa1",
                              "email": "empresa@test.com",
                              "password": "1234",
                              "rol": "EMPRESA"
                            }
                            """))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "empresa@test.com",
                              "password": "1234"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        // TEST DEL LOGOUT
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andReturn();

        // Verificar que la cookie se eliminó
        Cookie deletedCookie = logoutResult.getResponse().getCookie("refreshToken");
        assertThat(deletedCookie).isNotNull();
        assertThat(deletedCookie.getMaxAge()).isEqualTo(0);

        // Verificar que el refresh token ya no funciona
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isNotFound());
    }
}