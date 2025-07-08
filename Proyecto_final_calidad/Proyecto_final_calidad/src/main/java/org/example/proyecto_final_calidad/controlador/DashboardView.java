package org.example.proyecto_final_calidad.controlador;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
import org.example.proyecto_final_calidad.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("dashboard")
@RouteAlias("")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserRepository userRepository;
    private final ProductoRepositorio productoRepositorio;

    @Autowired
    public DashboardView(UserRepository userRepository,
                         ProductoRepositorio productoRepositorio) {
        this.userRepository = userRepository;
        this.productoRepositorio = productoRepositorio;
        setId("dashboard-view");
        setSizeFull();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        getStyle().set("gap", "2rem");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            event.forwardTo(LoginView.class);
            return;
        }
        buildDashboard((User) authentication.getPrincipal());
    }

    private void buildDashboard(User currentUser) {
        removeAll();

        HorizontalLayout header = new HorizontalLayout();
        header.setId("dashboard-header");
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 welcome = new H1("Bienvenido, " + currentUser.getUsername());
        welcome.setId("dashboard-welcome");
        Button logout = new Button("Cerrar sesión");
        logout.setId("logout-button");
        logout.addThemeVariants(ButtonVariant.LUMO_ERROR);
        logout.addClickListener(e -> logout());

        header.add(welcome, logout);
        add(header);

        H2 panelTitle = new H2("Panel de Control del Inventario");
        panelTitle.setId("panel-title");
        add(panelTitle);

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setId("stats-layout");
        statsLayout.setWidthFull();
        statsLayout.getStyle().set("gap", "1rem");

        if (currentUser.getRole() == Role.ADMINISTRADOR) {
            long totalProductos = productoRepositorio.count();
            long lowStockCount = productoRepositorio.countByCantidadLessThanMinima();
            long totalUsuarios = userRepository.count();

            statsLayout.add(
                createStatCard("Total de productos", totalProductos, "total-productos"),
                createStatCard("Productos con stock bajo", lowStockCount, "stock-bajo"),
                createStatCard("Total de usuarios", totalUsuarios, "total-usuarios")
            );
            add(statsLayout);
        } else if (currentUser.getRole() == Role.EMPLEADO) {
            long totalProductos = productoRepositorio.count();
            long lowStockCount = productoRepositorio.countByCantidadLessThanMinima();

            statsLayout.add(
                createStatCard("Total de productos", totalProductos, "total-productos"),
                createStatCard("Productos con stock bajo", lowStockCount, "stock-bajo")
            );
            add(statsLayout);
        } else {
            long totalProductos = productoRepositorio.count();

            statsLayout.add(
                createStatCard("Total de productos", totalProductos, "total-productos")
            );
            add(statsLayout);
        }

        HorizontalLayout navLayout = new HorizontalLayout();
        navLayout.setId("nav-layout");
        navLayout.setWidthFull();
        navLayout.getStyle().set("gap", "1rem");

        Button btnProductos = new Button("Ver productos");
        btnProductos.setId("btn-ver-productos");
        btnProductos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnProductos.addClickListener(e -> UI.getCurrent().navigate("productos"));
        navLayout.add(btnProductos);

        if (currentUser.getRole() == Role.ADMINISTRADOR) {
            Button btnUsuarios = new Button("Ver usuarios");
            btnUsuarios.setId("btn-ver-usuarios");
            btnUsuarios.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnUsuarios.addClickListener(e -> UI.getCurrent().navigate("users"));
            navLayout.add(btnUsuarios);
        }

        if (currentUser.getRole() == Role.ADMINISTRADOR || currentUser.getRole() == Role.EMPLEADO) {
            Button btnStock = new Button("Control de stock");
            btnStock.setId("btn-control-stock");
            btnStock.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnStock.addClickListener(e -> UI.getCurrent().navigate("stock"));
            navLayout.add(btnStock);
        }

        add(navLayout);
    }

    private Div createStatCard(String titulo, long valor, String id) {
        Div card = new Div();
        card.setId("card-" + id);
        card.getStyle().set("padding", "1rem");
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("border-radius", "0.5rem");
        card.getStyle().set("width", "14rem");
        card.getStyle().set("text-align", "center");
        card.getStyle().set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
        card.getStyle().set("background-color", "#f9f9f9");

        H3 header = new H3(titulo);
        header.setId("header-" + id);
        Paragraph number = new Paragraph(String.valueOf(valor));
        number.setId("value-" + id);
        number.getStyle().set("font-size", "1.5rem");
        number.getStyle().set("font-weight", "bold");

        card.add(header, number);
        return card;
    }

    private void logout() {
        SecurityContextHolder.clearContext();
        VaadinSession.getCurrent().setAttribute("jwt", null);
        UI.getCurrent().navigate("login");
    }
}
