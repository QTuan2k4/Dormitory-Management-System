package com.group7.DMS.Epic4;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class US42_CreateInvoiceTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

      
        driver.get("http://localhost:8080/DMS/login");

        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        //Vào trang tạo hóa đơn phòng C5-01
        driver.get("http://localhost:8080/DMS/admin/invoices/create?roomId=10");
    }

    @BeforeMethod
    public void beforeEachTest() {
        // Reload form trước mỗi TC
        driver.navigate().refresh();
    }

    // TC_9.2.01 – Tạo hóa đơn thành công
    @Test
    public void TC_9_2_01_createInvoiceSuccess() throws InterruptedException {

        selectMonthYear("3", "2026");
        inputUsage("150", "20");

        Thread.sleep(1500);

        clickSubmitSafely();

        // Redirect về danh sách hóa đơn
        wait.until(ExpectedConditions.urlContains("/admin/invoices"));

        Assert.assertTrue(
                driver.getCurrentUrl().contains("/admin/invoices"),
                "Không chuyển về trang danh sách hóa đơn"
        );

        Thread.sleep(2000);
    }

    // TC_9.2.02 – Trùng hóa đơn cùng tháng/năm
    @Test
    public void TC_9_2_02_createInvoiceDuplicateFail() throws InterruptedException {

        selectMonthYear("1", "2026");
        inputUsage("150", "20");

        Thread.sleep(1500);

        clickSubmitSafely();

        WebElement errorAlert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger"))
        );

        Assert.assertTrue(
                errorAlert.getText().contains("Đã tồn tại hóa đơn"),
                "Không hiển thị lỗi trùng hóa đơn"
        );

        Assert.assertTrue(
                driver.getCurrentUrl().contains("/admin/invoices/create"),
                "Không ở lại trang Tạo Hóa Đơn"
        );

        Thread.sleep(2000);
    }

    // TC_9.2.03 – Nhập dữ liệu không hợp lệ
    @Test
    public void TC_9_2_03_createInvoiceInvalidInput() throws InterruptedException {

        selectMonthYear("1", "2026");

        WebElement electricity = driver.findElement(By.id("electricityUsage"));
        electricity.clear();
        electricity.sendKeys("-150");

        WebElement water = driver.findElement(By.id("waterUsage"));
        water.clear();
        water.sendKeys("abc");

        Thread.sleep(1000);

        clickSubmitSafely();

        //URL không đổi → form không submit
        Assert.assertTrue(
                driver.getCurrentUrl().contains("/admin/invoices/create"),
                " Form bị submit dù dữ liệu không hợp lệ"
        );

        // HTML5 validation
        Boolean electricityValid = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return document.getElementById('electricityUsage').checkValidity();");

        Boolean waterValid = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return document.getElementById('waterUsage').checkValidity();");

        Assert.assertFalse(electricityValid, "❌ Số điện âm vẫn hợp lệ");
        Assert.assertFalse(waterValid, "❌ Số nước nhập chữ vẫn hợp lệ");

        Thread.sleep(1500);
    }

    // HÀM DÙNG CHUNG
    private void selectMonthYear(String month, String year) {
        new Select(driver.findElement(By.id("monthSelect"))).selectByValue(month);
        new Select(driver.findElement(By.id("yearSelect"))).selectByValue(year);
    }

    private void inputUsage(String electricity, String water) {
        WebElement electricityInput = driver.findElement(By.id("electricityUsage"));
        electricityInput.clear();
        electricityInput.sendKeys(electricity);

        WebElement waterInput = driver.findElement(By.id("waterUsage"));
        waterInput.clear();
        waterInput.sendKeys(water);
    }
    private void clickSubmitSafely() {
        WebElement submitBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type='submit']"))
        );

        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", submitBtn);

        wait.until(ExpectedConditions.elementToBeClickable(submitBtn));

        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", submitBtn);
    }


    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
