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

public class US46_InvoiceDetailTest {

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

    // LOGIN 
    public void loginAsAdmin() {
        driver.get("http://localhost:8080/DMS/login");
        driver.findElement(By.id("username")).sendKeys("1");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    // TC_9.6.01 
    @Test
    public void TC_9_6_01_markInvoicePaidSuccess() {

        driver.get("http://localhost:8080/DMS/admin/invoices");

        // 1. Click Xem hóa đơn CHƯA TT (scroll + wait clickable)
        WebElement viewBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//tr[td//span[normalize-space()='Chưa TT']]//a[contains(@href,'/admin/invoices/') and not(contains(@href,'edit'))]")
                )
        );
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", viewBtn);
        wait.until(ExpectedConditions.elementToBeClickable(viewBtn)).click();

        // 2. Click Đánh dấu Đã TT
        WebElement payBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Đánh dấu Đã TT']")
        ));
        payBtn.click();

        // 3. Accept confirm
        driver.switchTo().alert().accept();

        // 4. Verify trạng thái
        WebElement paidStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[contains(text(),'ĐÃ THANH TOÁN')]")
        ));
        Assert.assertTrue(paidStatus.isDisplayed());

        // 5. Verify không chỉnh sửa
        Assert.assertEquals(
                driver.findElements(By.xpath("//a[normalize-space()='Chỉnh sửa']")).size(),
                0
        );

        // 6. Quay lại danh sách (safe click)
        WebElement backBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.linkText("Quay lại danh sách")
        ));
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", backBtn);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", backBtn);

       
    }

    // TC_9.6.02 
    @Test
    public void TC_9_6_02_cannotRevertPaidInvoice() {

        driver.get("http://localhost:8080/DMS/admin/invoices");

        // Click Xem hóa đơn ĐÃ TT
        WebElement viewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//tr[td//span[normalize-space()='Đã TT']]//a[contains(@href,'/admin/invoices/') and not(contains(@href,'edit'))]")
        ));
        viewBtn.click();

        // Không có nút Đánh dấu
        Assert.assertEquals(
                driver.findElements(By.xpath("//button[contains(text(),'Đánh dấu')]")).size(),
                0
        );

        // Không có chỉnh sửa
        Assert.assertEquals(
                driver.findElements(By.xpath("//a[normalize-space()='Chỉnh sửa']")).size(),
                0
        );

        // Trạng thái vẫn ĐÃ THANH TOÁN
        WebElement status = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[contains(text(),'ĐÃ THANH TOÁN')]")
        ));
        Assert.assertTrue(status.isDisplayed());
    }

}
