package com.group7.DMS.Epic3;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class US32_RoomFormTest {

    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;

        driver.manage().window().maximize();

        driver.get("http://localhost:8080/DMS/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    /* HELPER */

    public void openCreateRoomForm() {
        driver.get("http://localhost:8080/DMS/admin/rooms");

        WebElement btnCreate = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[2]/div[2]/div/div/div[2]/div[1]/a")
                )
        );

        btnCreate.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//form")
        ));
    }

    public void fillRequiredFields() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("roomNumber")));

        driver.findElement(By.name("building")).sendKeys("C5");
        driver.findElement(By.name("roomNumber")).sendKeys("23");
        driver.findElement(By.name("floor")).sendKeys("2");
        driver.findElement(By.name("capacity")).sendKeys("4");
        driver.findElement(By.name("area")).sendKeys("50");
        driver.findElement(By.name("price")).sendKeys("30000000");

        driver.findElement(By.name("status")).sendKeys("Còn chỗ");
    }

    public void clickByJS(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        js.executeScript("arguments[0].click();", element);
    }

    /* TEST CASES  */

    // TC_3.2.03
    @Test
    public void TC_3_2_03_addRoomSuccessfully() {
        openCreateRoomForm();
        fillRequiredFields();

        WebElement btnSave = driver.findElement(By.cssSelector("button[type='submit']"));
        clickByJS(btnSave);

        WebElement msg = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(),'Thêm phòng')]")
                )
        );

        Assert.assertTrue(msg.getText().toLowerCase().contains("thành công"));
    }

    // TC_3.2.04
    @Test
    public void TC_3_2_04_cannotSaveWhenMissingRequiredField() {
        openCreateRoomForm();

        WebElement btnSave = driver.findElement(By.cssSelector("button[type='submit']"));
        clickByJS(btnSave);

        WebElement roomNumber = driver.findElement(By.name("roomNumber"));
        String validationMsg = roomNumber.getAttribute("validationMessage");

        Assert.assertTrue(validationMsg != null && !validationMsg.isEmpty());
    }

    // TC_3.2.06
    @Test
    public void TC_3_2_06_cancelCreateRoom() {
        openCreateRoomForm();

        WebElement btnCancel = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.linkText("Hủy")
                )
        );

        clickByJS(btnCancel);

        wait.until(ExpectedConditions.urlContains("/admin/rooms"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/rooms"));
    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }
}
