package org.example.proyecto_final_calidad.controlador;

import org.example.proyecto_final_calidad.model.*;
import org.example.proyecto_final_calidad.security.JwtUtils;
import org.example.proyecto_final_calidad.servicios.ProductoServicio;
import org.example.proyecto_final_calidad.servicios.StockServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST que proporciona autenticación y manejo de productos y stock
 * para integración con otros sistemas.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private StockServicio stockServicio;

    /**
     * Autentica al usuario y retorna un JWT.
     *
     * @param username nombre de usuario
     * @param password contraseña
     * @return JWT como String en el cuerpo de la respuesta
     */
    @PostMapping("/auth")
    public ResponseEntity<?> autenticar(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(jwt);
    }

    @GetMapping("/documentacion")
    public ResponseEntity<?> documentacionApi() {
        Map<String, Object> docs = new HashMap<>();

        docs.put("auth", Map.of(
                "POST /api/auth", "Autentica un usuario y retorna un token JWT (params: username, password)"
        ));

        docs.put("productos", Map.ofEntries(
                Map.entry("GET /api/productos", "Lista productos con filtros opcionales (rol: ADMINISTRADOR)"),
                Map.entry("GET /api/productos/{id}", "Obtiene un producto por su ID (rol: ADMINISTRADOR)"),
                Map.entry("POST /api/productos", "Crea un nuevo producto (rol: ADMINISTRADOR)"),
                Map.entry("PUT /api/productos/{id}", "Actualiza un producto existente (rol: ADMINISTRADOR)"),
                Map.entry("DELETE /api/productos/{id}", "Desactiva (no elimina) un producto (rol: ADMINISTRADOR)")
        ));

        docs.put("stock", Map.ofEntries(
                Map.entry("POST /api/stock/movimiento", "Registra un movimiento de stock (rol: ADMINISTRADOR)"),
                Map.entry("GET /api/stock/historial", "Obtiene historial de movimientos con filtros opcionales (rol: ADMINISTRADOR)")
        ));

        docs.put("autenticacion", Map.of(
                "Token JWT", "Todos los endpoints excepto /api/auth requieren el token JWT en el header Authorization: Bearer <token>"
        ));

        return ResponseEntity.ok(docs);
    }


    // --- PRODUCTOS ---
    /**
     * Obtiene una lista de productos filtrados según los parámetros dados.
     * Requiere rol ADMINISTRADOR.
     *
     * @param searchTerm texto de búsqueda en nombre o descripción (opcional)
     * @param categoria categoría del producto (opcional)
     * @param minPrecio precio mínimo (opcional)
     * @param maxPrecio precio máximo (opcional)
     * @param minCantidad cantidad mínima (opcional)
     * @param maxCantidad cantidad máxima (opcional)
     * @return lista de productos que cumplen los filtros
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @GetMapping("/productos")
    public List<Producto> obtenerProductosFiltrados(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) CategoriaProducto categoria,
            @RequestParam(required = false) Double minPrecio,
            @RequestParam(required = false) Double maxPrecio,
            @RequestParam(required = false) Integer minCantidad,
            @RequestParam(required = false) Integer maxCantidad
    ) {
        return productoServicio.findByFilters(searchTerm, categoria, minPrecio, maxPrecio, minCantidad, maxCantidad);
    }

    /**
     * Obtiene un producto por su ID.
     * Requiere rol ADMINISTRADOR.
     *
     * @param id identificador del producto
     * @return producto si existe, 404 si no
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @GetMapping("/productos/{id}")
    public ResponseEntity<?> obtenerProductoPorId(@PathVariable Long id) {
        return productoServicio.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo producto. Campo estaActivo se setea automáticamente en true.
     * Requiere rol ADMINISTRADOR.
     *
     * @param producto objeto producto recibido en el cuerpo
     * @return producto creado o mensaje de error
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @PostMapping("/productos")
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        try {
            producto.setId(null); // Asegura que se cree uno nuevo
            producto.setEstaActivo(true);
            return ResponseEntity.ok(productoServicio.save(producto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Actualiza un producto existente.
     * Requiere rol ADMINISTRADOR.
     *
     * @param id ID del producto a actualizar
     * @param actualizado objeto con los nuevos datos
     * @return producto actualizado o 404 si no se encuentra
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @PutMapping("/productos/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto actualizado) {
        return productoServicio.findById(id).map(p -> {
            p.setNombre(actualizado.getNombre());
            p.setDescripcion(actualizado.getDescripcion());
            p.setCategoria(actualizado.getCategoria());
            p.setPrecio(actualizado.getPrecio());
            p.setCantidad(actualizado.getCantidad());
            p.setStockMinimo(actualizado.getStockMinimo());
            return ResponseEntity.ok(productoServicio.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Marca un producto como inactivo (no lo elimina).
     * Requiere rol ADMINISTRADOR.
     *
     * @param id ID del producto a desactivar
     * @return producto actualizado o 404 si no existe
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<?> desactivarProducto(@PathVariable Long id) {
        return productoServicio.findById(id).map(p -> {
            p.setEstaActivo(false);
            return ResponseEntity.ok(productoServicio.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- STOCK ---
    /**
     * Registra un movimiento de stock (entrada o salida).
     * Requiere rol ADMINISTRADOR.
     *
     * @param productoId ID del producto
     * @param cantidad cantidad a mover (positiva)
     * @param tipo tipo de movimiento (ENTRADA o SALIDA)
     * @param usuario nombre del usuario que realiza el movimiento
     * @return mensaje de éxito o error
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @PostMapping("/stock/movimiento")
    public ResponseEntity<?> registrarMovimiento(@RequestParam Long productoId, @RequestParam int cantidad, @RequestParam TipoMovimiento tipo, @RequestParam String usuario) {
        try {
            Producto p = productoServicio.findById(productoId).orElseThrow();
            stockServicio.registrarMovimiento(p, cantidad, tipo, usuario);
            return ResponseEntity.ok("Movimiento registrado");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtiene el historial de movimientos de stock, con filtros opcionales.
     * Requiere rol ADMINISTRADOR.
     *
     * @param productoId ID del producto (opcional)
     * @param tipo tipo de movimiento (opcional)
     * @param desde fecha/hora desde (opcional, formato ISO 8601)
     * @param hasta fecha/hora hasta (opcional, formato ISO 8601)
     * @return lista de movimientos de stock
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @GetMapping("/stock/historial")
    public List<Stock> historialStock(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        return stockServicio.filtrarMovimientos(productoId, tipo, desde, hasta);
    }
}