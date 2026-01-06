package com.group7.DMS.Epic8;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class U85 {

    WebDriver driver;
    WebDriverWait wait;

    String BASE_URL = "http://localhost:8080/DMS"; // Update theo log của bạn
    String LOGIN_URL = "http://localhost:8080/login"; // Update nếu cần
    String CONTRACT_URL = BASE_URL + "/admin/contracts";

    // LOCATORS
    By expiredContractsTab = By.xpath("/html/body/div[2]/div[2]/div[1]/a");
    By terminateAllBtn = By.xpath("/html/body/div[2]/div[2]/form/button");

    @BeforeClass
    public void setup() {

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("http://localhost:8080/DMS/login");
        
        driver.findElement(By.name("username")).sendKeys("4551050111");
        driver.findElement(By.name("password")).sendKeys("thao123");
        driver.findElement(By.xpath("/html/body/div/form/button")).click();
        
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    @BeforeMethod
    public void navigateToPage() {
        // --- QUAN TRỌNG: Xử lý Popup còn sót lại từ test trước ---
        try {
            Alert alert = driver.switchTo().alert();
            System.out.println("⚠️ Phát hiện Popup còn treo từ test trước -> Đóng lại.");
            alert.accept();
        } catch (NoAlertPresentException e) {
            // Không có popup thì thôi, chạy tiếp bình thường
        }

        // Sau đó mới chuyển trang
        driver.get(CONTRACT_URL);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test(priority = 1)
    public void TC_8_5_18_BatchTerminateSuccess() {
        System.out.println("--- Running TC 8.5.18: Batch Terminate Success ---");

        try {
            WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(expiredContractsTab));
            tab.click();
        } catch (Exception e) {
            System.out.println("❌ Không tìm thấy tab Hết hạn. Web có thể đang lỗi layout.");
        }

        List<WebElement> activeRows = driver.findElements(By.xpath("//td[contains(text(), 'Active')]"));
        System.out.println("Số HĐ Active trước khi chạy: " + activeRows.size());

        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(terminateAllBtn));
            btn.click();
        } catch (Exception e) {
            Assert.fail("❌ Không tìm thấy nút 'Terminated tất cả'. Check lại XPath!");
        }

        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            System.out.println("Nội dung Popup thực tế: " + alertText);

            boolean isTextCorrect = alertText.contains("Terminate toàn bộ") || alertText.contains("hết hạn");
            alert.accept(); 
            
            Assert.assertTrue(isTextCorrect, "Nội dung popup không đúng! Thực tế là: " + alertText);

        } catch (TimeoutException e) {
            Assert.fail("Popup xác nhận không hiện ra!");
        }

        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        List<WebElement> remainingActive = driver.findElements(By.xpath("//td[contains(text(), 'Active')]"));
     
        if (activeRows.size() > 0) {
            Assert.assertTrue(remainingActive.size() < activeRows.size(), "Số lượng hợp đồng Active không giảm đi!");
        }
    }

    @Test(priority = 2)
    public void TC_8_5_19_BatchTerminateCancel() {
        System.out.println("--- Running TC 8.5.19: Batch Terminate Cancel ---");

        wait.until(ExpectedConditions.elementToBeClickable(expiredContractsTab)).click();

        int countBefore = driver.findElements(By.xpath("//td[contains(text(), 'Active')]")).size();

        wait.until(ExpectedConditions.elementToBeClickable(terminateAllBtn)).click();

        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Popup hiện ra, nhấn Cancel.");
            alert.dismiss(); 
        } catch (TimeoutException e) {
            Assert.fail("Popup không hiện ra!");
        }

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        int countAfter = driver.findElements(By.xpath("//td[contains(text(), 'Active')]")).size();
        Assert.assertEquals(countAfter, countBefore, "Số lượng hợp đồng thay đổi dù đã Cancel!");

        String body = driver.findElement(By.tagName("body")).getText();
        Assert.assertFalse(body.contains("Thành công") || body.contains("Success"), "Vẫn hiện thông báo thành công khi Cancel!");
    }
}