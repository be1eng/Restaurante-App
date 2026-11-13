package com.example.demo.seguridad;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de INTEGRACIÓN: arranca la app completa y usa MySQL real (base restaurante_test).
 * Perfiles: "local" aporta tu usuario/clave de MySQL (si existe), "test" cambia la base.
 */
@SpringBootTest
@ActiveProfiles({"local", "test"})
class AuthIntegrationTest {

    @Autowired WebApplicationContext context;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String body(String email, String password) {
        return "{\"nombre\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void flujoCompleto_registro_duplicado_login_yEndpointProtegido() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@mail.com";

        // 1) Registro -> 201 y token
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(body(email, "Clave1234")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // 2) Mismo email otra vez -> 409
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(body(email, "Clave1234")))
                .andExpect(status().isConflict());

        // 3) Login correcto -> 200 y token
        String respuesta = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Clave1234\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(respuesta, "$.token");

        // 4) Endpoint protegido con token -> 200 y datos del usuario
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles[0]").value("CLIENTE"));
    }

    @Test
    void registro_conDatosInvalidos_devuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(body("no-es-un-email", "corta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.email").exists())
                .andExpect(jsonPath("$.detalles.password").exists());
    }

    @Test
    void login_conClaveIncorrecta_devuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie@mail.com\",\"password\":\"Incorrecta1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegido_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }
}
