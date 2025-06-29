package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.example.proyecto_final_calidad.model.CategoriaProducto;
import org.example.proyecto_final_calidad.model.Producto;
import org.example.proyecto_final_calidad.model.Role;
import org.example.proyecto_final_calidad.servicios.ProductoServicio;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;


@Route("productos")
public class ProductoView extends VerticalLayout implements BeforeEnterObserver {

    private final ProductoServicio _productoServicio;

    private boolean isAdmin = false;
    private boolean isEmployee = false;

    private void checkUserRoles() {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            // Check user roles
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMINISTRATOR.name()))) {
                isAdmin = true;
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.EMPLOYEE.name()))) {
                isEmployee = true;
            }
        }
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
            // If user is neither admin nor manager, redirect to dashboard
            // Regular users should only view products through the dashboard
            event.forwardTo(DashboardView.class);
        }
    }

    private final Grid<Producto> grid = new Grid<>(Producto.class);

    private final TextField nombre = new TextField("Nombre");
    private final TextField descripcion = new TextField("Descripción");
    private final Select<CategoriaProducto> categoria = new Select<>();
    private final NumberField precio = new NumberField("Precio");
    private final NumberField cantidad = new NumberField("Cantidad");

    private final Button guardar = new Button("Guardar");
    private final Button limpiar = new Button("Limpiar");

    // Filter components
    private final Dialog filterDialog = new Dialog();
    private final Button openFilterButton = new Button("Filtros");

    private final TextField searchField = new TextField("Buscar por nombre o descripción");
    private final Select<CategoriaProducto> categoriaFilter = new Select<>();
    private final NumberField minPrecio = new NumberField("Precio mínimo");
    private final NumberField maxPrecio = new NumberField("Precio máximo");
    private final NumberField minCantidad = new NumberField("Cantidad mínima");
    private final NumberField maxCantidad = new NumberField("Cantidad máxima");
    private final Button aplicarFiltros = new Button("Aplicar filtros");
    private final Button limpiarFiltros = new Button("Limpiar filtros");

    private Producto productoSeleccionado;

    public ProductoView(ProductoServicio productoServicio) {
        this._productoServicio = productoServicio;

        // Check user roles before configuring UI components
        checkUserRoles();

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Create title based on role
        H2 title;
        if (isAdmin) {
            title = new H2("Administración de Productos");
        } else if (isEmployee) {
            title = new H2("Gestión de Productos");
        } else {
            title = new H2("Visualización de Productos");
        }

        // Create buttons layout for right side of header
        HorizontalLayout buttonsLayout = new HorizontalLayout();

        // Add dashboard button for all users
        Button dashboardButton = new Button("Dashboard");
        dashboardButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));
        buttonsLayout.add(dashboardButton);

        // Add user management button for admins
        if (isAdmin) {
            Button usersButton = new Button("Gestión de Usuarios");
            usersButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("users")));
            buttonsLayout.add(usersButton);
        }

        // Add logout button
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.addClickListener(e -> logout());
        buttonsLayout.add(logoutButton);

        headerLayout.add(title, buttonsLayout);
        add(headerLayout);

        categoria.setLabel("Categoría");
        categoria.setItems(CategoriaProducto.values());
        categoria.setPlaceholder("Seleccione una categoría");

        configurarFiltros();
        configurarFormulario();

        HorizontalLayout filterButtonLayout = new HorizontalLayout(openFilterButton);
        filterButtonLayout.setWidthFull();
        filterButtonLayout.setJustifyContentMode(JustifyContentMode.END);
        add(filterButtonLayout);

        configurarGrid();
        cargarProductos();

        guardar.addClickListener(e -> guardarProducto());
        limpiar.addClickListener(e -> limpiarFormulario());
    }

    private void configurarFormulario() {
        FormLayout formulario = new FormLayout();

        nombre.setRequired(true);
        descripcion.setRequired(true);
        categoria.setEmptySelectionAllowed(false);
        categoria.setRequiredIndicatorVisible(true);
        precio.setRequired(true);
        cantidad.setRequired(true);

        // Set validation for price
        precio.setMin(0);
        precio.setStep(0.01);
        precio.setHelperText("Debe ser mayor o igual a 0");

        // Set validation for quantity
        cantidad.setMin(0);
        cantidad.setStep(1);
        cantidad.setHelperText("Debe ser un número entero mayor o igual a 0");

        // Show form to administrators and managers
        if (isAdmin || isEmployee) {
            // Agrupar botones en una sola fila
            HorizontalLayout botonesLayout = new HorizontalLayout(guardar, limpiar);
            formulario.add(nombre, descripcion, categoria, precio, cantidad, botonesLayout);
            add(formulario);

            // Add title based on role
            if (isAdmin) {
                add(new H2("Administración de productos"));
            } else {
                add(new H2("Gestión de productos"));
            }
        } else {
            // Show message for regular users
            add(new H2("Visualización de productos"));
        }
    }

    private void configurarGrid() {
        grid.setColumns("id", "nombre", "descripcion", "categoria", "precio", "cantidad");

        // Add action buttons based on user role
        if (isAdmin || isEmployee) {
            // Add a column with action buttons
            grid.addComponentColumn(producto -> {
                HorizontalLayout buttonsLayout = new HorizontalLayout();

                // Create edit button (available to both admin and manager)
                Button editButton = new Button("Editar");
                editButton.addClickListener(e -> {
                    productoSeleccionado = producto;
                    llenarFormulario(productoSeleccionado);
                });
                buttonsLayout.add(editButton);

                // Create delete button (only available to admin)
                if (isAdmin) {
                    Button deleteButton = new Button("Eliminar");
                    deleteButton.addClickListener(e -> {
                        // Confirm deletion
                        ConfirmDialog dialog = new ConfirmDialog(
                                "Confirmar eliminación",
                                "¿Está seguro que desea eliminar este producto?",
                                "Eliminar", event -> {
                            // Delete the product
                            _productoServicio.deleteById(producto.getId());
                            Notification.show("Producto eliminado");
                            cargarProductos(); // Refresh the grid
                        },
                                "Cancelar", event -> {
                            // User cancelled, do nothing
                        }
                        );
                        dialog.open();
                    });
                    buttonsLayout.add(deleteButton);
                }

                return buttonsLayout;
            }).setHeader("Acciones");
        }

        add(grid);
    }

    private void cargarProductos() {
        List<Producto> productos = _productoServicio.findAll();
        grid.setItems(productos);
    }

    private void llenarFormulario(Producto producto) {
        nombre.setValue(producto.getNombre() != null ? producto.getNombre() : "");
        descripcion.setValue(producto.getDescripcion() != null ? producto.getDescripcion() : "");
        categoria.setValue(producto.getCategoria());
        precio.setValue(producto.getPrecio());
        cantidad.setValue((double) producto.getCantidad());
    }

    private void guardarProducto() {
        // Validate input fields
        List<String> errors = validateForm();

        if (!errors.isEmpty()) {
            // Show error notification
            Notification notification = Notification.show(
                    errors.getFirst(),
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (productoSeleccionado == null) {
            productoSeleccionado = new Producto();
        }

        productoSeleccionado.setNombre(nombre.getValue());
        productoSeleccionado.setDescripcion(descripcion.getValue());
        productoSeleccionado.setCategoria(categoria.getValue());
        productoSeleccionado.setPrecio(precio.getValue() != null ? precio.getValue() : 0);
        productoSeleccionado.setCantidad(cantidad.getValue() != null ? cantidad.getValue().intValue() : 0);

        try {
            _productoServicio.save(productoSeleccionado);
            Notification.show("Producto guardado");
            limpiarFormulario();
            cargarProductos();
        } catch (IllegalArgumentException e) {
            Notification notification = Notification.show(
                    "Error al guardar: " + e.getMessage(),
                    5000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        // Name validation
        if (nombre.getValue() == null || nombre.getValue().trim().isEmpty()) {
            errors.add("El nombre no puede estar vacío.");
            nombre.setInvalid(true);
        } else {
            nombre.setInvalid(false);
        }

        // Description validation
        if (descripcion.getValue() == null || descripcion.getValue().trim().isEmpty()) {
            errors.add("La descripción no puede estar vacía.");
            descripcion.setInvalid(true);
        } else {
            descripcion.setInvalid(false);
        }

        // Category validation
        if (categoria.getValue() == null) {
            errors.add("Debe seleccionar una categoría.");
            categoria.setInvalid(true);
        } else {
            categoria.setInvalid(false);
        }

        // Price validation
        if (precio.getValue() == null) {
            errors.add("El precio no puede estar vacío.");
            precio.setInvalid(true);
        } else if (precio.getValue() < 0) {
            errors.add("El precio no puede ser negativo.");
            precio.setInvalid(true);
        } else {
            precio.setInvalid(false);
        }

        // Quantity validation
        if (cantidad.getValue() == null) {
            errors.add("La cantidad no puede estar vacía.");
            cantidad.setInvalid(true);
        } else if (cantidad.getValue() < 0) {
            errors.add("La cantidad no puede ser negativa.");
            cantidad.setInvalid(true);
        } else if (cantidad.getValue() != Math.floor(cantidad.getValue())) {
            errors.add("La cantidad no puede ser decimal.");
            cantidad.setInvalid(true);
        } else {
            cantidad.setInvalid(false);
        }

        return errors;
    }

    private void limpiarFormulario() {
        productoSeleccionado = null;
        nombre.clear();
        descripcion.clear();
        categoria.clear();
        precio.clear();
        cantidad.clear();
        grid.asSingleSelect().clear();
    }

    private void configurarFiltros() {
        // Configure filter components
        searchField.setPlaceholder("Ingrese texto a buscar");
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull(); // Make search field take full width

        categoriaFilter.setLabel("Filtrar por categoría");
        categoriaFilter.setItems(CategoriaProducto.values());
        categoriaFilter.setPlaceholder("Todas las categorías");
        categoriaFilter.setEmptySelectionAllowed(true);
        categoriaFilter.setWidthFull(); // Make category filter take full width

        minPrecio.setPlaceholder("Mínimo");
        minPrecio.setMin(0);
        minPrecio.setClearButtonVisible(true);

        maxPrecio.setPlaceholder("Máximo");
        maxPrecio.setMin(0);
        maxPrecio.setClearButtonVisible(true);

        minCantidad.setPlaceholder("Mínimo");
        minCantidad.setMin(0);
        minCantidad.setStep(1);
        minCantidad.setClearButtonVisible(true);

        maxCantidad.setPlaceholder("Máximo");
        maxCantidad.setMin(0);
        maxCantidad.setStep(1);
        maxCantidad.setClearButtonVisible(true);

        // Add click listeners to filter buttons
        aplicarFiltros.addClickListener(e -> {
            aplicarFiltros();
            filterDialog.close(); // Close dialog after applying filters
        });

        limpiarFiltros.addClickListener(e -> {
            limpiarFiltros();
            filterDialog.close(); // Close dialog after clearing filters
        });

        // Create a vertical layout for the search and category fields
        VerticalLayout searchAndCategoryLayout = new VerticalLayout(searchField, categoriaFilter);
        searchAndCategoryLayout.setSpacing(true);
        searchAndCategoryLayout.setPadding(false);
        searchAndCategoryLayout.setWidthFull();

        HorizontalLayout precioLayout = new HorizontalLayout(minPrecio, maxPrecio);
        precioLayout.setWidthFull();

        HorizontalLayout cantidadLayout = new HorizontalLayout(minCantidad, maxCantidad);
        cantidadLayout.setWidthFull();

        HorizontalLayout botonesLayout = new HorizontalLayout(aplicarFiltros, limpiarFiltros);
        botonesLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout filtrosForm = new VerticalLayout();
        filtrosForm.add(searchAndCategoryLayout, precioLayout, cantidadLayout, botonesLayout);
        filtrosForm.setSpacing(true);
        filtrosForm.setPadding(true);
        filtrosForm.setWidthFull();

        // Configure the dialog
        filterDialog.setHeaderTitle("Filtros de Productos");
        filterDialog.add(filtrosForm);
        filterDialog.setWidth("500px"); // Make dialog smaller

        // Configure the open filter button
        openFilterButton.addClickListener(e -> filterDialog.open());
    }

    private void aplicarFiltros() {
        String searchTerm = searchField.getValue();
        CategoriaProducto categoriaSelected = categoriaFilter.getValue();
        Double minPrecioValue = minPrecio.getValue();
        Double maxPrecioValue = maxPrecio.getValue();
        Integer minCantidadValue = minCantidad.getValue() != null ? minCantidad.getValue().intValue() : null;
        Integer maxCantidadValue = maxCantidad.getValue() != null ? maxCantidad.getValue().intValue() : null;

        // Validate price range
        if (minPrecioValue != null && maxPrecioValue != null && minPrecioValue > maxPrecioValue) {
            Notification.show("El precio mínimo no puede ser mayor que el precio máximo");
            return;
        }

        // Validate quantity range
        if (minCantidadValue != null && maxCantidadValue != null && minCantidadValue > maxCantidadValue) {
            Notification.show("La cantidad mínima no puede ser mayor que la cantidad máxima");
            return;
        }

        // Apply filters
        List<Producto> filteredProducts = _productoServicio.findByFilters(
                searchTerm,
                categoriaSelected,
                minPrecioValue,
                maxPrecioValue,
                minCantidadValue,
                maxCantidadValue
        );

        grid.setItems(filteredProducts);
    }

    private void limpiarFiltros() {
        searchField.clear();
        categoriaFilter.clear();
        minPrecio.clear();
        maxPrecio.clear();
        minCantidad.clear();
        maxCantidad.clear();

        cargarProductos(); // Reset to show all products
    }
}
