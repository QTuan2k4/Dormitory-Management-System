package com.group7.DMS.Epic7;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class U72 {

    WebDriver driver;


    String baseUrl = "http://localhost:8080/login";

    private By usernameInput = By.xpath("//input[@id='username']");
    private By passwordInput = By.xpath("//input[@id='password']");
    private By loginButton   = By.xpath("//form[@method='post']//button[@type='submit']");


    private By errorAlert    = By.xpath("//div[contains(@class,'alert') and contains(@class,'alert-danger')]");
   

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(baseUrl);
        Thread.sleep(1000);
    }

    // =====================================================================
    // U7.2_TC01 - Sai TK / sai MK / sai cả hai (tài khoản admin)
    // =====================================================================
    @DataProvider(name = "adminInvalidCreds")
    public Object[][] adminInvalidCreds() {
        return new Object[][]{
                {"admin1", "admin123", "TH1 - TK sai, MK đúng"},
                {"admin", "admin124", "TH2 - TK đúng, MK sai"},
                {"admin2", "sai-mk", "TH3 - TK và MK đều sai"}
        };
    }

    @Test(dataProvider = "adminInvalidCreds")
    public void TC_U7_2_01_invalidUsernameOrPassword_Admin(String username,
                                                           String password,
                                                           String scenario) throws Exception {
        driver.findElement(usernameInput).clear();
        driver.findElement(passwordInput).clear();

        driver.findElement(usernameInput).sendKeys(username);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();

        Thread.sleep(1000); // cho alert render

        WebElement alert = driver.findElement(errorAlert);
        String errorText = alert.getText().trim();
        System.out.println("TC01 - " + scenario + " - Error: " + errorText);

        // Theo AC: "Hiển thị lỗi: Tên đăng nhập hoặc mật khẩu không đúng"
        Assert.assertTrue(
                errorText.contains("Tên đăng nhập") || errorText.contains("mật khẩu"),
                "Không hiển thị thông báo lỗi phù hợp khi sai TK/MK (admin)"
        );

        String currentUrl = driver.getCurrentUrl();
        System.out.println("TC01 - After invalid login URL = " + currentUrl);

        // Không vào Dashboard → vẫn ở trang login (có thể /login hoặc /login?error=true)
        Assert.assertTrue(
                currentUrl.contains("/login"),
                "Sau khi sai TK/MK vẫn phải ở trang login (URL phải chứa /login)"
        );

        Assert.assertFalse(
                currentUrl.contains("/dashboard"),
                "Không được chuyển hướng vào Dashboard khi TK/MK sai (admin)"
        );
    }

    // =====================================================================
    // U7.2_TC02 - Không cho login khi bỏ trống dữ liệu
    // =====================================================================
    @Test
    public void TC_U7_2_02_emptyFields() throws Exception {
        // Bỏ trống username/password, bấm Login
        driver.findElement(loginButton).click();
        Thread.sleep(1000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("TC02 - URL = " + currentUrl);

        // AC: Form không submit, URL giữ nguyên /login
        Assert.assertEquals(
                currentUrl,
                baseUrl,
                "Khi để trống cả Username và Password thì form không được submit, URL phải giữ nguyên /login"
        );
    }

    // =====================================================================
    // U7.2_TC03 - Đăng nhập hợp lệ (admin/admin123) → Dashboard theo role
    // =====================================================================
    @Test
    public void TC_U7_2_03_loginSuccess_Generic() throws Exception {
        driver.findElement(usernameInput).sendKeys("admin");
        driver.findElement(passwordInput).sendKeys("admin123");
        driver.findElement(loginButton).click();

        Thread.sleep(2000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("TC03 - After login URL = " + currentUrl);

        boolean isDashboard =
                currentUrl.contains("/admin/dashboard") ||
                currentUrl.contains("/staff/dashboard") ||
                currentUrl.contains("/student/dashboard");

        Assert.assertTrue(
                isDashboard,
                "Đăng nhập hợp lệ phải chuyển đến Dashboard theo role (admin/staff/student)"
        );
    }

    // =====================================================================
    // U7.2_TC04 - Sai mã SV / sai MK / sai cả hai (tài khoản Sinh viên)
    // =====================================================================
    @DataProvider(name = "studentInvalidCreds")
    public Object[][] studentInvalidCreds() {
        return new Object[][]{
                {"4551050251", "vien123", "TH1 - Mã SV sai, MK đúng"},
                {"4551050256", "admin124", "TH2 - Mã SV đúng, MK sai"},
                {"4551050222", "sai-mk", "TH3 - Mã SV và MK đều sai"}
        };
    }

    @Test(dataProvider = "studentInvalidCreds")
    public void TC_U7_2_04_invalidStudentIdOrPassword(String studentId,
                                                      String password,
                                                      String scenario) throws Exception {
        driver.findElement(usernameInput).clear();
        driver.findElement(passwordInput).clear();

        driver.findElement(usernameInput).sendKeys(studentId);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();

        Thread.sleep(1000);

        WebElement alert = driver.findElement(errorAlert);
        String errorText = alert.getText().trim();
        System.out.println("TC04 - " + scenario + " - Error: " + errorText);

        Assert.assertTrue(
                errorText.contains("Tên đăng nhập") || errorText.contains("mật khẩu"),
                "Không hiển thị thông báo lỗi phù hợp khi sai mã SV/MK (student)"
        );

        String currentUrl = driver.getCurrentUrl();
        System.out.println("TC04 - After invalid login URL = " + currentUrl);

        Assert.assertTrue(
                currentUrl.contains("/login"),
                "Sau khi sai mã SV/MK vẫn phải ở trang login (URL phải chứa /login)"
        );

        Assert.assertFalse(
                currentUrl.contains("/dashboard"),
                "Không được chuyển hướng vào Dashboard khi mã SV/MK sai (student)"
        );
    }

    // =====================================================================
    // U7.2_TC06 - Đăng nhập hợp lệ với Sinh viên 4551050256/vien123
    // =====================================================================
    @Test
    public void TC_U7_2_06_loginSuccess_Student() throws Exception {
        driver.findElement(usernameInput).sendKeys("4551050256");
        driver.findElement(passwordInput).sendKeys("vien123");
        driver.findElement(loginButton).click();

        Thread.sleep(2000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("TC06 - After login URL = " + currentUrl);

        Assert.assertTrue(
                currentUrl.contains("/student/dashboard"),
                "Đăng nhập sinh viên phải chuyển đến /student/dashboard"
        );
    }

    // =====================================================================
    // U7.2_TC07 - Trim khoảng trắng đầu/cuối username (mã SV) trước khi xác thực
    // =====================================================================
    @Test
    public void TC_U7_2_07_trimmedUsername_Student() throws Exception {
  
        driver.findElement(usernameInput).sendKeys("   4551050256   ");
        driver.findElement(passwordInput).sendKeys("vien123");
        driver.findElement(loginButton).click();

        Thread.sleep(2000);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("TC07 - After login URL = " + currentUrl);

        Assert.assertTrue(
                currentUrl.contains("/student/dashboard"),
                "Hệ thống phải trim khoảng trắng username và cho đăng nhập sinh viên → /student/dashboard"
        );
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        if (driver != null) {
            driver.quit();
        }
    }
}
