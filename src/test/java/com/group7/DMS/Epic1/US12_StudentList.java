package com.group7.DMS.Epic1;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class US12_StudentList {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Truy cập trang login
        driver.get("http://localhost:8080/DMS/login");
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) driver.quit();
    }

    private void type(By locator, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).sendKeys(text);
    }

    private void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); window.scrollBy(0, -100);", element);
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // ===== Locators =====
    By usernameInput = By.id("username");
    By passwordInput = By.id("password");
    By loginBtn = By.cssSelector("button.btn-login");
    By studentMenu = By.xpath("//a[contains(@href,'/admin/students')]"); // link student

    @Test
    public void TC01_DisplayStudentList() {
        // Đăng nhập
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // Click vào menu "Quản lý sinh viên"
        click(studentMenu);

        // Chờ URL thay đổi
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // Kiểm tra URL
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/admin/students"), "Chưa chuyển tới trang danh sách sinh viên!");
        System.out.println("Login thành công, trang danh sách sinh viên hiển thị.");
    }
    
    @Test
    public void TC02_StudentListPaginationNext() throws InterruptedException {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Truy cập trực tiếp trang danh sách sinh viên
        driver.get("http://localhost:8080/DMS/admin/students");

        // 3️⃣ Chờ bảng student load
        By studentRows = By.cssSelector("table tbody tr");
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));

        // 4️⃣ Lấy số trang hiện tại
        By activePage = By.cssSelector(".pagination .page-item.active a");
        String page1 = wait.until(ExpectedConditions.visibilityOfElementLocated(activePage)).getText();

        // 5️⃣ Click nút "Sau" (Next)
        By nextBtn = By.xpath("//a[contains(text(),'Sau') or contains(@aria-label,'Next')]");
        WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(nextBtn));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
        Thread.sleep(200); 
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);

        // 6️⃣ Chờ page active thay đổi
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(activePage, page1)));

        // ✅ Test thành công
        System.out.println("TC02: Click nút 'Sau' chuyển sang trang 2 thành công. Trang trước: " + page1 +
                           ", Trang hiện tại: " + wait.until(ExpectedConditions.visibilityOfElementLocated(activePage)).getText());
    }


}
