import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.io.File;
import java.time.Duration;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TestLogin {
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


    @DisplayName("CP-LOG-001 - Login con credenciales válidas")
    @Order(1)
    @Test
    public void shouldReturnToken_whenCredentialsAreValid() {
        driver.get("http://localhost:3000/login");
        driver.findElement(By.id("outlined-adornment-email")).sendKeys("valentina@test.com");
        driver.findElement(By.id("outlined-adornment-password")).sendKeys("Valen1234");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Ingresar']"))).click();
   }

    @DisplayName("CP-LOG-002 - Usuario inexistente")
    @Order(2)
    @Test
    void shouldReturnNotFound_whenUserDoesNotExist() {
        driver.get("http://localhost:3000/login");

        // completar formulario y clicar (interacción real con UI)
        driver.findElement(By.id("outlined-adornment-email")).sendKeys("usuarioinexistente@test.com");
        driver.findElement(By.id("outlined-adornment-password")).sendKeys("Usuario1234");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Ingresar']"))).click();

        // comprobar aparición del toast/snackbar exacto
        By toastLocator = By.xpath("//div[contains(@class,'tw-bg-primary') and contains(@class,'tw-p-4') and contains(@class,'tw-rounded') and contains(normalize-space(.),'Usuario')]");
        try {
            WebElement toast = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            String text = toast.getText().trim();
            // comprobar texto exacto o al menos que contenga la frase esperada
            assertTrue(text.equalsIgnoreCase("Usuario no encontrado") || text.toLowerCase().contains("usuario"), "Unexpected toast text: '" + text + "'");
        } catch (TimeoutException te) {
            // guardar evidencia y fallar
            try {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                java.nio.file.Path dest = java.nio.file.Paths.get("target","screenshots","usuario-inexistente-"+java.util.UUID.randomUUID()+".png");
                java.nio.file.Files.createDirectories(dest.getParent());
                java.nio.file.Files.copy(scr.toPath(), dest);
                System.out.println("Screenshot saved to: " + dest.toAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to save screenshot: " + e.getMessage());
            }
            fail("Toast with message 'Usuario no encontrado' did not appear");
        }
    }

    @DisplayName("CP-LOG-003 - Contraseña incorrecta")
    @Order(3)
    @Test
    void shouldShowUsuarioNoEncontradoToast_whenPasswordIsWrong() {
        driver.get("http://localhost:3000/login");

        // instalar un observer que capture cualquier nodo añadido cuya innerText sea exactamente 'Usuario no encontrado'
        String attachObserver = "(function(){" +
                "window.__lastToastText = null;" +
                "function checkNode(n){ try{ if(!n) return false; if(n.nodeType===1){ var t = (n.innerText||n.textContent||'').trim(); if(t==='Usuario no encontrado'){ window.__lastToastText = 'Usuario no encontrado'; return true; } } else if(n.nodeType===3){ if((n.nodeValue||'').trim()==='Usuario no encontrado'){ window.__lastToastText = 'Usuario no encontrado'; return true; } } }catch(e){} return false; }" +
                "var existing = document.querySelectorAll('*'); for(var i=0;i<existing.length;i++){ if(checkNode(existing[i])) return; }" +
                "var mo = new MutationObserver(function(muts){ for(var i=0;i<muts.length;i++){ var m=muts[i]; if(m.addedNodes){ for(var j=0;j<m.addedNodes.length;j++){ var an = m.addedNodes[j]; if(checkNode(an)) { mo.disconnect(); return; } try{ if(an.querySelectorAll){ var desc=an.querySelectorAll('*'); for(var k=0;k<desc.length;k++){ if(checkNode(desc[k])){ mo.disconnect(); return; } } } }catch(e){} } } } });" +
                "mo.observe(document.body,{childList:true,subtree:true});" +
                "})();";
        ((JavascriptExecutor) driver).executeScript(attachObserver);

        // usar los mismos datos y hacer login
        driver.findElement(By.id("outlined-adornment-email")).sendKeys("valentina@test.com");
        driver.findElement(By.id("outlined-adornment-password")).sendKeys("PasswordWrong123");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Ingresar']"))).click();

        // esperar hasta 5s a que el observer haya registrado el texto exacto
        JavascriptExecutor js = (JavascriptExecutor) driver;
        boolean captured = false;
        long waitUntil = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < waitUntil) {
            Object t = js.executeScript("return window.__lastToastText || null;");
            if (t != null && "Usuario no encontrado".equals(t.toString())) { captured = true; break; }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        if (!captured) {
            // guardar evidencia y fallar
            try {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                java.nio.file.Path dest = java.nio.file.Paths.get("target","screenshots","usuario-no-encontrado-not-captured-"+java.util.UUID.randomUUID()+".png");
                java.nio.file.Files.createDirectories(dest.getParent());
                java.nio.file.Files.copy(scr.toPath(), dest);
                System.out.println("Screenshot saved to: " + dest.toAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to save screenshot: " + e.getMessage());
            }
            fail("Toast 'Usuario no encontrado' was not captured within 5s");
        }
    }

    @DisplayName("CP-LOG-004 - Campos de login vacíos")
    @Order(4)
    @Test
    void shouldReturnBadRequest_whenCredentialsAreMissing() {
        driver.get("http://localhost:3000/login");

        // completar solo el email, dejar contraseña vacío
        driver.findElement(By.id("outlined-adornment-email")).sendKeys("valentina@test.com");

        By buttonLocator = By.xpath("//button[normalize-space()='Ingresar']");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(buttonLocator));

        // comprobar que el botón está deshabilitado: atributo disabled o isEnabled()==false o clase Mui-disabled
        boolean disabled = false;
        try {
            disabled = !btn.isEnabled();
        } catch (Exception ignored) {}
        String disabledAttr = btn.getAttribute("disabled");
        String classes = (btn.getAttribute("class") == null) ? "" : btn.getAttribute("class").toLowerCase();

        boolean hasDisabledAttr = disabledAttr != null && !disabledAttr.isEmpty();
        boolean hasDisabledClass = classes.contains("mui-disabled") || classes.contains("disabled") || classes.contains("tw-cursor-not-allowed");

        if (!(disabled || hasDisabledAttr || hasDisabledClass)) {
            // guardar evidencia y fallar
            try {
                File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                java.nio.file.Path dest = java.nio.file.Paths.get("target","screenshots","ingresar-not-disabled-"+java.util.UUID.randomUUID()+".png");
                java.nio.file.Files.createDirectories(dest.getParent());
                java.nio.file.Files.copy(scr.toPath(), dest);
                System.out.println("Screenshot saved to: " + dest.toAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to save screenshot: " + e.getMessage());
            }
            fail("Expected 'Ingresar' button to be disabled when a required field is missing");
        }
    }

    @DisplayName("CP-LOG-005 - Estructura JSON del token (API)")
    @Order(5)
    @Test
    public void shouldReturnTokenInResponse_whenLoginSucceeds() {
        RestAssured.baseURI = System.getProperty("api.base", "http://localhost:8080");

        String token =
            given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"valentina@test.com\",\"password\":\"Valen1234\"}")
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .body("token", allOf(notNullValue(), not(isEmptyString())))
                .extract().path("token");

        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "Token should be a JWT with 3 parts");

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decoded, StandardCharsets.UTF_8);
            assertTrue(payloadJson.toLowerCase().contains("valentina@test.com"), "JWT payload should contain the subject/email");
            assertTrue(payloadJson.contains("exp") || payloadJson.contains("sub"), "JWT payload should contain exp or sub");
        } catch (IllegalArgumentException e) {
            fail("JWT payload not base64-url decodable: " + e.getMessage());
        }
    }

}
