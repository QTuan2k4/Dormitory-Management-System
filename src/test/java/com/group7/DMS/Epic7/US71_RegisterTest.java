package com.group7.DMS.Epic7;

import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
public class US71_RegisterTest {
	 WebDriver driver;
	    WebDriverWait wait;

	    @BeforeMethod
	    public void setup() {
	        WebDriverManager.chromedriver().setup();
	        driver = new ChromeDriver();
	        driver.manage().window().maximize();

	        // Truy cập trang đăng ký
	        driver.get("http://localhost:8080/DMS/register");  
	        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	    }

	    @AfterMethod
	    public void teardown() {
	        if (driver != null) driver.quit();
	    }

	    // Hàm nhập nhanh
	    private void type(By locator, String text) {
	        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).sendKeys(text);
	    }

	 // Hàm click nâng cao
	    private void click(By locator) {
	        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

	        // Scroll vào view và kéo lên 100px để tránh bị header cố định che
	        ((JavascriptExecutor) driver).executeScript(
	            "arguments[0].scrollIntoView(true); window.scrollBy(0, -100);", element);

	        // Chờ thêm một chút cho animation hoặc overlay load xong
	        try { Thread.sleep(200); } catch (InterruptedException e) { }

	        // Click bằng JS trực tiếp để chắc chắn click thành công
	        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
	    }


	    private String getText(By locator) {
	        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
	    }

	    // ====== Locators ======
	    By fullName = By.id("fullName");
	    By studentId = By.id("studentId");
	    By course = By.id("course");
	    By major = By.id("major");
	    By email = By.id("email");
	    By password = By.id("password");
	    By confirmPassword = By.id("confirmPassword");
	    By registerBtn = By.xpath("//button[@type='submit']");
	    By successMessage = By.cssSelector(".alert-success");
	    By errorMessage = By.cssSelector(".alert.alert-danger");

	    // ==================== TEST CASES ======================

	    @Test
	    public void TC02_RegisterSuccess() throws InterruptedException {
	        type(fullName, "Nguyễn Tú Quyên");
	        type(studentId, "4551050177");
	        type(course, "45");
	        type(major, "CNTT");
	        type(email, "test" + System.currentTimeMillis() + "@gmail.com");
	        type(password, "123456");
	        type(confirmPassword, "123456");

	        click(registerBtn);

	        System.out.println("TC02 Message: " + getText(successMessage));
	    }

	    @Test
	    public void TC03_MissingFullName() throws InterruptedException {
	        type(studentId, "4551050177");
	        type(course, "45");
	        type(major, "CNTT");
	        type(email, "test" + System.currentTimeMillis() + "@gmail.com");
	        type(password, "123456");
	        type(confirmPassword, "123456");

	        click(registerBtn);

	        String error = getText(errorMessage);

	        Assert.assertFalse(error.isEmpty(), "Không hiển thị lỗi khi thiếu họ tên");
	        System.out.println("TC03 PASSED");
	    }

	    @Test
	    public void TC04_InvalidStudentId() throws InterruptedException {
	        type(studentId, "abc@");
	        type(fullName, "Nguyễn Tú Quyên");
	        type(course, "45");
	        type(major, "CNTT");
	        type(email, "test" + System.currentTimeMillis() + "@gmail.com");
	        type(password, "123456");
	        type(confirmPassword, "123456");

	        click(registerBtn);

	        String error = getText(errorMessage);
	        Assert.assertFalse(error.isEmpty(), "Không hiển thị lỗi khi mã SV sai định dạng");
	        System.out.println("TC04 PASSED");
	    }

	    @Test
	    public void TC06_PasswordTooShort() throws InterruptedException {
	        type(fullName, "Nguyễn Tú Quyên");
	        type(studentId, "4551050177");
	        type(course, "45");
	        type(major, "CNTT");
	        type(email, "test" + System.currentTimeMillis() + "@gmail.com");

	        type(password, "111");  
	        type(confirmPassword, "111");

	        click(registerBtn);

	        String error = getText(errorMessage);
	        Assert.assertFalse(error.isEmpty(), "Không hiển thị lỗi khi mật khẩu quá ngắn");
	        System.out.println("TC06 PASSED");
	    }

	    @Test
	    public void TC08_StudentIdAlreadyExists() throws InterruptedException {
	        type(fullName, "Nguyễn Tú Quyên");
	        type(studentId, "4551050175"); // tồn tại
	        type(course, "K45");
	        type(major, "CNTT");
	        type(email, "test" + System.currentTimeMillis() + "@gmail.com");
	        type(password, "123456");
	        type(confirmPassword, "123456");

	        click(registerBtn);

	        String error = getText(errorMessage);
	        Assert.assertFalse(error.isEmpty(), "Không hiển thị lỗi");
	        System.out.println("TC08 PASSED");
	    }

	    @Test
	    public void TC09_EmailAlreadyExists() throws InterruptedException {
	        type(fullName, "Nguyễn Tú Quyên");
	        type(studentId, "4551050177");
	        type(course, "K45");
	        type(major, "CNTT");
	        type(email, "abc@gmail.com"); // tồn tại
	        type(password, "123456");
	        type(confirmPassword, "123456");

	        click(registerBtn);

	        String error = getText(errorMessage);
	        Assert.assertFalse(error.isEmpty(), "Không hiển thị lỗi email trùng");
	        System.out.println("TC09 PASSED");
	    }
}
