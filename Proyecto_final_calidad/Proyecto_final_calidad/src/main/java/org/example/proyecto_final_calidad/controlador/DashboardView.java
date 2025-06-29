package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import org.example.proyecto_final_calidad.model.Role;
import org.example.proyecto_final_calidad.model.User;
import org.example.proyecto_final_calidad.repositorio.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Dashboard view that serves as the main landing page for all authenticated users.
 * Displays different content based on the user's role.
 */
@Route("dashboard")
@RouteAlias("")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserRepository userRepository;

    public DashboardView(UserRepository userRepository) {
        this.userRepository = userRepository;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            // Redirect to login page if not authenticated
            event.forwardTo(LoginView.class);
            return;
        }

        // User is authenticated, build the dashboard
        buildDashboard(authentication);
    }

    private void buildDashboard(Authentication authentication) {
        // Clear previous content
        removeAll();

        // Get current user
        User currentUser = (User) authentication.getPrincipal();
        Role userRole = currentUser.getRole();

        // Create header with welcome message and logout button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        H1 welcomeMessage = new H1("Bienvenido, " + currentUser.getUsername());
        
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.addClickListener(e -> logout());
        
        headerLayout.add(welcomeMessage, logoutButton);
        add(headerLayout);

        // Add role-specific content
        add(new H2("Panel de Control"));
        
        // Common section for all users
        VerticalLayout commonSection = new VerticalLayout();
        commonSection.add(new Paragraph("Acceso a productos"));
        Button viewProductsButton = new Button("Ver Productos");
        viewProductsButton.addClickListener(e -> UI.getCurrent().navigate("productos"));
        commonSection.add(viewProductsButton);
        add(commonSection);

        // Role-specific sections
        if (userRole == Role.ADMINISTRATOR) {
            addAdministratorSection();
        } else if (userRole == Role.EMPLOYEE) {
            addEmployeeSection();
        } else if (userRole == Role.CUSTOMERGUEST) {
            addCustomerGuestSection();
        }
    }

    private void addAdministratorSection() {
        VerticalLayout adminSection = new VerticalLayout();
        adminSection.add(new H2("Administración"));
        adminSection.add(new Paragraph("Como administrador, tienes acceso completo al sistema."));
        
        Button userManagementButton = new Button("Gestión de Usuarios");
        userManagementButton.addClickListener(e -> UI.getCurrent().navigate("users"));
        
        adminSection.add(userManagementButton);
        adminSection.add(new Paragraph("Puedes gestionar usuarios, crear, editar y eliminar productos."));
        
        add(adminSection);
    }

    private void addEmployeeSection() {
        VerticalLayout employeeSection = new VerticalLayout();
        employeeSection.add(new H2("Gestión"));
        employeeSection.add(new Paragraph("Como gerente, puedes gestionar productos y ver información de usuarios."));
        employeeSection.add(new Paragraph("Tienes permisos para añadir y editar productos."));
        
        add(employeeSection);
    }

    private void addCustomerGuestSection() {
        VerticalLayout customerGuestSection = new VerticalLayout();
        customerGuestSection.add(new H2("Usuario"));
        customerGuestSection.add(new Paragraph("Como usuario básico, puedes ver los productos disponibles."));
        customerGuestSection.add(new Paragraph("No tienes permisos para modificar productos o gestionar usuarios."));
        
        add(customerGuestSection);
    }

    private void logout() {
        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();

        // Eliminar el token JWT de la sesión de Vaadin
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute("jwt", null);
            session.close(); // opcional: cerrar la sesión por completo
        }

        // Redirigir a la página de login
        getUI().ifPresent(ui -> ui.getPage().setLocation("login"));
    }

}