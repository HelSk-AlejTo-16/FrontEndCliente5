package com.mx.cbtis.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Tareas;

/**
 * Pruebas de cobertura de ramas (branch coverage) para GestorController.
 * Cubren cada lado de los operadores ternarios y condiciones compuestas.
 */
@SpringBootTest(classes = com.mx.cbtis.Ejercicio5ClienteApplication.class)
@AutoConfigureMockMvc
class GestorRamasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    private void stubPostForEntity() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));
    }

    // ---------------------- Constructor y getter ----------------------

    @Test
    @DisplayName("Constructor con RestTemplate nulo crea uno por defecto")
    void testConstructorRestTemplateNulo() {
        GestorController controller = new GestorController((RestTemplate) null);
        assertNotNull(controller.getRestTemplate());
    }

    @Test
    @DisplayName("Getter devuelve un RestTemplate nuevo si el campo es nulo")
    void testGetterRestTemplateNulo() {
        GestorController controller = new GestorController(new RestTemplate());
        controller.setRestTemplate(null);
        assertNotNull(controller.getRestTemplate());
    }

    // ---------------------- saveUsuario ----------------------

    @Test
    @DisplayName("Guardar usuario con id_usuario=0 usa addUsuario")
    void testSaveUsuarioIdCero() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/saveUsuario").param("id_usuario", "0").param("nombre", "Ana"))
                .andExpect(status().is3xxRedirection());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/addUsuario"), any(), eq(Object.class));
    }

    @Test
    @DisplayName("Guardar usuario con id_usuario>0 usa updateUsuario")
    void testSaveUsuarioIdPositivo() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/saveUsuario").param("id_usuario", "5").param("nombre", "Ana"))
                .andExpect(status().is3xxRedirection());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/updateUsuario"), any(), eq(Object.class));
    }

    @Test
    @DisplayName("Guardar usuario sin id_usuario usa addUsuario")
    void testSaveUsuarioIdNulo() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/saveUsuario").param("nombre", "Ana"))
                .andExpect(status().is3xxRedirection());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/addUsuario"), any(), eq(Object.class));
    }

    // ---------------------- saveProyecto ----------------------

    @Test
    @DisplayName("Guardar proyecto con id_proyecto=0 usa addProyecto")
    void testSaveProyectoIdCero() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/saveProyecto").param("id_proyecto", "0").param("nombre", "P"))
                .andExpect(status().is3xxRedirection());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/addProyecto"), any(), eq(Object.class));
    }

    @Test
    @DisplayName("Guardar proyecto con id_proyecto>0 usa updateProyecto")
    void testSaveProyectoIdPositivo() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/saveProyecto").param("id_proyecto", "5").param("nombre", "P"))
                .andExpect(status().is3xxRedirection());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/updateProyecto"), any(), eq(Object.class));
    }

    // ---------------------- saveTarea ----------------------

    @Test
    @DisplayName("Guardar tarea con id_tarea=0 usa addTareas")
    void testSaveTareaIdCero() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/saveTarea").param("id_tarea", "0").param("id_proyecto", "1"))
                .andExpect(status().is3xxRedirection());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/addTareas"), any(), eq(Object.class));
    }

    @Test
    @DisplayName("Guardar tarea con id_tarea>0 usa updateTareas")
    void testSaveTareaIdPositivo() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/saveTarea").param("id_tarea", "5").param("id_proyecto", "1"))
                .andExpect(status().is3xxRedirection());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/updateTareas"), any(), eq(Object.class));
    }

    // ---------------------- cambiosTareas ----------------------

    @Test
    @DisplayName("Cambios en tarea con id_tarea=0 usa addTareas")
    void testCambiosTareasIdCero() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/cambiosTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":0,\"nombre\":\"T\"}"))
                .andExpect(status().isOk());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/addTareas"), any(), eq(Object.class));
    }

    @Test
    @DisplayName("Cambios en tarea con id_tarea>0 usa updateTareas")
    void testCambiosTareasIdPositivo() throws Exception {
        stubPostForEntity();
        mockMvc.perform(post("/cambiosTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":5,\"nombre\":\"T\"}"))
                .andExpect(status().isOk());
        verify(restTemplate).postForEntity(eq("http://localhost:8081/updateTareas"), any(), eq(Object.class));
    }

    // ---------------------- verificarLogin ----------------------

    @Test
    @DisplayName("Login con cuerpo de error nulo devuelve mensaje de respaldo")
    void testVerificarLoginCuerpoErrorNulo() throws Exception {
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.UNAUTHORIZED) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getResponseBodyAsString() {
                return null;
            }
        };
        when(restTemplate.postForEntity(eq("http://localhost:8081/getUsuarioLogin"), any(), eq(Object.class)))
                .thenThrow(ex);

        mockMvc.perform(post("/verificarLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo_electronico\":\"a@a.com\",\"contrasena\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales incorrectas"));
    }

    // ---------------------- Respuestas nulas del API ----------------------

    @Test
    @DisplayName("Obtener tareas cuando el API responde nulo devuelve lista vacía")
    void testObtenerTareasRespuestaNula() throws Exception {
        when(restTemplate.postForObject(eq("http://localhost:8081/getTarea"), any(), eq(Tareas[].class)))
                .thenReturn(null);

        mockMvc.perform(post("/obtenerTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_proyecto\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Obtener comentarios cuando el API responde nulo devuelve lista vacía")
    void testObtenerComentariosRespuestaNula() throws Exception {
        when(restTemplate.postForObject(eq("http://localhost:8081/getComentario"), any(), eq(Comentarios[].class)))
                .thenReturn(null);

        mockMvc.perform(post("/obtenerComentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

