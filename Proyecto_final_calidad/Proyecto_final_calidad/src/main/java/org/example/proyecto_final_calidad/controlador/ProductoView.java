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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.example.proyecto_final_calidad.model.CategoriaProducto;
import org.example.proyecto_final_calidad.model.Producto;
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

    private final Grid<Producto> grid = new Grid<>(Producto.class);

    private final TextField nombre = new TextField("Nombre");
    private final TextField descripcion = new TextField("Descripción");
    private final Select<CategoriaProducto> categoria = new Select<>();
    private final NumberField precio = new NumberField("Precio");
    private final NumberField cantidad = new NumberField("Cantidad");
    private final NumberField stockMinimo = new NumberField("Stock Mínimo");

    private final Button guardar = new Button("Guardar");
    private final Button limpiar = new Button("Limpiar");

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
        verificarRoles();
        configurarEncabezado();
        configurarFiltros();
        configurarFormulario();
        configurarGrid();
        cargarProductos();
        guardar.addClickListener(e -> guardarProducto());
        limpiar.addClickListener(e -> limpiarFormulario());
    }

    private void verificarRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
            isEmployee = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLEADO"));
        }
    }

    private void configurarEncabezado() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 titulo = new H2(isAdmin ? "Administración de Productos" : isEmployee ? "Gestión de Productos" : "Visualización de Productos");

        Button dashboardButton = new Button("Dashboard", e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));
        Button logoutButton = new Button("Cerrar Sesión", e -> cerrarSesion());

        dashboardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout botones = new HorizontalLayout(dashboardButton);

        if (isAdmin) {
            Button usuariosButton = new Button("Ver Usuarios", e -> getUI().ifPresent(ui -> ui.navigate("users")));
            usuariosButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            botones.add(usuariosButton);
        }

        if (isAdmin || isEmployee) {
            Button stockButton = new Button("Control de Stock", e -> getUI().ifPresent(ui -> ui.navigate("stock")));
            stockButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            botones.add(stockButton);
        }

        botones.add(logoutButton);
        headerLayout.add(titulo, botones);
        add(headerLayout);
    }

    private void cerrarSesion() {
        SecurityContextHolder.clearContext();
        VaadinSession.getCurrent().setAttribute("jwt", null);
        getUI().ifPresent(ui -> ui.navigate("login"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            event.forwardTo(LoginView.class);
        }
    }

    private void configurarFormulario() {
        FormLayout formulario = new FormLayout();

        nombre.setRequired(true);
        descripcion.setRequired(true);
        categoria.setLabel("Categoría");
        categoria.setItems(CategoriaProducto.values());
        categoria.setPlaceholder("Seleccione una categoría");
        precio.setMin(0);
        precio.setStep(0.01);
        cantidad.setMin(0);
        cantidad.setStep(1);
        stockMinimo.setMin(0);
        stockMinimo.setStep(1);

        if (isAdmin || isEmployee) {
            HorizontalLayout botones = new HorizontalLayout(guardar, limpiar);
            guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            limpiar.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
            formulario.add(nombre, descripcion, categoria, precio, cantidad, stockMinimo, botones);
            add(formulario);
        }
    }

    private void configurarGrid() {
        HorizontalLayout filtrosBoton = new HorizontalLayout(openFilterButton);
        filtrosBoton.setWidthFull();
        filtrosBoton.setJustifyContentMode(JustifyContentMode.END);
        openFilterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(filtrosBoton);

        grid.setColumns("id", "nombre", "descripcion", "categoria", "precio", "cantidad", "stockMinimo");
        if (isAdmin || isEmployee) {
            grid.addComponentColumn(producto -> {
                Button editar = new Button("Editar", e -> {
                    productoSeleccionado = producto;
                    llenarFormulario(producto);
                });
                editar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button toggleActivo = new Button(producto.isEstaActivo() ? "Desactivar" : "Activar");

                if (isAdmin) {
                    toggleActivo.addClickListener(e -> {
                        ConfirmDialog dialogo = new ConfirmDialog();
                        dialogo.setHeader("Confirmar cambio de estado");
                        dialogo.setText("¿Estás seguro que deseas " + (producto.isEstaActivo() ? "desactivar" : "activar") + " este producto?");

                        dialogo.setConfirmText(producto.isEstaActivo() ? "Desactivar" : "Activar");
                        dialogo.setConfirmButtonTheme(producto.isEstaActivo() ? ButtonVariant.LUMO_ERROR.getVariantName() : ButtonVariant.LUMO_SUCCESS.getVariantName());

                        dialogo.setCancelText("Cancelar");
                        dialogo.setCancelable(true);

                        dialogo.addConfirmListener(event -> {
                            producto.setEstaActivo(!producto.isEstaActivo());
                            _productoServicio.save(producto);

                            Notification.show(
                                producto.isEstaActivo() ? "Producto activado" : "Producto desactivado",3000, Notification.Position.TOP_CENTER
                            );

                            cargarProductos();
                        });

                        dialogo.open();
                    });
                } else if (isEmployee) {
                    toggleActivo.setEnabled(false);
                }


                toggleActivo.addThemeVariants(
                    producto.isEstaActivo() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS
                );

                VerticalLayout acciones = new VerticalLayout(editar, toggleActivo);
                acciones.setSpacing(false);
                acciones.setPadding(false);
                acciones.setMargin(false);
                acciones.setWidthFull();

                return acciones;
            }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);
        }
        add(grid);
    }

    private void cargarProductos() {
        grid.setItems(_productoServicio.findAll());
    }

    private void llenarFormulario(Producto producto) {
        nombre.setValue(producto.getNombre());
        descripcion.setValue(producto.getDescripcion());
        categoria.setValue(producto.getCategoria());
        precio.setValue(producto.getPrecio());
        cantidad.setValue((double) producto.getCantidad());
        stockMinimo.setValue((double) producto.getStockMinimo());
    }

    private void guardarProducto() {
        List<String> errores = validarFormulario();
        if (!errores.isEmpty()) {
            Notification.show(errores.getFirst(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (productoSeleccionado == null) productoSeleccionado = new Producto();
        productoSeleccionado.setNombre(nombre.getValue());
        productoSeleccionado.setDescripcion(descripcion.getValue());
        productoSeleccionado.setCategoria(categoria.getValue());
        productoSeleccionado.setPrecio(precio.getValue());
        productoSeleccionado.setCantidad(cantidad.getValue().intValue());
        productoSeleccionado.setStockMinimo(stockMinimo.getValue().intValue());

        try {
            _productoServicio.save(productoSeleccionado);
            Notification.show("Producto guardado exitosamente");
            limpiarFormulario();
            cargarProductos();
        } catch (Exception e) {
            Notification.show("Error al guardar: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private List<String> validarFormulario() {
        List<String> errores = new ArrayList<>();

        if (nombre.isEmpty()) errores.add("El nombre no puede estar vacío.");
        if (descripcion.isEmpty()) errores.add("La descripción no puede estar vacía.");
        if (categoria.isEmpty()) errores.add("Debe seleccionar una categoría.");
        if (precio.isEmpty() || precio.getValue() < 0) errores.add("Precio inválido.");
        if (cantidad.isEmpty() || cantidad.getValue() < 0) errores.add("Cantidad inválida.");
        if (stockMinimo.isEmpty() || stockMinimo.getValue() < 0) errores.add("Stock mínimo inválido.");

        return errores;
    }

    private void limpiarFormulario() {
        productoSeleccionado = null;
        nombre.clear();
        descripcion.clear();
        categoria.clear();
        precio.clear();
        cantidad.clear();
        stockMinimo.clear();
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
