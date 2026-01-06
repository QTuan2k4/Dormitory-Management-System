package com.group7.DMS.Epic4;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;

public class U43_TC13 {

    WebDriver driver;

    String BASE_URL  = "http://localhost:8080";
    String LOGIN_URL = BASE_URL + "/login";
    String LIST_URL  = BASE_URL + "/admin/invoices";

    String ADMIN_USER = "admin";
    String ADMIN_PASS = "admin123";

    int WAIT = 1200;
    int VIEW_WAIT = 5000; // chờ 5s để nhìn

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        driver.get(LOGIN_URL);
        Thread.sleep(800);

        driver.findElement(By.id("username")).sendKeys(ADMIN_USER);
        driver.findElement(By.id("password")).sendKeys(ADMIN_PASS);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(WAIT);

        driver.get(LIST_URL);
        Thread.sleep(WAIT);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Thread.sleep(VIEW_WAIT);
        if (driver != null) driver.quit();
    }

    // click an toàn: scroll -> click; nếu bị chặn thì JS click
    private void clickSafeByXpath(String xpath) throws Exception {
        WebElement el = driver.findElement(By.xpath(xpath));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        Thread.sleep(300);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
        Thread.sleep(WAIT);
    }

    private String firstRowText() {
        return driver.findElement(By.xpath("(//table//tbody//tr)[1]")).getText().trim();
    }

    @Test
    public void U4_3_TC13_Page1_Page2_Page1() throws Exception {
        // đảm bảo có data
        Assert.assertTrue(driver.findElements(By.xpath("//table//tbody//tr")).size() > 0, "Danh sách hóa đơn trống.");

        String rowPage1 = firstRowText();

        // Click trang 2 (bắt cả a/button)
        String PAGE_2 = "(//ul[contains(@class,'pagination')]//*[self::a or self::button][normalize-space(.)='2'])[1]"
                      + " | (//*[self::a or self::button][normalize-space(.)='2'])[1]";
        clickSafeByXpath(PAGE_2);

        String rowPage2 = firstRowText();
        Assert.assertNotEquals(rowPage2, rowPage1, "Chuyển sang trang 2 nhưng dữ liệu không đổi.");
        Thread.sleep(VIEW_WAIT); // xem trang 2

        // Click lại trang 1
        String PAGE_1 = "(//ul[contains(@class,'pagination')]//*[self::a or self::button][normalize-space(.)='1'])[1]"
                      + " | (//*[self::a or self::button][normalize-space(.)='1'])[1]";
        clickSafeByXpath(PAGE_1);

        String rowBack1 = firstRowText();
        Assert.assertEquals(rowBack1, rowPage1, "Quay lại trang 1 nhưng dữ liệu không khớp ban đầu.");
        Thread.sleep(VIEW_WAIT); // xem trang 1
    }
}
