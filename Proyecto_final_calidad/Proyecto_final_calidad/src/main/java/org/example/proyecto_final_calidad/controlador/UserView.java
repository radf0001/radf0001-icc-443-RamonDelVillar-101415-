package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
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
    private final Select<Role> filtroRol = new Select<>();

    private final TextField username = new TextField("Usuario");
    private final PasswordField password = new PasswordField("Contraseña");
    private final EmailField email = new EmailField("Correo Electrónico");
    private final Select<Role> role = new Select<>();

    private final Button save = new Button("Guardar");
    private final Button clear = new Button("Limpiar");
    private final Button filtroButton = new Button("Filtrar");

    private final Dialog filtroDialog = new Dialog();

    private User selectedUser;
    private String currentUsername;
    private boolean isAdmin = false;
    private boolean isEmployee = false;

    public UserView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        save.addClickListener(e -> saveUser());
        clear.addClickListener(e -> clearForm());

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clear.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        filtroButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        grid.setId("gridUsers");
        username.setId("username");
        password.setId("password");
        email.setId("email");
        save.setId("saveUser");
        clear.setId("clearForm");
        filtroButton.setId("filterUser");
        filtroRol.setId("rolFilter");
        role.setId("role");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            event.forwardTo(LoginView.class);
            return;
        }

        isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        isEmployee = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));
        if (!isAdmin) {
            event.forwardTo("dashboard");
            return;
        }

        currentUsername = authentication.getName();
        initializeUI();
    }

    private void initializeUI() {
        removeAll();

        // ENCABEZADO
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 title = new H2("Gestión de Usuarios");

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        Button dashboard = new Button("Dashboard", e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));
        Button productos = new Button("Ver Productos", e -> getUI().ifPresent(ui -> ui.navigate("productos")));
        Button logout = new Button("Cerrar Sesión", e -> logout());

        dashboard.setId("btnDashboard");
        productos.setId("btnVerProductos");
        logout.setId("btnLogout");

        dashboard.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        productos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logout.addThemeVariants(ButtonVariant.LUMO_ERROR);

        buttonsLayout.add(dashboard, productos);
        if (isAdmin || isEmployee) {
            Button stock = new Button("Control de Stock", e -> getUI().ifPresent(ui -> ui.navigate("stock")));
            stock.setId("btnStock");
            stock.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            buttonsLayout.add(stock);
        }
        buttonsLayout.add(logout);

        headerLayout.add(title, buttonsLayout);
        add(headerLayout);

        if (isAdmin) {
            configurarFormulario();
        }

        configurarFiltro();
        configurarGrid();
        loadUsers();
    }

    private void configurarFiltro() {
        filtroRol.setLabel("Filtrar por Rol");
        filtroRol.setItems(Role.values());
        filtroRol.setEmptySelectionAllowed(true);
        filtroRol.setPlaceholder("Todos");

        Button aplicar = new Button("Aplicar", e -> {
            Role seleccionado = filtroRol.getValue();
            if (seleccionado != null) {
                grid.setItems(userRepository.findAll().stream()
                        .filter(u -> u.getRole() == seleccionado)
                        .toList());
            } else {
                loadUsers();
            }
            filtroDialog.close();
        });
        aplicar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button limpiar = new Button("Limpiar", e -> {
            filtroRol.clear();
            loadUsers();
            filtroDialog.close();
        });
        limpiar.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        VerticalLayout contenido = new VerticalLayout(filtroRol, new HorizontalLayout(aplicar, limpiar));
        filtroDialog.setHeaderTitle("Filtrar usuarios");
        filtroDialog.add(contenido);
        filtroDialog.setWidth("300px");

        filtroButton.addClickListener(e -> filtroDialog.open());

        HorizontalLayout wrapper = new HorizontalLayout(filtroButton);
        wrapper.setWidthFull();
        wrapper.setJustifyContentMode(JustifyContentMode.END);
        add(wrapper);
    }

    private void configurarFormulario() {
        FormLayout form = new FormLayout();

        username.setRequired(true);
        password.setRequired(true);
        email.setRequired(true);
        email.setErrorMessage("Correo inválido");

        role.setLabel("Rol");
        role.setItems(Role.values());
        role.setRequiredIndicatorVisible(true);

        HorizontalLayout botones = new HorizontalLayout(save, clear);
        form.add(username, password, email, role, botones);
        add(form);
    }

    private void configurarGrid() {
        grid.setColumns("id", "username", "email", "role");

        if (isAdmin) {
            grid.addComponentColumn(user -> {
                boolean esMismoUsuario = user.getUsername().equals(currentUsername);

                Button editar = new Button("Editar", e -> {
                    selectedUser = user;
                    fillForm(user);
                });
                editar.setEnabled(!esMismoUsuario);
                editar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button toggleEstado = new Button(user.isEnabled() ? "Desactivar" : "Activar");

                toggleEstado.addClickListener(e -> {
                    ConfirmDialog dialogo = new ConfirmDialog();
                    dialogo.setHeader("Confirmar cambio de estado");
                    dialogo.setText("¿Estás seguro que deseas " + (user.isEnabled() ? "desactivar" : "activar") + " este usuario?");

                    dialogo.setConfirmText(user.isEnabled() ? "Desactivar" : "Activar");
                    dialogo.setConfirmButtonTheme(user.isEnabled() ? ButtonVariant.LUMO_ERROR.getVariantName() : ButtonVariant.LUMO_SUCCESS.getVariantName());

                    dialogo.setCancelable(true);
                    dialogo.setCancelText("Cancelar");

                    dialogo.addConfirmListener(event -> {
                        user.setEnabled(!user.isEnabled());
                        userRepository.save(user);

                        Notification.show("Usuario " + (user.isEnabled() ? "activado" : "desactivado"));


                        loadUsers();
                    });

                    dialogo.open();
                });
                toggleEstado.setEnabled(!esMismoUsuario);
                toggleEstado.addThemeVariants(user.isEnabled() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);


                VerticalLayout acciones = new VerticalLayout(editar, toggleEstado);
                acciones.setSpacing(false);
                acciones.setPadding(false);
                return acciones;
            }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        }

        add(grid);
    }

    private void loadUsers() {
        grid.setItems(userRepository.findAll());
    }

    private void fillForm(User user) {
        username.setValue(user.getUsername());
        password.clear(); // No mostrar contraseña
        email.setValue(user.getEmail());
        role.setValue(user.getRole());
    }

    private void saveUser() {
        List<String> errores = validarFormulario();

        if (!errores.isEmpty()) {
            Notification.show(errores.getFirst(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (selectedUser == null) selectedUser = new User();

        if (selectedUser.getId() == null && userRepository.existsByUsername(username.getValue())) {
            Notification.show("El nombre de usuario ya existe", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if ((selectedUser.getId() == null || !email.getValue().equals(selectedUser.getEmail()))
                && userRepository.existsByEmail(email.getValue())) {
            Notification.show("El correo ya está registrado", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        selectedUser.setUsername(username.getValue());
        if (!password.isEmpty()) {
            selectedUser.setPassword(passwordEncoder.encode(password.getValue()));
        }
        selectedUser.setEmail(email.getValue());
        selectedUser.setRole(role.getValue());

        userRepository.save(selectedUser);
        Notification.show("Usuario guardado exitosamente");
        clearForm();
        loadUsers();
    }

    private List<String> validarFormulario() {
        List<String> errores = new ArrayList<>();

        if (username.isEmpty()) errores.add("El nombre de usuario es obligatorio");
        if ((selectedUser == null || selectedUser.getId() == null) && password.isEmpty())
            errores.add("La contraseña es obligatoria");
        if (email.isEmpty() || email.isInvalid()) errores.add("Correo inválido");
        if (role.isEmpty()) errores.add("Debe seleccionar un rol");

        return errores;
    }

    private void clearForm() {
        selectedUser = null;
        username.clear();
        password.clear();
        email.clear();
        role.clear();
        grid.asSingleSelect().clear();
    }

    private void logout() {
        SecurityContextHolder.clearContext();
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute("jwt", null);
        }
        getUI().ifPresent(ui -> ui.navigate("login"));
    }
}
