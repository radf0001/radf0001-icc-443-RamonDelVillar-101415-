package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import org.example.proyecto_final_calidad.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Vista de inicio de sesión de la aplicación.
 * Punto de entrada para que los usuarios se autentiquen.
 * Redirige a usuarios autenticados al panel de control.
 */
@Route("login")
@RouteAlias("inicio")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final TextField usuario = new TextField("Usuario");
    private final PasswordField contrasena = new PasswordField("Contraseña");

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public LoginView(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Contenedor del formulario de inicio de sesión
        VerticalLayout loginForm = new VerticalLayout();
        loginForm.setWidth("400px");
        loginForm.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Sistema de Gestión de Inventario");

        // Configurar campos y botones
        usuario.setWidthFull();
        usuario.setPlaceholder("Ingresa tu usuario");
        contrasena.setWidthFull();
        contrasena.setPlaceholder("Ingresa tu contrasena");
        Button loginButton = new Button("Iniciar sesión");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> login());

        loginForm.add(title, usuario, contrasena, loginButton);
        add(loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verificar si el usuario ya está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            // Redirigir al panel si ya está autenticado
            event.forwardTo(DashboardView.class);
        }
    }

    private void login() {
        try {
            // Autenticación del usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            usuario.getValue().trim(), contrasena.getValue().trim()));

            // Establecer contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generar token JWT
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Almacenar token en sesión
            VaadinSession.getCurrent().setAttribute("jwt", jwt);

            // Navegar al panel de control
            UI.getCurrent().navigate("dashboard");

        } catch (AuthenticationException e) {
            // Mostrar notificación de error
            Notification notification = Notification.show(
                    "Usuario o contrasena inválidos",
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
