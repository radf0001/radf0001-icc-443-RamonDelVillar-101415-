package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
 * Login view for the application.
 * This is the entry point for users to authenticate.
 * Redirects authenticated users to the dashboard.
 */
@Route("login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button loginButton = new Button("Login");

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public LoginView(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Create a container for the login form
        VerticalLayout loginForm = new VerticalLayout();
        loginForm.setWidth("400px");
        loginForm.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Sistema de Gestión de Productos");

        username.setWidthFull();
        password.setWidthFull();
        loginButton.setWidthFull();

        loginButton.addClickListener(e -> login());

        loginForm.add(title, username, password, loginButton);
        add(loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if user is already authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            // Redirect to dashboard if already authenticated
            event.forwardTo(DashboardView.class);
        }
    }

    private void login() {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.getValue(), password.getValue()));

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Store the token in the session
            VaadinSession.getCurrent().setAttribute("jwt", jwt);

            // Navigate to the dashboard
            UI.getCurrent().navigate("dashboard");

        } catch (AuthenticationException e) {
            // Show error notification
            Notification notification = Notification.show(
                    "Invalid username or password",
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
