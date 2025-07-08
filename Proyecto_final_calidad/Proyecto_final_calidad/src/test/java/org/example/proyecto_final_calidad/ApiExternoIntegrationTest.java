package org.example.proyecto_final_calidad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiExternoIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String username = "admin";
    private final String password = "admin123";

    private String obtenerJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "username=" + username + "&password=" + password;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // Recibe directamente el JWT como String
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth", request, String.class);

        assertEquals(200, response.getStatusCodeValue(), "Debe responder 200 en autenticación");

        String token = response.getBody();
        assertNotNull(token, "El token JWT no debe ser null o vacío");
        assertTrue(token.length() > 10, "El token JWT parece inválido");

        return token;
    }


    @Test
    void autenticarConCredencialesValidas() {
        obtenerJwt(); // Esto ya valida el login correctamente
    }

    @Test
    void obtenerProductosConJwtDebeFuncionar() {
        String token = obtenerJwt();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/productos", HttpMethod.GET, request, String.class);

        assertEquals(200, response.getStatusCodeValue(), "Debe devolver 200 con JWT válido");
        assertNotNull(response.getBody());
    }

    @Test
    void obtenerProductosSinJwtDebeFallar() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/productos", String.class);

        // 👇 Imprimir el código de estado y el cuerpo de la respuesta
        System.out.println("Código HTTP: " + response.getStatusCodeValue());
        System.out.println("Cuerpo: " + response.getBody());

        assertTrue(response.getStatusCode().value() == 401 || response.getStatusCode().value() == 403, "Debe fallar sin JWT");
    }

    @Test
    void obtenerStockEntreFechasConJwt() {
        String token = obtenerJwt();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String desde = LocalDateTime.now().minusDays(5).toString();
        String hasta = LocalDateTime.now().toString();

        String url = "/api/stock/historial?desde=" + desde + "&hasta=" + hasta;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void crearProductoConJwtDebeFuncionar() {
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

        ResponseEntity<String> response = restTemplate.postForEntity("/api/productos", request, String.class);

        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Debe devolver 200 OK al crear producto");
        assertTrue(response.getBody().contains("Producto API Test"), "Debe contener el nombre del producto");
    }

    @Test
    void obtenerProductoPorIdDebeFuncionar() {
        String token = obtenerJwt();

        // Primero crea un producto
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
        ResponseEntity<Map> postResponse = restTemplate.postForEntity("/api/productos", request, Map.class);
        Long id = Long.valueOf(postResponse.getBody().get("id").toString());

        // Ahora buscarlo por ID
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/productos/" + id, HttpMethod.GET, getRequest, String.class);

        assertEquals(200, response.getStatusCodeValue(), "Debe devolver 200 al buscar producto por ID");
        assertTrue(response.getBody().contains("Test ID"));
    }

    @Test
    void registrarMovimientoDeStockDebeFuncionar() {
        String token = obtenerJwt();

        // Crear producto primero
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

        ResponseEntity<Map> postResponse = restTemplate.postForEntity("/api/productos", new HttpEntity<>(productoJson, headers), Map.class);
        Long productoId = Long.valueOf(postResponse.getBody().get("id").toString());

        // Movimiento de stock
        String url = "/api/stock/movimiento?productoId=" + productoId + "&cantidad=3&tipo=ENTRADA&usuario=admin";

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        assertEquals(200, response.getStatusCodeValue(), "Debe registrar movimiento de stock");
        assertTrue(response.getBody().contains("Movimiento registrado"));
    }

    @Test
    void accesoSinJwtDebeDevolver401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/stock/historial", String.class);
        assertTrue(response.getStatusCode().is4xxClientError(), "Debe devolver 401 o 403");
    }
}

