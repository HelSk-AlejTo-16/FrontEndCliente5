package com.mx.cbtis.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

@Controller
public class GestorController {
	
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private RestTemplate restTemplate;

	public RestTemplate getRestTemplate() {
		return (this.restTemplate != null) ? this.restTemplate : new RestTemplate();
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@GetMapping("/")
	public String getHome() {
		return "home";
	}
	
	@GetMapping("/tutorial")
	public String getTutorial() {
		return "tutorial";
	}
	
	@GetMapping("/manual")
	public String getManual() {
		return "manual";
	}
	
	@GetMapping("/login")
	public String getLogin() {
		return "login";
	}
	
	@GetMapping("/register")
	public String getRegister(Model modelo) {
		
		Usuario usuario = new Usuario();
		modelo.addAttribute("usuario", usuario);
		
		return "register";
	}
	
	@PostMapping("/saveUsuario")
	public ModelAndView setSaveUsuario(@ModelAttribute Usuario usuario) {
                   
    	RestTemplate template = getRestTemplate();
    	String urlservicebd = (usuario.getId_usuario() != null && usuario.getId_usuario() > 0)
    			? "http://localhost:8081/updateUsuario"
    			: "http://localhost:8081/addUsuario";
    	try {
    		ResponseEntity<Object> response = template.postForEntity(urlservicebd, usuario, Object.class);
    	} catch (HttpStatusCodeException ex) {
    		System.out.println("Aviso al guardar usuario: " + ex.getStatusCode());
    	}

    	return new ModelAndView("redirect:/login");

	}
	
	@GetMapping("/inicio")
	public String getInicio(Model modelo) {
		
		Proyectos proyectos = new Proyectos();
		modelo.addAttribute("proyectos", proyectos);
		
		return "inicio";
	}
	
	@PostMapping("/saveProyecto") //Actualiza y Crea proyectos.
	public ModelAndView setSaveProyecto(@ModelAttribute Proyectos proyecto) {

    	RestTemplate template = getRestTemplate();
    	String urlservicebd = (proyecto.getId_proyecto() != null && proyecto.getId_proyecto() > 0)
    			? "http://localhost:8081/updateProyecto"
    			: "http://localhost:8081/addProyecto";
    	ResponseEntity<Object> response = template.postForEntity(urlservicebd, proyecto, Object.class);

    	return new ModelAndView("redirect:/inicio");

	}
	
	@PostMapping("/consultarProyectos")
	public ResponseEntity<Object> consultarProyectos(Model modelo) {
		
		RestTemplate template = getRestTemplate();
		String urlservicebd = "http://localhost:8081/buscarProyectos";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Proyectos[]  response = template.postForObject(urlservicebd, headers, Proyectos[].class);
		List<Proyectos> proyectos = Arrays.asList(response);
		modelo.addAttribute("listaProyectos", proyectos);
		
		//ResponseEntity<List<Proyectos>> respuesta = (ResponseEntity<List<Proyectos>>) proyectos;
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(proyectos, HttpStatusCode.valueOf(200));
	
		return objeto;
	}
	
	@PostMapping("/eliminarProyecto")
	public ModelAndView eliminarProyecto(@RequestBody Proyectos proyecto) {
		System.out.println("El id del proyecto es: " + proyecto.getId_proyecto());
		RestTemplate  template = getRestTemplate();
		String urlservicebd = "http://localhost:8081/deleteProyecto";
		HttpEntity<Proyectos> request = new HttpEntity<Proyectos>(proyecto); 
		try {
			ResponseEntity<Object> response = template.exchange(urlservicebd, HttpMethod.DELETE, request, Object.class);
		} catch (HttpStatusCodeException ex) {
			System.out.println("Aviso al eliminar proyecto: " + ex.getStatusCode());
		}
	
		return new ModelAndView("redirect:/inicio");
	}
	
	@PostMapping ("/actualizarProyecto")
	public ResponseEntity<Object> updateProyecto(@RequestBody Proyectos proyecto) {
		RestTemplate  template = getRestTemplate();
		Map<String,Integer> params = new HashMap<String,Integer>();
		params.put("id_proyecto", proyecto.getId_proyecto());
		String urlservicebd = "http://localhost:8081/buscarProyecto?id_proyecto={id_proyecto}";
		try {
			Object proyectos = template.getForObject(urlservicebd, Object.class, params);
			return new ResponseEntity<Object>(proyectos, HttpStatusCode.valueOf(200));
		} catch (HttpStatusCodeException ex) {
			return ResponseEntity.status(ex.getStatusCode())
					.contentType(MediaType.APPLICATION_JSON)
					.body(ex.getResponseBodyAsString());
		}
	}
	
	
	@PostMapping("/verificarLogin")
	public ResponseEntity<Object> verificarUsuario(@RequestBody Usuario paramUser) {
		String urlservicebd = "http://localhost:8081/getUsuarioLogin";
		RestTemplate template = getRestTemplate();
		try {
			ResponseEntity<Object> response = template.postForEntity(urlservicebd, paramUser, Object.class);
			return response;
		} catch (HttpStatusCodeException ex) {
			String errorBody = ex.getResponseBodyAsString();
			if (errorBody == null || errorBody.trim().isEmpty()) {
				Map<String, Object> fallback = new HashMap<>();
				fallback.put("status", ex.getStatusCode().value());
				fallback.put("nombre", "000");
				fallback.put("mensaje", "Credenciales incorrectas");
				return ResponseEntity.status(ex.getStatusCode()).body(fallback);
			}
			return ResponseEntity.status(ex.getStatusCode())
					.contentType(MediaType.APPLICATION_JSON)
					.body(errorBody);
		}

	}
	
	/*@PostMapping("/obtenerTareas")
	public ResponseEntity<Object> obtenerTareas(@RequestBody Tareas idProyecto, Model modelo) {
		String urlservicebd = "http://localhost:8081/getTarea";
		RestTemplate  template = getRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Tareas[] response = template.postForObject(urlservicebd, idProyecto, Tareas[].class);
		List<Tareas> listaTareas = Arrays.asList(response);
		modelo.addAttribute("tareas", listaTareas);
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(listaTareas, HttpStatusCode.valueOf(200));
	
		return objeto;

	}*/
	
	@PostMapping("/consultarUsuarios")
	public ResponseEntity<Object> consultarUsuarios(Model modelo) {
		
		RestTemplate template = getRestTemplate();
		String urlservicebd = "http://localhost:8081/buscarUsuarios";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Usuario[]  response = template.postForObject(urlservicebd, headers, Usuario[].class);
		List<Usuario> usuarios = Arrays.asList(response);
		modelo.addAttribute("listaUsuarios", usuarios);
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(usuarios, HttpStatusCode.valueOf(200));
	
		return objeto;
	}
	
	@PostMapping("/obtenerTareas")
	public ResponseEntity<Object> obtenerTareas(@RequestBody Tareas idProyecto, Model modelo) {
	    String urlservicebd = "http://localhost:8081/getTarea";
	    RestTemplate template = getRestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    try {
	        Tareas[] response = template.postForObject(urlservicebd, idProyecto, Tareas[].class);
	        List<Tareas> listaTareas = (response != null) ? Arrays.asList(response) : java.util.Collections.emptyList();
	        modelo.addAttribute("tareas", listaTareas);
	        return ResponseEntity.ok(listaTareas);
	    } catch (HttpStatusCodeException ex) {
	        return ResponseEntity.status(ex.getStatusCode())
	                .contentType(MediaType.APPLICATION_JSON)
	                .body(ex.getResponseBodyAsString());
	    }
	}
	
	@PostMapping("/obtenerComentarios")
	public ResponseEntity<Object> obtenerComentarios(@RequestBody Comentarios idComentario, Model modelo) {
	    String urlservicebd = "http://localhost:8081/getComentario";
	    RestTemplate template = getRestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    try {
	        Comentarios[] response = template.postForObject(urlservicebd, idComentario, Comentarios[].class);
	        List<Comentarios> listaComentarios = (response != null) ? Arrays.asList(response) : java.util.Collections.emptyList();
	        modelo.addAttribute("comentarios", listaComentarios);
	        return ResponseEntity.ok(listaComentarios);
	    } catch (HttpStatusCodeException ex) {
	        return ResponseEntity.status(ex.getStatusCode())
	                .contentType(MediaType.APPLICATION_JSON)
	                .body(ex.getResponseBodyAsString());
	    }
	}
	
	//Liliana
	@GetMapping("/proyecto")
	public String getProyecto(Model modelo) {
		Tareas tarea = new Tareas();
		modelo.addAttribute("tarea", tarea);
		
		Comentarios comentario = new Comentarios();
		modelo.addAttribute("comentarios", comentario);
		
		RestTemplate template = getRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String urlservicebdUsuario = "http://localhost:8081/buscarUsuarios";
		Usuario[] responseUsuario = template.postForObject(urlservicebdUsuario, headers, Usuario[].class);  
		List<Usuario> usuarios = Arrays.asList(responseUsuario);
		modelo.addAttribute("listaUsuarios", usuarios);
		
		return "proyecto";
	}
	
	@GetMapping("/tarea")
	public String getTarea(Model modelo) {

		RestTemplate template = getRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Tareas tareas = new Tareas();
		modelo.addAttribute("tareas", tareas);
		modelo.addAttribute("tarea", tareas);
		modelo.addAttribute("comentarios", new Comentarios());
		
		return "proyecto";
	}
	
	@PostMapping("/saveTarea")
	public ModelAndView setSaveTareas(@ModelAttribute Tareas tareas) {
		
		RestTemplate template = getRestTemplate();
		String urlservicebd = (tareas.getId_tarea() != null && tareas.getId_tarea() > 0)
				? "http://localhost:8081/updateTareas"
				: "http://localhost:8081/addTareas";
		ResponseEntity<Object> response = template.postForEntity(urlservicebd, tareas, Object.class);
		ModelAndView mav = new ModelAndView("redirect:/proyecto");
		mav.addObject("id_proyecto", tareas.getId_proyecto());
		
		return mav;
	}
	@PostMapping("/saveComentario")
	public ResponseEntity<Object> setSaveComentario(@RequestBody Comentarios comentarios) {
		RestTemplate template = getRestTemplate();
		String urlservicebd = "http://localhost:8081/addComentario";
		try {
			ResponseEntity<Object> response = template.postForEntity(urlservicebd, comentarios, Object.class); 
			return response;
		} catch (HttpStatusCodeException ex) {
			return ResponseEntity.status(ex.getStatusCode())
					.contentType(MediaType.APPLICATION_JSON)
					.body(ex.getResponseBodyAsString());
		}
	}
	
	@PostMapping("/actualizarTarea")
	public ResponseEntity<Object> updateTarea(@RequestBody  Tareas tarea) {
		RestTemplate template = getRestTemplate();
		Map<String,Integer> params = new HashMap<String,Integer>();
		params.put("id_tarea", tarea.getId_tarea());
		String urlservicebd = "http://localhost:8081/buscarTarea?id_tarea={id_tarea}";
		try {
			Object tareas = template.getForObject(urlservicebd, Object.class, params);
			return new ResponseEntity<Object>(tareas, HttpStatusCode.valueOf(200));
		} catch (HttpStatusCodeException ex) {
			return ResponseEntity.status(ex.getStatusCode())
					.contentType(MediaType.APPLICATION_JSON)
					.body(ex.getResponseBodyAsString());
		}
	}
	
	@PostMapping("/eliminarTarea")
	public ModelAndView eliminarTarea(@RequestBody Tareas tareas) {
		System.out.println("El id d de la tarea es: " + tareas.getId_tarea());
		RestTemplate template = getRestTemplate();
		String urlservicebd = "http://localhost:8081/deleteTareas";
		HttpEntity<Tareas> request = new HttpEntity<Tareas>(tareas);
		try {
			ResponseEntity<Object> response = template.exchange(urlservicebd, HttpMethod.DELETE, request, Object.class);
		} catch (HttpStatusCodeException ex) {
			System.out.println("Aviso al eliminar tarea: " + ex.getStatusCode());
		}
		
		return new ModelAndView("redirect:/proyecto");
	}
	
	@PostMapping("/verificarTarea")
	public ResponseEntity<Object> verificarTarea(@RequestBody Tareas paramName){
		String urlservicebd = "http://localhost:8081/addTareas";
		RestTemplate template = getRestTemplate();
		try {
			ResponseEntity<Object> response = template.postForEntity(urlservicebd, paramName, Object.class);
			return response;
		} catch (HttpStatusCodeException ex) {
			return ResponseEntity.status(ex.getStatusCode())
					.contentType(MediaType.APPLICATION_JSON)
					.body(ex.getResponseBodyAsString());
		}
	}
	
	@PostMapping("/cambiosTareas") 
	public ResponseEntity<Object> cambiosTareas(@RequestBody Tareas paramDatos){
		String urlservicebd = (paramDatos.getId_tarea() != null && paramDatos.getId_tarea() > 0)
				? "http://localhost:8081/updateTareas"
				: "http://localhost:8081/addTareas";
		RestTemplate template = getRestTemplate();
		try {
			ResponseEntity<Object> response = template.postForEntity(urlservicebd, paramDatos, Object.class);
			return response;
		} catch (HttpStatusCodeException ex) {
			return ResponseEntity.status(ex.getStatusCode())
					.contentType(MediaType.APPLICATION_JSON)
					.body(ex.getResponseBodyAsString());
		}
	}
	
	@PostMapping("/consultarTareas")
	public ResponseEntity<Object> consultarTareas() {	
		RestTemplate template = getRestTemplate();
		String urlservicebd = "http://localhost:8081/buscarTareas";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Tareas[]  response = template.postForObject(urlservicebd, headers, Tareas[].class);
		List<Tareas> tareas = Arrays.asList(response);
		//ResponseEntity<List<Proyectos>> respuesta = (ResponseEntity<List<Proyectos>>) proyectos;
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(tareas, HttpStatusCode.valueOf(200));
		
		return objeto;

	}
	
	@PostMapping("/consultarComentarios")
	public ResponseEntity<Object> consultarComentarios() {	
		RestTemplate template = getRestTemplate();
		String urlservicebd = "http://localhost:8081/buscarComentarios";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Comentarios[]  response = template.postForObject(urlservicebd, headers, Comentarios[].class);
		List<Comentarios> comentarios = Arrays.asList(response);
		//ResponseEntity<List<Proyectos>> respuesta = (ResponseEntity<List<Proyectos>>) proyectos;
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(comentarios, HttpStatusCode.valueOf(200));
		
		return objeto;

	}
}
