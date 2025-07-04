# Proyecto Final de Calidad de Software
**Sistema de Gestión de Inventario**

---

##  Descripción

Este proyecto es una aplicación web para la gestión de productos y control de stock, desarrollada como parte de la asignatura de Calidad de Software.  
Permite a usuarios con distintos roles (Administrador, Empleado, Invitado) gestionar el catálogo de productos, registrar movimientos de inventario y consultar historiales, tanto desde una interfaz Vaadin como a través de una API REST protegida con JWT.

Este proyecto tiene como objetivo diseñar, implementar y documentar un Sistema de Gestión de Inventario que cubra todo el ciclo de desarrollo y aseguramiento de la calidad, poniendo especial énfasis en la trazabilidad de requisitos, las pruebas funcionales y no funcionales, y la adopción de buenas prácticas de ingeniería.



---

##  Funcionalidades principales

-  **Autenticación y autorización**
    - Login con Spring Security + JWT
    - Roles: `ADMINISTRATOR`, `EMPLOYEE`, `INVITADO`

-  **Gestión de Productos**
    - Crear, editar, desactivar/reactivar
    - Listado filtrable por texto, categoría, precio y stock mínimo

-  **Control de Stock**
    - Registrar entradas y salidas de inventario
    - Alertas cuando un producto baja de su stock mínimo
    - Historial filtrable por fecha, producto y tipo de movimiento

-  **API REST**
    - `POST /api/auth` → Autenticación y generación de JWT
    - CRUD de productos y consulta de stock vía endpoints protegidos

-  **Pruebas de Calidad**
    - **Unitarias & de mutación**: Demo.isTriangle (JUnit + Pitest)
    - **UI cross-browser**: Playwright Java (Chromium, Firefox, WebKit)
    - **Guía de Pruebas manuales** documentada

-  **Dashboard**
    - Vista `/dashboard` con estadísticas clave del inventario

---

## 🛠️ Stack Tecnológico

- **Java 21**, Spring Boot 3.x
- **Vaadin Flow** para UI server-side
- **Spring Security** + JWT
- **PostgreSQL** para persistencia
- **Gradle** como sistema de construcción
- **Playwright Java** para pruebas E2E cross-browser
- **JUnit 5** + **Pitest** para tests unitarios y mutación

---

## Prerrequisitos

- Java 21 JDK
- Gradle 8.x (se incluye wrapper)
- PostgreSQL 12+
- Git
- (Opcional) Docker & Docker Compose

---
##  Requisitos Funcionales

| Código    | Descripción                                                                                                                                                  |
|:---------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **RF-0.1** | Vista `/login` (alias `/inicio`) con campos **Usuario** y **Contraseña**, botón **Iniciar sesión**.                                                          |
| **RF-0.2** | Validación de credenciales vía Spring Security + JWT. Generar token y almacenar en sesión.                                                                   |
| **RF-0.3** | Impedir acceso a vistas protegidas si no hay JWT válido; redirigir a `/login`.                                                                              |
| **RF-0.4** | Si ya existe JWT válido, redirigir de `/login` a `/dashboard` automáticamente.                                                                               |
| **RF-1.1** | Crear producto: `POST /api/productos` y formulario Vaadin. Campos: nombre, descripción, categoría, precio, cantidad, stock mínimo.                           |
| **RF-1.2** | Editar producto: `PUT /api/productos/{id}` y formulario Vaadin.                                                                                               |
| **RF-1.3** | Desactivar/reactivar producto: `DELETE /api/productos/{id}` (soft-delete).                                                                                    |
| **RF-1.4** | Listar y filtrar productos: `GET /api/productos` y grid Vaadin con filtros por texto, categoría, precio y cantidad.                                           |
| **RF-2.1** | Registrar movimiento de stock: `POST /api/stock/movimiento` y formulario Vaadin (producto, tipo, cantidad, usuario).                                          |
| **RF-2.2** | Alertar en UI cuando `cantidad < stockMinimo`.                                                                                                              |
| **RF-2.3** | Historial de stock: `GET /api/stock/historial` y grid Vaadin con filtros por producto, tipo y rango de fechas.                                               |
| **RF-3.1** | Autenticación externa: `POST /api/auth?username=&password=` devuelve JWT en el cuerpo de la respuesta.                                                     |
| **RF-3.2** | Todos los endpoints excepto `/api/auth` requieren cabecera `Authorization: Bearer <token>`.                                                                  |
| **RF-4.1** | Dashboard: vista `/dashboard` que muestra estado general del inventario y estadísticas clave.                                                              |
| **RF-4.2** | Navegación intuitiva en toda la aplicación (botones Lumo, menús, diálogos de confirmación).                                                                 |

---

##  Requisitos No Funcionales

| Código   | Descripción                                                                                                                                                         |
|:--------:|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **RNF-1**  | **Persistencia**: PostgreSQL; esquema generado automáticamente (`spring.jpa.hibernate.ddl-auto=create`).                                                          |
| **RNF-2**  | **Configuración**: Parámetros externalizados en `application.properties` (URL, usuario/clave BD; `app.jwt.secret`; `app.jwt.expiration`).                            |
| **RNF-3**  | **Seguridad**: Spring Security + JWT; control de roles (`ADMINISTRATOR`, `EMPLOYEE`, `INVITADO`) en UI y REST.                                                       |
| **RNF-4**  | **Compatibilidad**: UI Vaadin debe funcionar correctamente en Chrome, Firefox y WebKit (Safari).                                                                     |
| **RNF-5**  | **Desempeño**: Tiempo de respuesta < 200 ms en listados y filtros con hasta 1 000 registros (entorno de desarrollo).                                                |
| **RNF-6**  | **Mantenimiento**: Métricas de cobertura y mutación; plan de actualizaciones trimestrales (pendiente de formalizar).                                                |
| **RNF-7**  | **Contenerización**: *(Pendiente)* Proveer `Dockerfile` y/o `docker-compose.yml` para orquestar app + base de datos.                                                 |
| **RNF-8**  | **Migración de BD**: *(Pendiente)* Integrar Flyway o Liquibase para versionado y migración de esquemas de base de datos.                                           |


