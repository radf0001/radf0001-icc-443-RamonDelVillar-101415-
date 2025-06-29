package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.example.proyecto_final_calidad.model.Stock;
import org.example.proyecto_final_calidad.model.Producto;
import org.example.proyecto_final_calidad.model.Role;
import org.example.proyecto_final_calidad.model.TipoMovimiento;
import org.example.proyecto_final_calidad.repositorio.ProductoRepositorio;
import org.example.proyecto_final_calidad.servicios.StockServicio;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Route("stock")
public class StockView extends VerticalLayout implements BeforeEnterObserver {

    private final ProductoRepositorio productoRepositorio;
    private final StockServicio controlStockServicio;

    private final ComboBox<Producto> comboProducto = new ComboBox<>("Producto");
    private final ComboBox<TipoMovimiento> comboTipo = new ComboBox<>("Tipo de movimiento");
    private final IntegerField cantidadField = new IntegerField("Cantidad");
    private final Button registrarButton = new Button("Registrar Movimiento");

    private final Grid<Stock> grid = new Grid<>(Stock.class);

    private String currentUsername;
    private boolean isAdmin = false;
    private boolean isEmployee = false;

    private final ComboBox<Producto> filtroProducto = new ComboBox<>("Producto");
    private final ComboBox<TipoMovimiento> filtroTipo = new ComboBox<>("Tipo");
    private final DateTimePicker filtroFechaInicio = new DateTimePicker("Desde");
    private final DateTimePicker filtroFechaFin = new DateTimePicker("Hasta");
    private final Button botonFiltrar = new Button("Aplicar Filtros");
    private final Button botonLimpiar = new Button("Limpiar Filtros");


    public StockView(ProductoRepositorio productoRepositorio, StockServicio controlStockServicio) {
        this.productoRepositorio = productoRepositorio;
        this.controlStockServicio = controlStockServicio;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            event.forwardTo(LoginView.class);
            return;
        }

        currentUsername = auth.getName();
        isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMINISTRADOR.name()));
        isEmployee = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.EMPLEADO.name()));

        if (!isAdmin && !isEmployee) {
            event.forwardTo("dashboard");
            return;
        }

        initializeUI();
    }

    private void initializeUI() {
        removeAll();

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        configurarHeader();

        comboProducto.setItems(productoRepositorio.findAllByEstaActivo(true));
        comboProducto.setItemLabelGenerator(Producto::getNombre);

        comboTipo.setItems(TipoMovimiento.values());
        cantidadField.setMin(1);

        registrarButton.addClickListener(e -> registrarMovimiento());
        registrarButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        if (isAdmin || isEmployee) {
            HorizontalLayout formLayout = new HorizontalLayout(comboProducto, comboTipo, cantidadField, registrarButton);
            formLayout.setWidthFull();
            formLayout.setAlignItems(Alignment.END);
            add(formLayout);
        }

        configurarFiltros();
        configurarGrid();
        cargarHistorial();
    }

    private void configurarFiltros() {
        filtroProducto.setItems(productoRepositorio.findAll());
        filtroProducto.setItemLabelGenerator(Producto::getNombre);
        filtroTipo.setItems(TipoMovimiento.values());

        botonFiltrar.addClickListener(e -> aplicarFiltros());
        botonLimpiar.addClickListener(e -> limpiarFiltros());

        botonFiltrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        botonLimpiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout filtrosLayout = new HorizontalLayout(
                filtroProducto, filtroTipo, filtroFechaInicio, filtroFechaFin, botonFiltrar, botonLimpiar
        );
        filtrosLayout.setWidthFull();
        filtrosLayout.setAlignItems(Alignment.END);

        filtrosLayout.setFlexGrow(0, filtroProducto, filtroTipo, filtroFechaInicio, filtroFechaFin, botonFiltrar, botonLimpiar);
        add(filtrosLayout);
    }

    private void aplicarFiltros() {
        Long productoId = filtroProducto.getValue() != null ? filtroProducto.getValue().getId() : null;
        TipoMovimiento tipo = filtroTipo.getValue();
        LocalDateTime fechaInicio = filtroFechaInicio.getValue();
        LocalDateTime fechaFin = filtroFechaFin.getValue();

        List<Stock> resultados = controlStockServicio.filtrarMovimientos(productoId, tipo, fechaInicio, fechaFin);
        grid.setItems(resultados);
    }


    private void limpiarFiltros() {
        filtroProducto.clear();
        filtroTipo.clear();
        filtroFechaInicio.clear();
        filtroFechaFin.clear();
        cargarHistorial();
    }

    private void configurarGrid() {
        grid.removeAllColumns();
        grid.addColumn(stock -> stock.getProducto().getNombre()).setHeader("Producto");
        grid.addColumn(Stock::getTipo).setHeader("Movimiento");
        grid.addColumn(Stock::getCantidad).setHeader("Cantidad");
        grid.addColumn(Stock::getFecha).setHeader("Fecha");
        grid.addColumn(Stock::getUsuario).setHeader("Registrado por");
        grid.setWidthFull();
        add(grid);
    }

    private void configurarHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H2 encabezado = new H2("Control de Stock");

        HorizontalLayout botones = new HorizontalLayout();

        Button dashboardButton = new Button("Dashboard", e -> getUI().ifPresent(ui -> ui.navigate("dashboard")));
        Button productosButton = new Button("Ver Productos", e -> getUI().ifPresent(ui -> ui.navigate("productos")));
        Button usuariosButton = new Button("Ver Usuarios", e -> getUI().ifPresent(ui -> ui.navigate("users")));
        Button logoutButton = new Button("Cerrar Sesión", e -> logout());

        dashboardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        productosButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        usuariosButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        botones.add(dashboardButton, productosButton);
        if (isAdmin) {
            botones.add(usuariosButton);
        }
        botones.add(logoutButton);

        headerLayout.add(encabezado, botones);
        add(headerLayout);
    }

    private void registrarMovimiento() {
        Producto producto = comboProducto.getValue();
        TipoMovimiento tipo = comboTipo.getValue();
        Integer cantidad = cantidadField.getValue();

        if (producto == null || tipo == null || cantidad == null || cantidad <= 0) {
            Notification.show("Por favor completa todos los campos correctamente.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            controlStockServicio.registrarMovimiento(producto, cantidad, tipo, currentUsername);
            Notification.show("Movimiento registrado exitosamente");
            comboProducto.clear();
            comboTipo.clear();
            cantidadField.clear();
            cargarHistorial();
        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void cargarHistorial() {
        List<Stock> historial = controlStockServicio.obtenerHistorial();
        grid.setItems(historial);
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
