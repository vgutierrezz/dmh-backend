import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestRegister {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    @DisplayName("CP-REG-001 - Registro con datos válidos")
    @Order(1)
    @Test
    public void shouldRegisterUserWithValidData() {
        // generar email único para evitar duplicados
        String unique = "user" + System.currentTimeMillis() + "@test.com";
        String password = "User1234";
        String dni = String.valueOf(90000000 + (System.currentTimeMillis() % 10000));

        driver.get("http://localhost:3000/register");
        try {
            // esperar a que el formulario se renderice
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));

            // helper sleep to slow down field filling so it's visible during run
            java.util.function.Consumer<Integer> pause = (ms) -> { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} };
            int fieldPause = Integer.parseInt(System.getProperty("ui.field.pause.ms", "400"));

            WebElement nameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-name")));
            nameEl.sendKeys("Test");
            pause.accept(fieldPause);

            WebElement lastEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-last-name")));
            lastEl.sendKeys("User");
            pause.accept(fieldPause);

            WebElement dniEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-dni")));
            dniEl.sendKeys(dni);
            pause.accept(fieldPause);

            WebElement emailEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-email")));
            emailEl.clear();
            emailEl.sendKeys(unique);
            pause.accept(fieldPause);

            WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-password")));
            passEl.sendKeys(password);
            pause.accept(fieldPause);

            WebElement passRepEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-password-repeated")));
            passRepEl.sendKeys(password);
            pause.accept(fieldPause);

            WebElement phoneEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-phone")));
            phoneEl.sendKeys("1555555555");
            pause.accept(fieldPause);

            // enviar formulario
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//form//button[normalize-space()='Ingresar']"))).click();

            // esperar un breve periodo para que el backend cree el usuario
            Thread.sleep(1000);

            // verificar vía API que se puede loguear con las credenciales creadas
            RestAssured.baseURI = System.getProperty("api.base", "http://localhost:8080");
            String token =
                    given()
                            .contentType(ContentType.JSON)
                            .body("{\"email\":\""+unique+"\",\"password\":\""+password+"\"}")
                            .when()
                            .post("/api/auth/login")
                            .then()
                            .statusCode(200)
                            .body("token", allOf(notNullValue(), not(isEmptyString())))
                            .extract().path("token");

            assertNotNull(token, "Expected token from login after registration");

            // adicional: decodificar y verificar email presente
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "Token should be a JWT with 3 parts");
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decoded, StandardCharsets.UTF_8);
            assertTrue(payloadJson.toLowerCase().contains(unique.toLowerCase()), "JWT payload should contain the registered email");

            // espera final para permitir observar la página antes de cerrarla
            int finalPause = Integer.parseInt(System.getProperty("ui.pause.after.ms", "3000"));
            try { Thread.sleep(finalPause); } catch (InterruptedException ignored) {}

        } catch (Exception e) {
            try {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                java.nio.file.Path dest = java.nio.file.Paths.get("target","screenshots","registro-failure-"+java.util.UUID.randomUUID()+".png");
                java.nio.file.Files.createDirectories(dest.getParent());
                java.nio.file.Files.copy(scr.toPath(), dest);
                System.out.println("Screenshot saved to: " + dest.toAbsolutePath());
            } catch (Exception ex) { System.out.println("Failed to save screenshot: " + ex.getMessage()); }
            fail("Registration test failed: " + e.getMessage());
        }
    }

    @DisplayName("CP-REG-002 - Registro con campos incompletos")
    @Order(2)
    @Test
    public void shouldKeepSubmitDisabledWithoutPhone() {
        // generar email único para evitar duplicados
        String unique = "user" + System.currentTimeMillis() + "@test.com";
        String password = "User1234";
        String dni = String.valueOf(90000000 + (System.currentTimeMillis() % 10000));

        driver.get("http://localhost:3000/register");
        try {
            // esperar a que el formulario se renderice
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));

            // helper sleep to slow down field filling so it's visible during run
            java.util.function.Consumer<Integer> pause = (ms) -> { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} };
            int fieldPause = Integer.parseInt(System.getProperty("ui.field.pause.ms", "400"));

            WebElement nameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-name")));
            nameEl.sendKeys("Test");
            pause.accept(fieldPause);

            WebElement lastEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-last-name")));
            lastEl.sendKeys("User");
            pause.accept(fieldPause);

            WebElement dniEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-dni")));
            dniEl.sendKeys(dni);
            pause.accept(fieldPause);

            WebElement emailEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-email")));
            emailEl.clear();
            emailEl.sendKeys(unique);
            pause.accept(fieldPause);

            WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-password")));
            passEl.sendKeys(password);
            pause.accept(fieldPause);

            WebElement passRepEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-password-repeated")));
            passRepEl.sendKeys(password);
            pause.accept(fieldPause);

            // no se completa el teléfono intencionalmente

            // localizar el botón de enviar
            WebElement submitBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form//button[normalize-space()='Ingresar']")));

            // dar un breve tiempo para que la validación reactive el estado del botón
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}

            // verificar que el botón esté deshabilitado
            assertFalse(submitBtn.isEnabled(), "El botón 'Ingresar' debe estar deshabilitado cuando falta el teléfono");

            // espera final para permitir observar la página antes de cerrarla
            int finalPause = Integer.parseInt(System.getProperty("ui.pause.after.ms", "1000"));
            try { Thread.sleep(finalPause); } catch (InterruptedException ignored) {}

        } catch (Exception e) {
            try {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                java.nio.file.Path dest = java.nio.file.Paths.get("target","screenshots","registro-no-phone-failure-"+java.util.UUID.randomUUID()+".png");
                java.nio.file.Files.createDirectories(dest.getParent());
                java.nio.file.Files.copy(scr.toPath(), dest);
                System.out.println("Screenshot saved to: " + dest.toAbsolutePath());
            } catch (Exception ex) { System.out.println("Failed to save screenshot: " + ex.getMessage()); }
            fail("Registration (no-phone) test failed: " + e.getMessage());
        }
    }

    @DisplayName("CP-REG-003 - Mail formato inválido")
    @Order(3)
    @Test
    public void shouldShowInvalidEmailMessage() {
        // generar email único con formato inválido
        String unique = "user" + System.currentTimeMillis() + "#test.com";
        String password = "User1234";
        String dni = String.valueOf(90000000 + (System.currentTimeMillis() % 10000));

        driver.get("http://localhost:3000/register");
        try {
            // esperar a que el formulario se renderice
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));

            // helper sleep to slow down field filling so it's visible during run
            java.util.function.Consumer<Integer> pause = (ms) -> { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} };
            int fieldPause = Integer.parseInt(System.getProperty("ui.field.pause.ms", "400"));

            WebElement nameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-name")));
            nameEl.sendKeys("Test");
            pause.accept(fieldPause);

            WebElement lastEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-last-name")));
            lastEl.sendKeys("User");
            pause.accept(fieldPause);

            WebElement dniEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-dni")));
            dniEl.sendKeys(dni);
            pause.accept(fieldPause);

            WebElement emailEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-email")));
            emailEl.clear();
            emailEl.sendKeys(unique);
            pause.accept(fieldPause);

            WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-password")));
            passEl.sendKeys(password);
            pause.accept(fieldPause);

            WebElement passRepEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-password-repeated")));
            passRepEl.sendKeys(password);
            pause.accept(fieldPause);

            WebElement phoneEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("outlined-adornment-phone")));
            phoneEl.sendKeys("1555555555");
            pause.accept(fieldPause);

            // click en el botón Ingresar para disparar la validación
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//form//button[normalize-space()='Ingresar']")));
            submitBtn.click();

            // dar un tiempo breve para que la validación en UI muestre el mensaje
            // buscar el elemento de error por sus clases
            By errorSelector = By.cssSelector("ul.tw-flex.tw-flex-col.tw-gap-y-4.tw-pt-4.tw-bg-background li.tw-text-error");
            WebElement errorEl = wait.until(ExpectedConditions.visibilityOfElementLocated(errorSelector));

            String errorText = errorEl.getText().trim();
            assertEquals("Correo electrónico inválido", errorText, "Se espera mensaje de correo inválido");

            // espera final para permitir observar la página antes de cerrarla
            int finalPause = Integer.parseInt(System.getProperty("ui.pause.after.ms", "1000"));
            try { Thread.sleep(finalPause); } catch (InterruptedException ignored) {}

        } catch (Exception e) {
            try {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                java.nio.file.Path dest = java.nio.file.Paths.get("target","screenshots","registro-invalid-email-failure-"+java.util.UUID.randomUUID()+".png");
                java.nio.file.Files.createDirectories(dest.getParent());
                java.nio.file.Files.copy(scr.toPath(), dest);
                System.out.println("Screenshot saved to: " + dest.toAbsolutePath());
            } catch (Exception ex) { System.out.println("Failed to save screenshot: " + ex.getMessage()); }
            fail("Registration (invalid email) test failed: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("CP-REG-004 - Registro: Rechazar email ya registrado")
    void shouldRejectAlreadyRegisteredEmail() {

        String email = "user" + System.currentTimeMillis() + "@test.com";
        String password = "User1234";

        String dni1 = String.valueOf(
                90000000 + (System.currentTimeMillis() % 10000)
        );

        String dni2 = String.valueOf(
                91000000 + (System.currentTimeMillis() % 10000)
        );

        // Primer registro exitoso
        registerUser(email, dni1, password);

        // Limpiar sesión
        driver.manage().deleteAllCookies();

        JavascriptExecutor js =
                (JavascriptExecutor) driver;

        js.executeScript("window.localStorage.clear();");
        js.executeScript("window.sessionStorage.clear();");

        // Segundo registro con mismo email
        registerUser(email, dni2, password);

        WebElement snackEl = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div.tw-bg-error")
                )
        );

        assertEquals(
                "El usuario ya existe",
                snackEl.getText().trim()
        );
    }

    @DisplayName("CP-REG-005 - UserResponse no expone password")
    @Order(5)
    @Test
    public void shouldNotExposePasswordInUserResponse() {

        String email = "user" + System.currentTimeMillis() + "@test.com";
        String password = "User1234";

        String dni = String.valueOf(
                90000000 + (System.currentTimeMillis() % 10000)
        );

        try {

            // Registrar usuario
            registerUser(email, dni, password);

            // Esperar a que el usuario quede persistido
            Thread.sleep(2000);

            RestAssured.baseURI = System.getProperty(
                    "api.base",
                    "http://localhost:8080"
            );

            Response userResponse =
                    given()
                            .when()
                            .get("/api/users/" + email);

            System.out.println("===== USER RESPONSE =====");
            System.out.println("Status: " + userResponse.statusCode());
            System.out.println("Body: " + userResponse.asPrettyString());

            assertEquals(
                    200,
                    userResponse.statusCode(),
                    "El endpoint de usuario debería responder 200"
            );

            userResponse.then()
                    .body("$", not(hasKey("password")));

            assertFalse(
                    userResponse.asString().contains(password),
                    "La contraseña no debe aparecer en la respuesta"
            );

        } catch (Throwable e) {

            try {
                File scr = ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.FILE);

                java.nio.file.Path dest =
                        java.nio.file.Paths.get(
                                "target",
                                "screenshots",
                                "registro-no-password-exposed-failure-"
                                        + java.util.UUID.randomUUID()
                                        + ".png"
                        );

                java.nio.file.Files.createDirectories(
                        dest.getParent()
                );

                java.nio.file.Files.copy(
                        scr.toPath(),
                        dest
                );

                System.out.println(
                        "Screenshot saved to: "
                                + dest.toAbsolutePath()
                );

            } catch (Exception ex) {
                System.out.println(
                        "Failed to save screenshot: "
                                + ex.getMessage()
                );
            }

            fail(
                    "CP-REG-005 falló: "
                            + e.getMessage()
            );
        }
    }

    private void registerUser(String email,
                              String dni,
                              String password) {

        driver.get("http://localhost:3000/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("outlined-adornment-email")
        ));

        driver.findElement(By.id("outlined-adornment-name"))
                .sendKeys("Test");

        driver.findElement(By.id("outlined-adornment-last-name"))
                .sendKeys("User");

        driver.findElement(By.id("outlined-adornment-dni"))
                .sendKeys(dni);

        driver.findElement(By.id("outlined-adornment-email"))
                .sendKeys(email);

        driver.findElement(By.id("outlined-adornment-password"))
                .sendKeys(password);

        driver.findElement(By.id("outlined-adornment-password-repeated"))
                .sendKeys(password);

        driver.findElement(By.id("outlined-adornment-phone"))
                .sendKeys("1555555555");

        driver.findElement(
                By.xpath("//form//button[normalize-space()='Ingresar']")
        ).click();
    }

    private void saveScreenshot(String prefix) {
        try {
            File screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            java.nio.file.Path destination =
                    java.nio.file.Paths.get(
                            "target",
                            "screenshots",
                            prefix + "-"
                                    + java.util.UUID.randomUUID()
                                    + ".png"
                    );

            java.nio.file.Files.createDirectories(
                    destination.getParent()
            );

            java.nio.file.Files.copy(
                    screenshot.toPath(),
                    destination
            );

            System.out.println(
                    "Screenshot saved to: "
                            + destination.toAbsolutePath()
            );
        } catch (Exception screenshotError) {
            System.out.println(
                    "Failed to save screenshot: "
                            + screenshotError.getMessage()
            );
        }
    }
}
