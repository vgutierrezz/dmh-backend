import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class testLogin {
    public WebDriver driver;

    @Test
    public void test_1() {
        driver = new ChromeDriver();
        driver.get("http://localhost:3000/login");

        String title = driver.getTitle();
        System.out.println("Title of the page is: " + title);

        driver.findElement(By.id("outlined-adornment-email")).sendKeys("valentina@test.com");
        driver.findElement(By.id("outlined-adornment-password")).sendKeys("Valen1234");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement ingresar = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Ingresar']"))
        );
        ingresar.click();
    }
}
