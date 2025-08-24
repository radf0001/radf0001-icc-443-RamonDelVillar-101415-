package org.example.proyecto_final_calidad.aceptacion.stepdefs;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ProductoSteps extends SpringIntegrationTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private static final boolean SHOW_BROWSER_UI = false;
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int ELEMENT_TIMEOUT = 15000;

    @Before
    public void setup() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(!SHOW_BROWSER_UI));
        }

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 1024)
                .setIgnoreHTTPSErrors(true));
        context.setDefaultTimeout(DEFAULT_TIMEOUT);
        page = context.newPage();
    }

    @After
    public void teardown() {
        if (page != null && !page.isClosed()) page.close();
        if (context != null) context.close();
    }

    @Given("el usuario está autenticado como administrador")
    public void usuarioAutenticado() {
        page.navigate("http://localhost:8080/login");
        fillVaadinField("Usuario", "admin",
                "#input-usuario", "#input-usuario input",
                "vaadin-text-field[id='input-usuario'] input", "[id='input-usuario'] input");

        fillVaadinField("Contraseña", "admin123",
                "#input-contrasena", "#input-contrasena input",
                "vaadin-password-field[id='input-contrasena'] input", "[id='input-contrasena'] input");

        page.locator("#btn-login").click();
        page.waitForURL("**/dashboard");
    }

    @When("el usuario navega a la vista de productos")
    public void irAVistaProductos() {
        page.locator("#btn-ver-productos").click();
        page.waitForURL("**/productos");
        page.waitForSelector("#nombre"); // asegura que el form esté cargado
    }

    @When("completa el formulario con:")
    public void llenarFormulario(DataTable dataTable) {
        Map<String, String> datos = dataTable.asMap(String.class, String.class);

        fillVaadinField("Nombre", datos.get("nombre"),
                "#nombre", "#nombre input",
                "vaadin-text-field[id='nombre'] input", "[id='nombre'] input");

        fillVaadinField("Descripción", datos.get("descripción"),
                "#descripcion", "#descripcion input",
                "vaadin-text-field[id='descripcion'] input", "[id='descripcion'] input");

        handleVaadinSelect("Categoría", datos.get("categoría"),
                "#categoria", "vaadin-select[id='categoria']", "[id='categoria']");

        fillVaadinField("Precio", datos.get("precio"),
                "#precio", "#precio input",
                "vaadin-number-field[id='precio'] input", "[id='precio'] input");

        fillVaadinField("Cantidad", datos.get("cantidad"),
                "#cantidad", "#cantidad input",
                "vaadin-number-field[id='cantidad'] input", "[id='cantidad'] input");

        fillVaadinField("Stock mínimo", datos.get("stockMinimo"),
                "#stockMinimo", "#stockMinimo input",
                "vaadin-number-field[id='stockMinimo'] input", "[id='stockMinimo'] input");
    }

    @When("pulsa el botón Guardar")
    public void pulsarGuardar() {
        page.locator("#btn-guardar").click();
    }

    @Then("ve la notificación {string}")
    public void verificarNotificacion(String mensaje) {
        Locator notification = page.locator("vaadin-notification-card:has-text('" + mensaje + "')");
        notification.waitFor(new Locator.WaitForOptions().setTimeout(5000));

        assertTrue(notification.isVisible());
    }


    // ================= MÉTODOS AUXILIARES =================
    private void fillVaadinField(String fieldName, String value, String...selectors) {
        System.out.println("🖊️ Llenando campo: " + fieldName + " con valor: " + value);
        Locator field = null;
        Exception lastException = null;
        for (String selector: selectors) {
            try {
                field = page.locator(selector);
                if (field.count() > 0 && field.first().isVisible()) {
                    System.out.println("✓ Campo encontrado con selector: " + selector);
                    break;
                }
            } catch (Exception e) {
                lastException = e;
                field = null;
            }
        }
        if (field == null) {
            throw new RuntimeException("No se pudo encontrar el campo: " + fieldName + ". Último error: " + (lastException != null ? lastException.getMessage() : "N/A"));
        }
        try {
            field.waitFor(new Locator.WaitForOptions().setTimeout(ELEMENT_TIMEOUT));
            boolean filled = false;
            try {
                Locator innerInput = field.locator("input").first();
                if (innerInput.count() > 0 && innerInput.isVisible()) {
                    innerInput.clear();
                    innerInput.fill(value);
                    System.out.println("✓ Campo llenado usando input interno");
                    filled = true;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Falló estrategia input interno: " + e.getMessage());
            }
            if (!filled) {
                try {
                    field.click();
                    page.waitForTimeout(300);
                    field.fill("");
                    field.pressSequentially(value, new Locator.PressSequentiallyOptions().setDelay(50));
                    System.out.println("✓ Campo llenado usando pressSequentially");
                    filled = true;
                } catch (Exception e) {
                    System.out.println("⚠️ Falló estrategia pressSequentially: " + e.getMessage());
                }
            }
            if (!filled) {
                try {
                    field.fill(value);
                    System.out.println("✓ Campo llenado usando fill() tradicional");
                    filled = true;
                } catch (Exception e) {
                    System.out.println("⚠️ Falló estrategia fill tradicional: " + e.getMessage());
                }
            }
            if (!filled) {
                try {
                    String selector = selectors[0];
                    page.evaluate(String.format("const element = document.querySelector('%s'); " + "if (element) { " + " element.value = '%s'; " + " element.dispatchEvent(new Event('input', { bubbles: true })); " + " element.dispatchEvent(new Event('change', { bubbles: true })); " + "}", selector.replace("'", "\\'"), value.replace("'", "\\'")));
                    System.out.println("✓ Campo llenado usando JavaScript");
                    filled = true;
                } catch (Exception e) {
                    System.out.println("⚠️ Falló estrategia JavaScript: " + e.getMessage());
                }
            }
            if (!filled) {
                throw new RuntimeException("No se pudo llenar el campo " + fieldName + " con ninguna estrategia");
            }
        } catch (Exception e) {
            captureFailureEvidence(e);
            throw new RuntimeException("Error llenando campo " + fieldName + ": " + e.getMessage(), e);
        }
    }

    private void handleVaadinSelect(String fieldName, String value, String...selectors) {
        System.out.println("🎯 Seleccionando en campo: " + fieldName + " valor: " + value);
        Locator field = waitForAnyLocator(ELEMENT_TIMEOUT, page.locator(selectors[0]), page.locator(selectors.length > 1 ? selectors[1] : selectors[0]), page.locator(selectors.length > 2 ? selectors[2] : selectors[0]));
        try {
            String tagName = field.evaluate("el => el.tagName.toLowerCase()").toString();
            if ("select".equals(tagName)) {
                field.selectOption(value);
                System.out.println("✓ Selección usando selectOption nativo");
                return;
            }
            field.click();
            page.waitForTimeout(500);
            Locator option = waitForAnyLocator(ELEMENT_TIMEOUT, page.locator("vaadin-item").filter(new Locator.FilterOptions().setHasText(value)), page.locator("[role='option']").filter(new Locator.FilterOptions().setHasText(value)), page.locator("vaadin-combo-box-item").filter(new Locator.FilterOptions().setHasText(value)), page.getByText(value).first());
            option.click();
            System.out.println("✓ Selección completada en dropdown Vaadin");
        } catch (Exception e) {
            try {
                String selector = selectors[0];
                page.evaluate(String.format("const element = document.querySelector('%s'); " + "if (element) { " + " element.value = '%s'; " + " element.dispatchEvent(new Event('change', { bubbles: true })); " + "}", selector.replace("'", "\\'"), value.replace("'", "\\'")));
                System.out.println("✓ Selección usando JavaScript fallback");
            } catch (Exception jsError) {
                captureFailureEvidence(e);
                throw new RuntimeException("Error seleccionando en " + fieldName + ": " + e.getMessage(), e);
            }
        }
    }

    private void captureFailureEvidence(Exception e) {
        try {
            System.err.println("🔍 Capturando evidencia del error: " + e.getMessage());
            String timestamp = String.valueOf(System.currentTimeMillis());
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("failure_" + timestamp + ".png")).setFullPage(true));
            try {
                String htmlContent = page.content();
                Files.write(Paths.get("page_source_" + timestamp + ".html"), htmlContent.getBytes());
            } catch (IOException ioException) {
                System.err.println("Error guardando HTML: " + ioException.getMessage());
            }
            System.err.println("URL actual: " + page.url());
            System.err.println("Título de la página: " + page.title());
        } catch (Exception captureException) {
            System.err.println("Error capturando evidencia: " + captureException.getMessage());
        }
    }

    private Locator waitForAnyLocator(int timeout, Locator...locators) {
        Exception lastException = null;
        for (Locator locator: locators) {
            try {
                locator.waitFor(new Locator.WaitForOptions().setTimeout(timeout));
                if (locator.count() > 0) {
                    return locator;
                }
            } catch (Exception e) {
                lastException = e;
                System.out.println("Locator no encontrado: " + locator + " - " + e.getMessage());
            }
        }
        throw new RuntimeException("Ninguno de los locators estuvo disponible. Último error: " + (lastException != null ? lastException.getMessage() : "N/A"));
    }
}
