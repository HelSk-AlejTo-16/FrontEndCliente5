package com.mx.cbtis.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

@SpringBootTest(classes = com.mx.cbtis.Ejercicio5ClienteApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestorPruebasAutomaticasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    // =========================================================================
    // 4.1 CASOS AUTOMÁTICOS DE ÉXITO (AUT-E01 a AUT-E10)
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("AUT-E01: Login exitoso con credenciales válidas (200 OK)")
    void test_AUT_E01_verificarUsuario_exito() throws Exception {
        Map<String, Object> userResp = new HashMap<>();
        userResp.put("id_usuario", 1);
        userResp.put("nombre", "Liliana");
        userResp.put("correo_electronico", "liliana22512@cbtis75.edu.mx");

        when(restTemplate.postForEntity(eq("http://localhost:8081/getUsuarioLogin"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(userResp));

        String payloadLogin = "{\"correo_electronico\":\"liliana22512@cbtis75.edu.mx\",\"contrasena\":\"ada\"}";

        mockMvc.perform(post("/verificarLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_usuario").value(1))
                .andExpect(jsonPath("$.correo_electronico").value("liliana22512@cbtis75.edu.mx"));
    }

    @Test
    @Order(2)
    @DisplayName("AUT-E02: Consulta de catálogo de proyectos (200 OK con lista no vacía)")
    void test_AUT_E02_consultarProyectos_exito() throws Exception {
        Proyectos p = new Proyectos();
        p.setId_proyecto(1);
        p.setNombre("Proyecto Alpha");
        Proyectos[] proys = new Proyectos[]{ p };

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarProyectos"), any(), eq(Proyectos[].class)))
                .thenReturn(proys);

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
        Tareas t = new Tareas();
        t.setId_tarea(1);
        t.setNombre("Tarea Inicial");
        Tareas[] tareas = new Tareas[]{ t };

        when(restTemplate.postForObject(eq("http://localhost:8081/getTarea"), any(), eq(Tareas[].class)))
                .thenReturn(tareas);

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
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        mockMvc.perform(post("/saveUsuario")
                .param("nombre", "Usuario Prueba")
                .param("correo_electronico", "test@cbtis75.edu.mx")
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
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", 200);
        resp.put("resultado", 1);

        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(resp));

        String payloadNuevaTarea = "{\"nombre\":\"Tarea Automatica\",\"lista\":\"pendientes\",\"id_proyecto\":197}";

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
        Map<String, Object> tareaMap = new HashMap<>();
        tareaMap.put("id_tarea", 199);
        tareaMap.put("nombre", "Tarea para Editar");

        when(restTemplate.getForObject(anyString(), eq(Object.class), anyMap()))
                .thenReturn(tareaMap);

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
        Map<String, Object> proyMap = new HashMap<>();
        proyMap.put("id_proyecto", 197);
        proyMap.put("nombre", "Proyecto 197");

        when(restTemplate.getForObject(anyString(), eq(Object.class), anyMap()))
                .thenReturn(proyMap);

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
        Comentarios c = new Comentarios();
        c.setId_comentario(1);
        Comentarios[] coms = new Comentarios[]{ c };

        when(restTemplate.postForObject(eq("http://localhost:8081/getComentario"), any(), eq(Comentarios[].class)))
                .thenReturn(coms);

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
        Usuario u = new Usuario();
        u.setId_usuario(1);
        u.setNombre("Ana");
        Usuario[] users = new Usuario[]{ u };

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarUsuarios"), any(), eq(Usuario[].class)))
                .thenReturn(users);

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
        Usuario u = new Usuario();
        u.setId_usuario(1);
        Usuario[] users = new Usuario[]{ u };

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarUsuarios"), any(), eq(Usuario[].class)))
                .thenReturn(users);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY,
                "{\"status\":401,\"error\":\"Credenciales invalidas\",\"nombre\":\"000\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForEntity(eq("http://localhost:8081/getUsuarioLogin"), any(), eq(Object.class)))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                "{\"status\":404,\"error\":\"Proyecto inexistente\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForObject(eq("http://localhost:8081/getTarea"), any(), eq(Tareas[].class)))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                "{\"status\":404}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.exchange(eq("http://localhost:8081/deleteProyecto"), any(), any(), eq(Object.class)))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "{\"status\":400}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "{\"status\":400,\"error\":\"Proyecto inexistente\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), any(), eq(Object.class)))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                "{\"status\":404}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.getForObject(anyString(), eq(Object.class), anyMap()))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                "{\"status\":404}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.getForObject(anyString(), eq(Object.class), anyMap()))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                "{\"status\":404}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForObject(eq("http://localhost:8081/getComentario"), any(), eq(Comentarios[].class)))
                .thenThrow(ex);

        String payloadComentarioInexistente = "{\"id_tarea\":99999}";

        mockMvc.perform(post("/obtenerComentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadComentarioInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Order(19)
    @DisplayName("AUT-F09: Eliminación de tarea con ID inexistente (Redirección /proyecto sin caída 500)")
    void test_AUT_F09_eliminarTarea_idInexistente() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY,
                "{\"status\":404}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.exchange(eq("http://localhost:8081/deleteTareas"), any(), any(), eq(Object.class)))
                .thenThrow(ex);

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
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "{\"status\":400,\"error\":\"Tarea inexistente\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForEntity(eq("http://localhost:8081/addComentario"), any(), eq(Object.class)))
                .thenThrow(ex);

        String payloadComentarioFkInvalida = "{\"texto_comentario\":\"Comentario huérfano\",\"id_tarea\":99999,\"id_usuario\":\"Test\"}";

        mockMvc.perform(post("/saveComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadComentarioFkInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Tarea inexistente"));
    }

    // =========================================================================
    // 4.3 COBERTURA DE RUTAS ESTÁTICAS DE VISTAS (GET)
    // =========================================================================

    @Test
    @Order(21)
    @DisplayName("AUT-V01: Rutas de navegación estándar (home, tutorial, manual, login, register, tarea)")
    void testRutasVistasEstaticas() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
        mockMvc.perform(get("/tutorial")).andExpect(status().isOk()).andExpect(view().name("tutorial"));
        mockMvc.perform(get("/manual")).andExpect(status().isOk()).andExpect(view().name("manual"));
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
        mockMvc.perform(get("/register")).andExpect(status().isOk()).andExpect(view().name("register"));
        mockMvc.perform(get("/tarea")).andExpect(status().isOk()).andExpect(view().name("proyecto"));
    }

    @Test
    @Order(22)
    @DisplayName("AUT-E11: Actualización de usuario existente (id_usuario > 0)")
    void test_AUT_E11_updateUsuario() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:8081/updateUsuario"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/saveUsuario")
                .param("id_usuario", "5")
                .param("nombre", "Usuario Actualizado")
                .param("contraseña", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Order(23)
    @DisplayName("AUT-E12: Actualización de proyecto existente (id_proyecto > 0)")
    void test_AUT_E12_updateProyecto() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:8081/updateProyecto"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/saveProyecto")
                .param("id_proyecto", "10")
                .param("nombre", "Proyecto Modificado")
                .param("descripcion", "Desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    @Order(24)
    @DisplayName("AUT-E13: Actualización de tarea existente con redirección con parámetro")
    void test_AUT_E13_updateTarea() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:8081/updateTareas"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/saveTarea")
                .param("id_tarea", "15")
                .param("id_proyecto", "3")
                .param("nombre", "Tarea Modificada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/proyecto?id_proyecto=3"));
    }

    @Test
    @Order(25)
    @DisplayName("AUT-E14: Cambios en tarea con id existente (updateTareas)")
    void test_AUT_E14_cambiosTareas_update() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:8081/updateTareas"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":200}"));

        String payload = "{\"id_tarea\":15,\"nombre\":\"Actualizar Tarea\"}";

        mockMvc.perform(post("/cambiosTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @Order(26)
    @DisplayName("AUT-E15: Cambios en tarea con error HTTP (HttpStatusCodeException)")
    void test_AUT_E15_cambiosTareas_error() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "{\"error\":\"Error de validación\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForEntity(eq("http://localhost:8081/updateTareas"), any(), eq(Object.class)))
                .thenThrow(ex);

        String payload = "{\"id_tarea\":15,\"nombre\":\"Invalida\"}";

        mockMvc.perform(post("/cambiosTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"));
    }

    @Test
    @Order(27)
    @DisplayName("AUT-E16: Verificación de tarea con error HTTP")
    void test_AUT_E16_verificarTarea_error() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY,
                "{\"error\":\"Tarea ya existe\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), any(), eq(Object.class)))
                .thenThrow(ex);

        mockMvc.perform(post("/verificarTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Duplicada\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Tarea ya existe"));
    }

    @Test
    @Order(28)
    @DisplayName("AUT-E17: Consulta general de tareas del sistema (200 OK)")
    void test_AUT_E17_consultarTareas_general() throws Exception {
        Tareas t = new Tareas();
        t.setId_tarea(1);
        t.setNombre("Tarea General");
        Tareas[] lista = new Tareas[]{ t };

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarTareas"), any(), eq(Tareas[].class)))
                .thenReturn(lista);

        mockMvc.perform(post("/consultarTareas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    @Order(29)
    @DisplayName("AUT-E18: Consulta general de comentarios del sistema (200 OK)")
    void test_AUT_E18_consultarComentarios_general() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_comentario(1);
        c.setTexto_comentario("Comentario general");
        Comentarios[] lista = new Comentarios[]{ c };

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarComentarios"), any(), eq(Comentarios[].class)))
                .thenReturn(lista);

        mockMvc.perform(post("/consultarComentarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    @Order(30)
    @DisplayName("AUT-E19: Fallback de login cuando cuerpo de error es vacío")
    void test_AUT_E19_verificarLogin_cuerpoVacio() throws Exception {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8);

        when(restTemplate.postForEntity(eq("http://localhost:8081/getUsuarioLogin"), any(), eq(Object.class)))
                .thenThrow(ex);

        mockMvc.perform(post("/verificarLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"test@test.com\",\"contrasena\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Credenciales incorrectas"));
    }

    @Test
    @Order(31)
    @DisplayName("AUT-E20: Constructores, Getters y Setters directos del controlador")
    void test_AUT_E20_getterSetterRestTemplate() {
        GestorController controller = new GestorController();
        org.junit.jupiter.api.Assertions.assertNotNull(controller.getRestTemplate());
        RestTemplate custom = new RestTemplate();
        GestorController controllerParam = new GestorController(custom);
        org.junit.jupiter.api.Assertions.assertSame(custom, controllerParam.getRestTemplate());
        controller.setRestTemplate(custom);
        org.junit.jupiter.api.Assertions.assertSame(custom, controller.getRestTemplate());
    }

    @Test
    @Order(32)
    @DisplayName("AUT-E21: Vista /inicio retorna modelo y template 'inicio'")
    void test_AUT_E21_getInicio() throws Exception {
        mockMvc.perform(get("/inicio"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeExists("proyectos"));
    }

    @Test
    @Order(33)
    @DisplayName("AUT-E22: Guardar proyecto nuevo (id_proyecto nulo -> addProyecto)")
    void test_AUT_E22_saveProyecto_nuevo() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:8081/addProyecto"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/saveProyecto")
                .param("nombre", "Proyecto Nuevo")
                .param("descripcion", "Desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    @Order(34)
    @DisplayName("AUT-E23: Eliminar proyecto exitoso sin excepción")
    void test_AUT_E23_eliminarProyecto_exito() throws Exception {
        when(restTemplate.exchange(eq("http://localhost:8081/deleteProyecto"), eq(org.springframework.http.HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/eliminarProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_proyecto\":10}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicio"));
    }

    @Test
    @Order(35)
    @DisplayName("AUT-E24: Guardar tarea nueva (id_tarea nulo -> addTareas)")
    void test_AUT_E24_saveTarea_nueva() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/saveTarea")
                .param("nombre", "Nueva Tarea"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/proyecto"));
    }

    @Test
    @Order(36)
    @DisplayName("AUT-E25: Guardar comentario exitoso (200 OK)")
    void test_AUT_E25_saveComentario_exito() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("resultado", 1);
        when(restTemplate.postForEntity(eq("http://localhost:8081/addComentario"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(post("/saveComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":1,\"texto_comentario\":\"Buen progreso\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @Order(37)
    @DisplayName("AUT-E26: Eliminar tarea exitoso sin excepción")
    void test_AUT_E26_eliminarTarea_exito() throws Exception {
        when(restTemplate.exchange(eq("http://localhost:8081/deleteTareas"), eq(org.springframework.http.HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        mockMvc.perform(post("/eliminarTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":5}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/proyecto"));
    }

    @Test
    @Order(38)
    @DisplayName("AUT-E27: Cambios en tarea nueva (id_tarea nulo -> addTareas)")
    void test_AUT_E27_cambiosTareas_nueva() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("resultado", 1);
        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(resp));

        mockMvc.perform(post("/cambiosTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Tarea sin ID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }
}
