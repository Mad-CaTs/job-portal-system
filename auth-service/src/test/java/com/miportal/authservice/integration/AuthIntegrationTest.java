package com.miportal.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miportal.authservice.config.TestSecurityConfig;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Import(TestSecurityConfig.class)
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
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        log.info("=== INICIANDO TEST - Preparando datos ===");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Crear roles si no existen
            stmt.executeUpdate("""
                INSERT INTO tbl_rol (vch_nombre, vch_usuario_creacion, dt_fec_creacion)
                VALUES 
                  ('POSTULANTE','system',CURRENT_TIMESTAMP),
                  ('EMPRESA','system',CURRENT_TIMESTAMP),
                  ('ADMIN','system',CURRENT_TIMESTAMP)
                ON CONFLICT (vch_nombre) DO NOTHING
            """);

            // Crear usuarios con contraseña hasheada "1234"
            String hashedPassword = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi."; // 1234

            String insertUser = """
                INSERT INTO tbl_usuario (vch_username, vch_email, vch_password, bit_estado, int_id_fk_rol, vch_usuario_creacion, dt_fec_creacion)
                SELECT ?, ?, ?, true, r.int_id, 'system', CURRENT_TIMESTAMP
                FROM tbl_rol r
                WHERE r.vch_nombre = ?
                ON CONFLICT (vch_email) DO NOTHING
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ps.setString(1, "junitUser");
                ps.setString(2, "junit@test.com");
                ps.setString(3, hashedPassword);
                ps.setString(4, "POSTULANTE");
                ps.executeUpdate();

                ps.setString(1, "pepito");
                ps.setString(2, "pepito@gmail.com");
                ps.setString(3, hashedPassword);
                ps.setString(4, "POSTULANTE");
                ps.executeUpdate();

                ps.setString(1, "empresa1");
                ps.setString(2, "empresa@test.com");
                ps.setString(3, hashedPassword);
                ps.setString(4, "EMPRESA");
                ps.executeUpdate();
            }

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM tbl_usuario");
            rs.next();
            log.info("Usuarios en BD antes del test: {}", rs.getInt("c"));
        }
    }

    @Test
    void registroYLoginDeberianFuncionar() throws Exception {
        log.info("=== TEST: Login (usuario ya existe en BD) ===");

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

        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie)
                .isNotNull()
                .contains("refreshToken=")
                .contains("HttpOnly");
    }

    @Test
    void refreshTokenDebeRetornarNuevoAccessToken() throws Exception {
        log.info("=== TEST: Refresh Token ===");

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

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    void logoutDebeEliminarRefreshToken() throws Exception {
        log.info("=== TEST: Logout ===");

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

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        Cookie deletedCookie = logoutResult.getResponse().getCookie("refreshToken");
        assertThat(deletedCookie).isNotNull();
        assertThat(deletedCookie.getMaxAge()).isEqualTo(0);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
