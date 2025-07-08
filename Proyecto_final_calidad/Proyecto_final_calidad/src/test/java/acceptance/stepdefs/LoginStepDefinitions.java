package acceptance.stepdefs;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginStepDefinitions {

    private Playwright playwright;
    private Browser browser;
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
    public void verificarDashboard() {
        page.waitForURL("**/dashboard");
        assertTrue(page.url().contains("dashboard"));
    }
}
