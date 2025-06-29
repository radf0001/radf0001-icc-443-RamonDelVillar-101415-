package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.example.proyecto_final_calidad.model.Role;
import org.example.proyecto_final_calidad.model.User;
import org.example.proyecto_final_calidad.repositorio.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Route("users")
public class UserView extends VerticalLayout implements BeforeEnterObserver {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Grid<User> grid = new Grid<>(User.class);

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final EmailField email = new EmailField("Email");
    private final Select<Role> role = new Select<>();

    private final Button save = new Button("Save");
    private final Button clear = new Button("Clear");

    private User selectedUser;

    public UserView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        // Event handlers for buttons
        save.addClickListener(e -> saveUser());
        clear.addClickListener(e -> clearForm());
    }

    private void initializeUI() {
        // Clear previous content
        removeAll();

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Create title based on role
        H2 title;
        if (isAdmin) {
            title = new H2("User Management");
        } else {
            title = new H2("User Information");
        }

        // Create buttons layout for right side of header
        HorizontalLayout buttonsLayout = new HorizontalLayout();

        // Add dashboard button
        Button dashboardButton = new Button("Dashboard");
        dashboardButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));
        buttonsLayout.add(dashboardButton);

        // Add products button
        Button productsButton = new Button("Products");
        productsButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("productos")));
        buttonsLayout.add(productsButton);

        // Add logout button
        Button logoutButton = new Button("Logout");
        logoutButton.addClickListener(e -> logout());
        buttonsLayout.add(logoutButton);

        headerLayout.add(title, buttonsLayout);
        add(headerLayout);

        configureForm();
        configureGrid();
        loadUsers();
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


    private boolean isAdmin = false;
    private boolean isEmployee = false;

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

        // Reset role flags
        isAdmin = false;
        isEmployee = false;

        // Check user roles
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMINISTRATOR.name()))) {
            isAdmin = true;
        } else if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.EMPLOYEE.name()))) {
            isEmployee = true;
        } else {
            // Redirect to dashboard if not an administrator or manager
            event.forwardTo(DashboardView.class);
            return;
        }

        // Initialize UI components after role flags are set
        initializeUI();
    }

    private void configureForm() {
        // Only show form to administrators
        if (isAdmin) {
            FormLayout form = new FormLayout();

            username.setRequired(true);
            password.setRequired(true);
            email.setRequired(true);

            role.setLabel("Role");
            role.setItems(Role.values());
            role.setEmptySelectionAllowed(false);
            role.setRequiredIndicatorVisible(true);

            // Group buttons in a single row
            HorizontalLayout buttonsLayout = new HorizontalLayout(save, clear);
            form.add(username, password, email, role, buttonsLayout);
            add(form);
        } else {
            // Show message for managers
            add(new H2("User information view only"));
        }
    }

    private void configureGrid() {
        // Configure which columns to show
        grid.setColumns("id", "username", "email", "role");

        // Add action buttons only for administrators
        if (isAdmin) {
            // Add a column with edit and delete buttons
            grid.addComponentColumn(user -> {
                // Create edit button
                Button editButton = new Button("Edit");
                editButton.addClickListener(e -> {
                    selectedUser = user;
                    fillForm(selectedUser);
                });

                // Create delete button
                Button deleteButton = new Button("Delete");
                deleteButton.addClickListener(e -> {
                    // Confirm deletion
                    ConfirmDialog dialog = new ConfirmDialog(
                            "Confirm deletion",
                            "Are you sure you want to delete this user?",
                            "Delete", event -> {
                        // Delete the user
                        userRepository.deleteById(user.getId());
                        Notification.show("User deleted");
                        loadUsers(); // Refresh the grid
                    },
                            "Cancel", event -> {
                        // User cancelled, do nothing
                    }
                    );
                    dialog.open();
                });

                // Return a layout with both buttons
                return new HorizontalLayout(editButton, deleteButton);
            }).setHeader("Actions");
        }

        add(grid);
    }

    private void loadUsers() {
        List<User> users = userRepository.findAll();
        grid.setItems(users);
    }

    private void fillForm(User user) {
        username.setValue(user.getUsername() != null ? user.getUsername() : "");
        // Don't fill the password field for security reasons
        password.clear();
        email.setValue(user.getEmail() != null ? user.getEmail() : "");
        role.setValue(user.getRole());
    }

    private void saveUser() {
        // Validate input fields
        List<String> errors = validateForm();

        if (!errors.isEmpty()) {
            // Show error notification
            Notification notification = Notification.show(
                    errors.get(0),
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (selectedUser == null) {
            selectedUser = new User();
        }

        // Check if username already exists (for new users)
        if (selectedUser.getId() == null && userRepository.existsByUsername(username.getValue())) {
            Notification notification = Notification.show(
                    "Username already exists",
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Check if email already exists (for new users or email change)
        if ((selectedUser.getId() == null || !email.getValue().equals(selectedUser.getEmail()))
                && userRepository.existsByEmail(email.getValue())) {
            Notification notification = Notification.show(
                    "Email already exists",
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        selectedUser.setUsername(username.getValue());
        // Only update password if it's provided
        if (!password.getValue().isEmpty()) {
            selectedUser.setPassword(passwordEncoder.encode(password.getValue()));
        }
        selectedUser.setEmail(email.getValue());
        selectedUser.setRole(role.getValue());

        try {
            userRepository.save(selectedUser);
            Notification.show("User saved");
            clearForm();
            loadUsers();
        } catch (Exception e) {
            Notification notification = Notification.show(
                    "Error saving user: " + e.getMessage(),
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        // Username validation
        if (username.getValue() == null || username.getValue().trim().isEmpty()) {
            errors.add("Username cannot be empty");
            username.setInvalid(true);
        } else if (username.getValue().length() < 3) {
            errors.add("Username must be at least 3 characters");
            username.setInvalid(true);
        } else {
            username.setInvalid(false);
        }

        // Password validation (only for new users)
        if (selectedUser == null || selectedUser.getId() == null) {
            if (password.getValue() == null || password.getValue().trim().isEmpty()) {
                errors.add("Password cannot be empty");
                password.setInvalid(true);
            } else if (password.getValue().length() < 8) {
                errors.add("Password must be at least 8 characters");
                password.setInvalid(true);
            } else {
                password.setInvalid(false);
            }
        }

        // Email validation
        if (email.getValue() == null || email.getValue().trim().isEmpty()) {
            errors.add("Email cannot be empty");
            email.setInvalid(true);
        } else if (!email.isInvalid()) {
            // The EmailField component already validates email format
            email.setInvalid(false);
        }

        // Role validation
        if (role.getValue() == null) {
            errors.add("Role must be selected");
            role.setInvalid(true);
        } else {
            role.setInvalid(false);
        }

        return errors;
    }

    private void clearForm() {
        selectedUser = null;
        username.clear();
        password.clear();
        email.clear();
        role.clear();
        grid.asSingleSelect().clear();
    }
}
