package org.example.proyecto_final_calidad;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.*;

public class ApiExternoIntegrationTest {

    private final String BASE_URL = "http://app:8080"; // apunta a tu app en 8080
    private final String username = "admin";
    private final String password = "admin123";
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        restTemplate = new RestTemplate();

        // Evita que RestTemplate lance excepción en 4xx/5xx
        restTemplate.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException { }
        });
    }

    private String obtenerJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "username=" + username + "&password=" + password;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/api/auth", request, String.class);

        assertEquals("Debe responder 200 en autenticación", 200, response.getStatusCodeValue());

        String token = response.getBody();
        assertNotNull("El token JWT no debe ser null o vacío", token);
        assertTrue("El token JWT parece inválido", token.length() > 10);

        return token;
    }

    @Test
    public void autenticarConCredencialesValidas() {
        obtenerJwt();
    }

    @Test
    public void obtenerProductosConJwtDebeFuncionar() {
        String token = obtenerJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "/api/productos", HttpMethod.GET, request, String.class);

        assertEquals("Debe devolver 200 con JWT válido", 200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    public void obtenerProductosSinJwtDebeFallar() {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/api/productos", String.class);
        assertTrue("Debe fallar sin JWT", response.getStatusCode().value() == 401 || response.getStatusCode().value() == 403);
    }

    @Test
    public void obtenerStockEntreFechasConJwt() {
        String token = obtenerJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String desde = LocalDateTime.now().minusDays(5).toString();
        String hasta = LocalDateTime.now().toString();
        String url = BASE_URL + "/api/stock/historial?desde=" + desde + "&hasta=" + hasta;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void crearProductoConJwtDebeFuncionar() {
        String token = obtenerJwt();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = """
        {
          "nombre": "Producto API Test",
          "descripcion": "Producto creado desde test de integración",
          "categoria": "OTROS",
          "precio": 100.0,
          "cantidad": 5,
          "stockMinimo": 1
        }
        """;

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/api/productos", request, String.class);

        assertEquals("Debe devolver 200 OK al crear producto", HttpStatus.OK, response.getStatusCode());
        assertTrue("Debe contener el nombre del producto", response.getBody().contains("Producto API Test"));
    }

    @Test
    public void obtenerProductoPorIdDebeFuncionar() {
        String token = obtenerJwt();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String productoJson = """
        {
            "nombre": "Test ID",
            "descripcion": "Test",
            "precio": 10.0,
            "cantidad": 2,
            "stockMinimo": 1,
            "categoria": "ALIMENTOS"
        }
        """;

        HttpEntity<String> request = new HttpEntity<>(productoJson, headers);
        ResponseEntity<Map> postResponse = restTemplate.postForEntity(BASE_URL + "/api/productos", request, Map.class);
        Long id = Long.valueOf(postResponse.getBody().get("id").toString());

        ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "/api/productos/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals("Debe devolver 200 al buscar producto por ID", 200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Test ID"));
    }

    @Test
    public void registrarMovimientoDeStockDebeFuncionar() {
        String token = obtenerJwt();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String productoJson = """
        {
            "nombre": "Producto Stock",
            "descripcion": "Para prueba",
            "precio": 10.0,
            "cantidad": 5,
            "stockMinimo": 2,
            "categoria": "ALIMENTOS"
        }
        """;

        ResponseEntity<Map> postResponse = restTemplate.postForEntity(BASE_URL + "/api/productos", new HttpEntity<>(productoJson, headers), Map.class);
        Long productoId = Long.valueOf(postResponse.getBody().get("id").toString());

        String url = BASE_URL + "/api/stock/movimiento?productoId=" + productoId + "&cantidad=3&tipo=ENTRADA&usuario=admin";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), String.class);

        assertEquals("Debe registrar movimiento de stock", 200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Movimiento registrado"));
    }

    @Test
    public void accesoSinJwtDebeDevolver401() {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/api/stock/historial", String.class);
        System.out.println(response.getStatusCode());
        assertTrue("Debe devolver 401 o 403", response.getStatusCode().is4xxClientError());
    }
}
