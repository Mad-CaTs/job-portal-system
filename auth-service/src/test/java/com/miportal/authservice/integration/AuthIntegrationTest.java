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
import java.sql.PreparedStatement;
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
                log.error("NO HAY ROLES EN LA BD - Esto causará fallos en el registro");
            } else {
                rs = stmt.executeQuery("SELECT vch_nombre FROM tbl_rol");
                log.info("Roles disponibles:");
                while (rs.next()) {
                    log.info("  - {}", rs.getString("vch_nombre"));
                }
            }

            // CREAR USUARIOS DE PRUEBA PARA TESTING (ya que no podemos registrarlos por API)
            createTestUsers(conn);
        }
    }

    private void createTestUsers(Connection conn) throws Exception {
        // Password hasheada para "1234" usando BCrypt
        String hashedPassword = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi."; // "1234"

        String insertUser = """
            INSERT INTO tbl_usuario (vch_username, vch_email, vch_password, bit_estado, int_id_fk_rol, vch_usuario_creacion, dt_fec_creacion)
            SELECT ?, ?, ?, true, r.int_id, 'system', CURRENT_TIMESTAMP
            FROM tbl_rol r 
            WHERE r.vch_nombre = ?
            ON CONFLICT (vch_email) DO NOTHING
        """;

        // Usuario para primer test
        try (PreparedStatement stmt = conn.prepareStatement(insertUser)) {
            stmt.setString(1, "junitUser");
            stmt.setString(2, "junit@test.com");
            stmt.setString(3, hashedPassword);
            stmt.setString(4, "POSTULANTE");
            stmt.executeUpdate();
        }

        // Usuario para segundo test
        try (PreparedStatement stmt = conn.prepareStatement(insertUser)) {
            stmt.setString(1, "pepito");
            stmt.setString(2, "pepito@gmail.com");
            stmt.setString(3, hashedPassword);
            stmt.setString(4, "POSTULANTE");
            stmt.executeUpdate();
        }

        // Usuario para tercer test
        try (PreparedStatement stmt = conn.prepareStatement(insertUser)) {
            stmt.setString(1, "empresa1");
            stmt.setString(2, "empresa@test.com");
            stmt.setString(3, hashedPassword);
            stmt.setString(4, "EMPRESA");
            stmt.executeUpdate();
        }

        log.info("Usuarios de prueba creados");
    }

    @Test
    void registroYLoginDeberianFuncionar() throws Exception {
        log.info("=== TEST: Login (usuario ya existe en BD) ===");
        // LOGIN (el usuario ya fue creado en setUp())
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
        // LOGIN PARA OBTENER REFRESH TOKEN (el usuario ya existe en BD)
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
        // LOGIN (el usuario ya existe en BD)
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