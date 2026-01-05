package com.group7.DMS.Epic4;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class US45_EditInvoiceTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        loginAsAdmin();
    }

    //LOGIN 
    public void loginAsAdmin() {
        driver.get("http://localhost:8080/DMS/login");
        driver.findElement(By.id("username")).sendKeys("1");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    //  TC_9.5.01 
    @Test
    public void TC_9_5_01_editInvoiceSuccess_unpaid() {

        driver.get("http://localhost:8080/DMS/admin/invoices");

        // Click nút Chỉnh sửa
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@href,'/edit')]")
        ));
        driver.findElement(By.xpath("//a[contains(@href,'/edit')]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editForm")));

        WebElement electricity = driver.findElement(By.id("electricityUsage"));
        WebElement water = driver.findElement(By.id("waterUsage"));

        electricity.clear();
        electricity.sendKeys("180");

        water.clear();
        water.sendKeys("25");

        String totalText = driver.findElement(By.id("totalAmount")).getText();
        Assert.assertTrue(totalText.contains("VNĐ"),
                "❌ Không hiển thị tổng tiền mới");

        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", submitBtn);

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", submitBtn);

        wait.until(ExpectedConditions.urlContains("/admin/invoices"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/invoices"),
                " Không quay về danh sách hóa đơn");
    }

    //TC_9.5.02
    @Test
    public void TC_9_5_02_cannotEditPaidInvoice() {

        driver.get("http://localhost:8080/DMS/admin/invoices");

        // Hóa đơn đã thanh toán → KHÔNG có nút chỉnh sửa
        List<WebElement> editButtons = driver.findElements(
                By.xpath("//tr[td[contains(text(),'Đã thanh toán')]]//a[contains(@href,'/edit')]")
        );

        Assert.assertEquals(editButtons.size(), 0,
                " Hóa đơn đã thanh toán vẫn hiển thị nút chỉnh sửa");
    }

    // TC_9.5.03 
    @Test
    public void TC_9_5_03_fixedFieldsCannotEdit() {

        driver.get("http://localhost:8080/DMS/admin/invoices");

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@href,'/edit')]")
        ));
        driver.findElement(By.xpath("//a[contains(@href,'/edit')]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("editForm")));

        // Phí internet (readonly)
        WebElement internetFee = driver.findElement(
                By.xpath("//input[contains(@value,'không đổi')]")
        );

        Assert.assertNotNull(
                internetFee.getAttribute("readonly"),
                "Phí Internet KHÔNG readonly"
        );

        // Điện + nước cho phép sửa
        Assert.assertTrue(driver.findElement(By.id("electricityUsage")).isEnabled());
        Assert.assertTrue(driver.findElement(By.id("waterUsage")).isEnabled());
    }


    @AfterMethod
    public void tearDown() throws InterruptedException {
    	Thread.sleep(2000);
        driver.quit();
    }
}
