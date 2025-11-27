package com.group7.DMS.Epic6;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;


public class US61_DormRegistration {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(priority = 0)
    public void login() {
        driver.get("http://localhost:8080/DMS/login");

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameInput.clear();
        usernameInput.sendKeys("4551050188");

        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passwordInput.clear();
        passwordInput.sendKeys("hao123");

        WebElement btnLogin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("form button[type='submit']")));
        btnLogin.click();
    }
    private void openRegisterPage() {
        driver.get("http://localhost:8080/DMS/student/register-personal-info");
    }

    @Test(priority = 1)
    public void registerDorm() {
    	 openRegisterPage();
        // Điền form cơ bản
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("birthDate"))).sendKeys("09/09/2007");
        driver.findElement(By.name("address")).sendKeys("Quy Nhơn");
        driver.findElement(By.name("gender")).sendKeys("nữ");
        driver.findElement(By.name("citizenId")).sendKeys("098767876567");

        // Chọn tầng
        WebElement floorSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("floorSelect")));
        floorSelect.sendKeys("Tầng 2: 8.000.000 VNĐ/học kỳ");

        // Scroll xuống cuối trang để các input file hiển thị
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Upload file
        WebElement file1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardFront")));
        file1.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        WebElement file2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardBack")));
        file2.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[3]/button")));
        submitBtn.click();
    }

    @Test(priority = 2)
    public void testMissingFloor() {
        openRegisterPage();

        driver.findElement(By.name("birthDate")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).sendKeys("Quy Nhơn");
        driver.findElement(By.name("gender")).sendKeys("Nam");
        driver.findElement(By.name("citizenId")).sendKeys("123456789012");

        // KHÔNG chọn tầng
        driver.findElement(By.id("floorSelect")).sendKeys("");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Upload file
        WebElement file1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardFront")));
        file1.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        WebElement file2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardBack")));
        file2.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[3]/button")));
        submitBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("floorError")));
        Assert.assertEquals(error.getText(), "Vui lòng chọn tầng");
    }

    // ---------------- 2. BỎ TRỐNG HỘ KHẨU ------------------
    @Test(priority = 3)
    public void testMissingAddress() {
        openRegisterPage();

        driver.findElement(By.name("birthDate")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).clear(); // bỏ trống hộ khẩu
        driver.findElement(By.name("gender")).sendKeys("Nam");
        driver.findElement(By.name("citizenId")).sendKeys("123456789012");
        driver.findElement(By.id("floorSelect")).sendKeys("Tầng 2");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Upload file
        WebElement file1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardFront")));
        file1.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        WebElement file2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardBack")));
        file2.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[3]/button")));
        submitBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addressError")));
        Assert.assertEquals(error.getText(), "Vui lòng nhập hộ khẩu thường trú");
    }

    // ---------------- 3. BỎ TRỐNG GIỚI TÍNH ------------------
    @Test(priority = 4)
    public void testMissingGender() {
        openRegisterPage();

        driver.findElement(By.name("birthDate")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).sendKeys("Quy Nhơn");
        // KHÔNG nhập giới tính
        driver.findElement(By.name("citizenId")).sendKeys("123456789012");
        driver.findElement(By.id("floorSelect")).sendKeys("Tầng 2");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Upload file
        WebElement file1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardFront")));
        file1.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        WebElement file2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardBack")));
        file2.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[3]/button")));
        submitBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("genderError")));
        Assert.assertEquals(error.getText(), "Vui lòng chọn giới tính");
    }

    // ---------------- 4. CCCD NGẮN (SAI ĐỊNH DẠNG) ------------------
    @Test(priority = 5)
    public void testInvalidCCCDShort() {
        openRegisterPage();

        driver.findElement(By.name("birthDate")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).sendKeys("Quy Nhơn");
        driver.findElement(By.name("gender")).sendKeys("Nam");
        driver.findElement(By.name("citizenId")).sendKeys("12345"); // NGẮN
        driver.findElement(By.id("floorSelect")).sendKeys("Tầng 2");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Upload file
        WebElement file1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardFront")));
        file1.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        WebElement file2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardBack")));
        file2.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[3]/button")));
        submitBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cccdError")));
        Assert.assertEquals(error.getText(), "Căn cước công dân phải có 12 số");
    }

    // ---------------- 5. CCCD DÀI (SAI ĐỊNH DẠNG) ------------------
    @Test(priority = 6)
    public void testInvalidCCCDLong() {
        openRegisterPage();

        driver.findElement(By.name("birthDate")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).sendKeys("Quy Nhơn");
        driver.findElement(By.name("gender")).sendKeys("Nam");
        driver.findElement(By.name("citizenId")).sendKeys("1234567890123456"); // QUÁ DÀI
        driver.findElement(By.id("floorSelect")).sendKeys("Tầng 2");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Upload file
        WebElement file1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardFront")));
        file1.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        WebElement file2 = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("idCardBack")));
        file2.sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[3]/button")));
        submitBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cccdError")));
        Assert.assertEquals(error.getText(), "Căn cước công dân phải có 12 số");
    }

    // ---------------- 6. UPLOAD FILE ĐÚNG ĐUÔI .PNG ------------------
    @Test(priority = 7)
    public void testUploadCCCDValid() {
        openRegisterPage();

        driver.findElement(By.name("birthDate")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).sendKeys("Quy Nhơn");
        driver.findElement(By.name("gender")).sendKeys("Nam");
        driver.findElement(By.name("citizenId")).sendKeys("123456789012");
        driver.findElement(By.id("floorSelect")).sendKeys("Tầng 2");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        driver.findElement(By.id("idCardFront")).sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");
        driver.findElement(By.id("idCardBack")).sendKeys("C:\\Users\\Thao\\Pictures\\Screenshots\\test61.png");

        driver.findElement(By.id("submitBtn")).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uploadSuccess")));
        Assert.assertEquals(msg.getText(), "Tải lên thành công");
    }

    // ---------------- 7. UPLOAD FILE SAI ĐUÔI ------------------
    @Test(priority = 8)
    public void testUploadCCCDInvalidFormat() {
        openRegisterPage();

        driver.findElement(By.name("birthDate")).sendKeys("01/01/2000");
        driver.findElement(By.name("address")).sendKeys("Quy Nhơn");
        driver.findElement(By.name("gender")).sendKeys("Nam");
        driver.findElement(By.name("citizenId")).sendKeys("123456789012");
        driver.findElement(By.id("floorSelect")).sendKeys("Tầng 2");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        driver.findElement(By.id("idCardFront")).sendKeys("Pictures\\test61(2).jpg");
        driver.findElement(By.id("idCardBack")).sendKeys("Pictures\\\\test61(2).jpg");
        driver.findElement(By.id("submitBtn")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileError")));
        Assert.assertEquals(error.getText(), "File phải có định dạng .png");
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}
