package com.miportal.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class AuthIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AuthIntegrationTest.class);

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("test_auth_db")
                    .withUsername("test_user")
                    .withPassword("test_pass")
                    .withReuse(true);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.data-locations", () -> "classpath:V1__init.sql");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        log.info("=== INICIANDO TEST - Verificando estado de la BD ===");

        // Verificar que los roles existen
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM tbl_rol");
            rs.next();
            int roleCount = rs.getInt("count");
            log.info("Número de roles en BD: {}", roleCount);

            if (roleCount == 0) {
                log.error("❌ NO HAY ROLES EN LA BD - Esto causará fallos en el registro");
            } else {
                rs = stmt.executeQuery("SELECT vch_nombre FROM tbl_rol");
                log.info("Roles disponibles:");
                while (rs.next()) {
                    log.info("  - {}", rs.getString("vch_nombre"));
                }
            }
        }
    }

    @Test
    void registroYLoginDeberianFuncionar() throws Exception {
        log.info("=== TEST: Registro y Login ===");

        // REGISTRO
        MvcResult registroResult = mockMvc.perform(post("/api/auth/registro/postulante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "username": "junitUser",
                              "email": "junit@test.com",
                              "password": "1234",
                              "rol": "POSTULANTE"
                            }
                            """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("junit@test.com"))
                .andExpect(jsonPath("$.rol").value("POSTULANTE"))
                .andReturn();

        log.info("Registro exitoso: {}", registroResult.getResponse().getContentAsString());

        // LOGIN
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "junit@test.com",
                              "password": "1234"
                            }
                            """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.email").value("junit@test.com"))
                .andExpect(jsonPath("$.rol").value("POSTULANTE"))
                .andReturn();

        // Verificar cookie
        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie)
                .isNotNull()
                .contains("refreshToken=")
                .contains("HttpOnly")
                .contains("Path=/");

        log.info("Login exitoso con cookie: {}", setCookie);
    }

    @Test
    void refreshTokenDebeRetornarNuevoAccessToken() throws Exception {
        log.info("=== TEST: Refresh Token ===");

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
                .andDo(print())
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
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isNotBlank();

        log.info("RefreshToken obtenido: {}", refreshCookie.getValue());

        // TEST DEL REFRESH TOKEN
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.message").value("Autenticación exitosa"));
    }

    @Test
    void logoutDebeEliminarRefreshToken() throws Exception {
        log.info("=== TEST: Logout ===");

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
                .andDo(print())
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "empresa@test.com",
                              "password": "1234"
                            }
                            """))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        // TEST DEL LOGOUT
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        // Verificar que la cookie se eliminó
        Cookie deletedCookie = logoutResult.getResponse().getCookie("refreshToken");
        assertThat(deletedCookie).isNotNull();
        assertThat(deletedCookie.getMaxAge()).isEqualTo(0);

        // Verificar que el refresh token ya no funciona
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}