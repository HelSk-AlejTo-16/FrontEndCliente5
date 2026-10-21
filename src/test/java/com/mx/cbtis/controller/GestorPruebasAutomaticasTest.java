package com.mx.cbtis.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite Completa de Pruebas Automáticas de Integración del Frontend (Ejercicio5Cliente)
 * Correspondiente a la Actividad 2 de la Práctica 2:
 * 10 Casos Automáticos de Éxito (AUT-E01 a AUT-E10)
 * 10 Casos Automáticos de Fracaso (AUT-F01 a AUT-F10)
 */
@SpringBootTest(classes = com.mx.cbtis.Ejercicio5ClienteApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestorPruebasAutomaticasTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // 4.1 CASOS AUTOMÁTICOS DE ÉXITO (AUT-E01 a AUT-E10)
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("AUT-E01: Login exitoso con credenciales válidas (200 OK)")
    void test_AUT_E01_verificarUsuario_exito() throws Exception {
        String payloadLogin = "{\"correo_electronico\":\"liliana22512@cbtis75.edu.mx\",\"contrasena\":\"ada\"}";

        mockMvc.perform(post("/verificarLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_usuario").exists())
                .andExpect(jsonPath("$.correo_electronico").value("liliana22512@cbtis75.edu.mx"));
    }

    @Test
    @Order(2)
    @DisplayName("AUT-E02: Consulta de catálogo de proyectos (200 OK con lista no vacía)")
    void test_AUT_E02_consultarProyectos_exito() throws Exception {
        mockMvc.perform(post("/consultarProyectos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    @Order(3)
    @DisplayName("AUT-E03: Consulta de tareas de un proyecto existente (200 OK)")
    void test_AUT_E03_obtenerTareas_proyectoExistente() throws Exception {
        String payloadTarea = "{\"id_proyecto\":197}";

        mockMvc.perform(post("/obtenerTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTarea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(4)
    @DisplayName("AUT-E04: Envío de formulario de nuevo usuario válido (Redirección 302 a /login)")
    void test_AUT_E04_saveUsuario_exito() throws Exception {
        mockMvc.perform(post("/saveUsuario")
                .param("nombre", "Usuario Prueba")
                .param("correo_electronico", "test" + System.currentTimeMillis() + "@cbtis75.edu.mx")
                .param("contrasena", "PassAuto123")
                .param("rol", "Estudiante")
                .param("imagen_usuario", "#54EA01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Order(5)
    @DisplayName("AUT-E05: Registro de nueva tarea en tablero (200 OK con mensaje de éxito)")
    void test_AUT_E05_verificarTarea_registroExitoso() throws Exception {
        String payloadNuevaTarea = "{\"nombre\":\"Tarea Automatica " + System.currentTimeMillis() + "\",\"lista\":\"pendientes\",\"id_proyecto\":197}";

        mockMvc.perform(post("/verificarTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNuevaTarea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @Order(6)
    @DisplayName("AUT-E06: Consulta de tarea por ID para edición (200 OK con datos de tarea)")
    void test_AUT_E06_actualizarTarea_consultaPorId() throws Exception {
        String payloadConsulta = "{\"id_tarea\":199}";

        mockMvc.perform(post("/actualizarTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadConsulta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_tarea").value(199))
                .andExpect(jsonPath("$.nombre").exists());
    }

    @Test
    @Order(7)
    @DisplayName("AUT-E07: Consulta de proyecto por ID para edición (200 OK con datos de proyecto)")
    void test_AUT_E07_actualizarProyecto_consultaPorId() throws Exception {
        String payloadConsulta = "{\"id_proyecto\":197}";

        mockMvc.perform(post("/actualizarProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadConsulta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_proyecto").value(197))
                .andExpect(jsonPath("$.nombre").exists());
    }

    @Test
    @Order(8)
    @DisplayName("AUT-E08: Consulta de comentarios de una tarea existente (200 OK)")
    void test_AUT_E08_obtenerComentarios_tareaExistente() throws Exception {
        String payloadConsulta = "{\"id_tarea\":199}";

        mockMvc.perform(post("/obtenerComentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadConsulta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(9)
    @DisplayName("AUT-E09: Consulta del catálogo de usuarios para asignaciones (200 OK)")
    void test_AUT_E09_consultarUsuarios_catalogoExitoso() throws Exception {
        mockMvc.perform(post("/consultarUsuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    @Order(10)
    @DisplayName("AUT-E10: Carga y renderizado de la vista del tablero Kanban (200 OK con modelo)")
    void test_AUT_E10_getProyecto_renderizadoTablero() throws Exception {
        mockMvc.perform(get("/proyecto").param("id_proyecto", "197"))
                .andExpect(status().isOk())
                .andExpect(view().name("proyecto"))
                .andExpect(model().attributeExists("tarea", "comentarios", "listaUsuarios"));
    }

    // =========================================================================
    // 4.2 CASOS AUTOMÁTICOS DE FRACASO (AUT-F01 a AUT-F10)
    // =========================================================================

    @Test
    @Order(11)
    @DisplayName("AUT-F01: Login fallido con contraseña incorrecta (401 Unauthorized)")
    void test_AUT_F01_verificarUsuario_passwordIncorrecto() throws Exception {
        String payloadLoginErroneo = "{\"correo_electronico\":\"liliana22512@cbtis75.edu.mx\",\"contrasena\":\"clave_erronea_xyz\"}";

        mockMvc.perform(post("/verificarLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadLoginErroneo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.nombre").value("000"));
    }

    @Test
    @Order(12)
    @DisplayName("AUT-F02: Consulta de tareas con proyecto inexistente (404 Not Found controlado)")
    void test_AUT_F02_obtenerTareas_proyectoInexistente() throws Exception {
        String payloadProyectoInexistente = "{\"id_proyecto\":99999}";

        mockMvc.perform(post("/obtenerTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadProyectoInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Order(13)
    @DisplayName("AUT-F03: Intento de eliminación de proyecto inexistente (Redirección /inicio sin caída 500)")
    void test_AUT_F03_eliminarProyecto_inexistente() throws Exception {
        String payloadEliminar = "{\"id_proyecto\":99999}";

        mockMvc.perform(post("/eliminarProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadEliminar))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    @Order(14)
    @DisplayName("AUT-F04: Formulario de registro con correo inválido (Redirección /login sin caída 500)")
    void test_AUT_F04_registro_correoInvalido() throws Exception {
        mockMvc.perform(post("/saveUsuario")
                .param("nombre", "Usuario Correo Invalido")
                .param("correo_electronico", "correo_sin_formato")
                .param("contrasena", "123")
                .param("rol", "Estudiante"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Order(15)
    @DisplayName("AUT-F05: Creación de tarea con proyecto inexistente (400 Bad Request por FK inválida)")
    void test_AUT_F05_verificarTarea_proyectoInexistente() throws Exception {
        String payloadTareaFkInvalida = "{\"nombre\":\"Tarea FK Inexistente\",\"lista\":\"pendientes\",\"id_proyecto\":99999}";

        mockMvc.perform(post("/verificarTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTareaFkInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Proyecto inexistente"));
    }

    @Test
    @Order(16)
    @DisplayName("AUT-F06: Consulta de tarea con ID inexistente (404 Not Found controlado)")
    void test_AUT_F06_actualizarTarea_idInexistente() throws Exception {
        String payloadTareaInexistente = "{\"id_tarea\":99999}";

        mockMvc.perform(post("/actualizarTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadTareaInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Order(17)
    @DisplayName("AUT-F07: Consulta de proyecto con ID inexistente (404 Not Found controlado)")
    void test_AUT_F07_actualizarProyecto_idInexistente() throws Exception {
        String payloadProyectoInexistente = "{\"id_proyecto\":99999}";

        mockMvc.perform(post("/actualizarProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadProyectoInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Order(18)
    @DisplayName("AUT-F08: Consulta de comentarios con ID de tarea inexistente (404 Not Found controlado)")
    void test_AUT_F08_obtenerComentarios_tareaInexistente() throws Exception {
        String payloadComentarioInexistente = "{\"id_tarea\":99999}";

        mockMvc.perform(post("/obtenerComentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadComexistente(payloadComentarioInexistente)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String payloadComexistente(String s) {
        return s;
    }

    @Test
    @Order(19)
    @DisplayName("AUT-F09: Eliminación de tarea con ID inexistente (Redirección /proyecto sin caída 500)")
    void test_AUT_F09_eliminarTarea_idInexistente() throws Exception {
        String payloadEliminar = "{\"id_tarea\":99999}";

        mockMvc.perform(post("/eliminarTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadEliminar))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/proyecto"));
    }

    @Test
    @Order(20)
    @DisplayName("AUT-F10: Guardado de comentario asociado a tarea inexistente (400 Bad Request por FK inválida)")
    void test_AUT_F10_saveComentario_tareaInexistente() throws Exception {
        String payloadComentarioFkInvalida = "{\"texto_comentario\":\"Comentario huérfano\",\"id_tarea\":99999,\"id_usuario\":\"Test\"}";

        mockMvc.perform(post("/saveComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadComentarioFkInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Tarea inexistente"));
    }
}
