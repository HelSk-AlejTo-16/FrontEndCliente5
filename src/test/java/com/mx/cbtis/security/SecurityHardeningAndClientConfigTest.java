package com.mx.cbtis.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class SecurityHardeningAndClientConfigTest {

    @Test
    @DisplayName("SecurityClientConfig inyecta X-Internal-Api-Key solo cuando esta configurada")
    void restTemplateInterceptorAddsInternalApiKeyWhenConfigured() {
        SecurityClientConfig config = new SecurityClientConfig();

        RestTemplate plainTemplate = config.restTemplate("   ");
        assertTrue(plainTemplate.getInterceptors().isEmpty());

        RestTemplate securedTemplate = config.restTemplate("shared-secret-key");
        assertEquals(1, securedTemplate.getInterceptors().size());

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(securedTemplate);
        mockServer.expect(requestTo("http://localhost:8081/buscarProyectos"))
                .andExpect(header("X-Internal-Api-Key", "shared-secret-key"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        String body = securedTemplate.getForObject("http://localhost:8081/buscarProyectos", String.class);
        assertEquals("[]", body);
        mockServer.verify();
    }

    @Test
    @DisplayName("Frontend SecurityHardeningFilter agrega cabeceras CSP/HSTS y permite /actuator/health e /info")
    void frontendFilterAllowsHealthProbesAndSetsSecurityHeaders() throws Exception {
        SecurityHardeningFilter filter = new SecurityHardeningFilter(
                "prom-secret", true, 2, 5, 60_000L);

        MockHttpServletRequest reqHealth = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse resHealth = new MockHttpServletResponse();
        filter.doFilter(reqHealth, resHealth, new MockFilterChain());

        assertEquals(200, resHealth.getStatus());
        assertEquals("nosniff", resHealth.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", resHealth.getHeader("X-Frame-Options"));
        assertTrue(resHealth.getHeader("Content-Security-Policy").contains("default-src 'self'"));

        MockHttpServletRequest reqInfo = new MockHttpServletRequest("GET", "/actuator/info");
        MockHttpServletResponse resInfo = new MockHttpServletResponse();
        filter.doFilter(reqInfo, resInfo, new MockFilterChain());
        assertEquals(200, resInfo.getStatus());
    }

    @Test
    @DisplayName("Frontend SecurityHardeningFilter protege /actuator/prometheus con Bearer token")
    void frontendFilterEnforcesMetricsBearerToken() throws Exception {
        SecurityHardeningFilter protectedFilter = new SecurityHardeningFilter("prom-secret", false, 10, 10);

        MockHttpServletRequest reqNoAuth = new MockHttpServletRequest("GET", "/actuator/prometheus");
        MockHttpServletResponse resNoAuth = new MockHttpServletResponse();
        protectedFilter.doFilter(reqNoAuth, resNoAuth, new MockFilterChain());
        assertEquals(401, resNoAuth.getStatus());

        MockHttpServletRequest reqBasic = new MockHttpServletRequest("GET", "/actuator/prometheus");
        reqBasic.addHeader("Authorization", "Basic xyz");
        MockHttpServletResponse resBasic = new MockHttpServletResponse();
        protectedFilter.doFilter(reqBasic, resBasic, new MockFilterChain());
        assertEquals(401, resBasic.getStatus());

        MockHttpServletRequest reqWrong = new MockHttpServletRequest("GET", "/actuator/prometheus");
        reqWrong.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse resWrong = new MockHttpServletResponse();
        protectedFilter.doFilter(reqWrong, resWrong, new MockFilterChain());
        assertEquals(401, resWrong.getStatus());

        MockHttpServletRequest reqOk = new MockHttpServletRequest("GET", "/actuator/prometheus");
        reqOk.addHeader("Authorization", "Bearer prom-secret");
        MockHttpServletResponse resOk = new MockHttpServletResponse();
        protectedFilter.doFilter(reqOk, resOk, new MockFilterChain());
        assertEquals(200, resOk.getStatus());

        SecurityHardeningFilter openFilter = new SecurityHardeningFilter("", false, 10, 10);
        MockHttpServletRequest reqOpen = new MockHttpServletRequest("GET", "/actuator/prometheus");
        MockHttpServletResponse resOpen = new MockHttpServletResponse();
        openFilter.doFilter(reqOpen, resOpen, new MockFilterChain());
        assertEquals(200, resOpen.getStatus());
    }

    @Test
    @DisplayName("Frontend SecurityHardeningFilter aplica Rate Limiting anti-fuerza bruta y reinicia ventana")
    void frontendFilterEnforcesRateLimiting() throws Exception {
        SecurityHardeningFilter filter = new SecurityHardeningFilter("", true, 1, 2, 50L);

        MockHttpServletRequest login1 = new MockHttpServletRequest("POST", "/verificarLogin");
        login1.addHeader("X-Forwarded-For", "198.51.100.7, 10.0.0.1");
        MockHttpServletResponse resLogin1 = new MockHttpServletResponse();
        filter.doFilter(login1, resLogin1, new MockFilterChain());
        assertEquals(200, resLogin1.getStatus());

        MockHttpServletRequest login2 = new MockHttpServletRequest("POST", "/saveUsuario");
        login2.addHeader("X-Forwarded-For", "198.51.100.7");
        MockHttpServletResponse resLogin2 = new MockHttpServletResponse();
        filter.doFilter(login2, resLogin2, new MockFilterChain());
        assertEquals(429, resLogin2.getStatus());
        assertEquals("60", resLogin2.getHeader("Retry-After"));

        MockHttpServletRequest web1 = new MockHttpServletRequest("GET", "/");
        web1.addHeader("X-Forwarded-For", "   ");
        web1.setRemoteAddr("192.0.2.99");
        MockHttpServletResponse resWeb1 = new MockHttpServletResponse();
        filter.doFilter(web1, resWeb1, new MockFilterChain());
        assertEquals(200, resWeb1.getStatus());

        MockHttpServletRequest web2 = new MockHttpServletRequest("GET", "/");
        web2.setRemoteAddr("192.0.2.99");
        MockHttpServletResponse resWeb2 = new MockHttpServletResponse();
        filter.doFilter(web2, resWeb2, new MockFilterChain());
        assertEquals(200, resWeb2.getStatus());

        MockHttpServletRequest web3 = new MockHttpServletRequest("GET", "/");
        web3.setRemoteAddr("192.0.2.99");
        MockHttpServletResponse resWeb3 = new MockHttpServletResponse();
        filter.doFilter(web3, resWeb3, new MockFilterChain());
        assertEquals(429, resWeb3.getStatus());

        assertTrue(filter.allowRequest("fe-window", 1, 2_000L));
        assertFalse(filter.allowRequest("fe-window", 1, 2_010L));
        assertTrue(filter.allowRequest("fe-window", 1, 2_060L));
    }
}
