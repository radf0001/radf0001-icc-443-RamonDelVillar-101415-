package acceptance.stepdefs;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.junit.Before;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductoSteps {
    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @Before
    public void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    @After
    public void teardown() {
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Given("el usuario está en la página de login")
    public void abrirLogin() {
        page.navigate("http://localhost:8080/login");
    }

    @When("ingresa usuario {string} y contraseña {string}")
    public void ingresarCredenciales(String usuario, String contrasena) {
        page.fill("#input-usuario", usuario);
        page.fill("#input-contrasena", contrasena);
    }

    @When("hace clic en {string}")
    public void hacerClick(String botonId) {
        page.click("#" + botonId);
    }

    @Then("debería ver el dashboard")
    public void validarDashboard() {
        page.waitForURL("**/dashboard");
        assertTrue(page.url().contains("/dashboard"));
    }

    @When("navega a la página de productos")
    public void irAProductos() {
        page.navigate("http://localhost:8080/productos");
    }

    @When("llena el formulario con nombre {string}, descripcion {string}, categoria {string}, precio {string}, cantidad {string}, stockMinimo {string}")
    public void llenarFormulario(String nombre, String descripcion, String categoria, String precio, String cantidad, String stockMinimo) {
        page.fill("#nombre", nombre);
        page.fill("#descripcion", descripcion);
        page.selectOption("#categoria", categoria);
        page.fill("#precio", precio);
        page.fill("#cantidad", cantidad);
        page.fill("#stockMinimo", stockMinimo);
    }

    @Then("debería ver el producto {string} en la tabla")
    public void validarProducto(String nombreProducto) {
        page.waitForSelector("#grid-productos");
        assertTrue(page.content().contains(nombreProducto));
    }
}
