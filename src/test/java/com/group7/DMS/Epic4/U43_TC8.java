package com.group7.DMS.Epic4;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.util.List;

public class U43_TC8 {

    WebDriver driver;

    String BASE_URL  = "http://localhost:8080";
    String LOGIN_URL = BASE_URL + "/login";
    String LIST_URL  = BASE_URL + "/admin/invoices";

    String ADMIN_USER = "admin";
    String ADMIN_PASS = "admin123";

    int WAIT = 1200;      // chờ load bình thường
    int VIEW_WAIT = 5000; // chờ 5s để xem kết quả

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
        Thread.sleep(VIEW_WAIT); // chờ 5s trước khi đóng để mày nhìn
        if (driver != null) driver.quit();
    }

    private WebElement first(String xpath) {
        List<WebElement> els = driver.findElements(By.xpath(xpath));
        return els.isEmpty() ? null : els.get(0);
    }

    private void backToList() throws Exception {
        WebElement back = first("//a[contains(@href,'/admin/invoices') and not(contains(@href,'/admin/invoices/'))][1]");
        if (back != null) back.click();
        else driver.get(LIST_URL);
        Thread.sleep(WAIT);
    }

    @Test
    public void U4_3_TC8_AdminMarkPaid() throws Exception {

        List<WebElement> rows = driver.findElements(By.xpath("//table//tbody//tr"));
        Assert.assertTrue(rows.size() > 0, "Danh sách hóa đơn trống.");

        boolean done = false;
        int limit = Math.min(rows.size(), 10); // duyệt 10 dòng đầu cho đơn giản

        for (int i = 1; i <= limit; i++) {

            // mở chi tiết từ link trong dòng
            WebElement detailLink = first("(//table//tbody//tr)[" + i + "]//a[contains(@href,'/admin/invoices/')][1]");
            if (detailLink == null) continue;

            detailLink.click();
            Thread.sleep(WAIT);

            // nếu có nút "Đánh dấu Đã TT" => hóa đơn chưa thanh toán
            WebElement markPaidBtn = first(
                    "//*[self::button or self::a][contains(.,'Đánh dấu') or contains(.,'Đã TT') or contains(.,'Đã thanh toán') or contains(.,'Mark')][1]"
            );

            if (markPaidBtn == null) {
                backToList();
                continue;
            }

            // bấm đánh dấu đã thanh toán
            markPaidBtn.click();
            Thread.sleep(500);

            // confirm browser alert
            try {
                Alert alert = driver.switchTo().alert();
                System.out.println("ALERT: " + alert.getText());
                alert.accept();
            } catch (NoAlertPresentException e) {
                // nếu không có alert thì bỏ qua
            }

            Thread.sleep(WAIT);

            // verify đơn giản: có chữ "ĐÃ THANH TOÁN" hoặc nút đánh dấu biến mất
            WebElement paidText = first("//*[contains(.,'ĐÃ THANH TOÁN') or contains(.,'Đã thanh toán') or contains(.,'DA THANH TOAN')]");
            WebElement stillMark = first("//*[self::button or self::a][contains(.,'Đánh dấu') or contains(.,'Đã TT') or contains(.,'Mark')][1]");

            Assert.assertTrue(paidText != null || stillMark == null,
                    "Không thấy trạng thái ĐÃ THANH TOÁN sau khi xác nhận.");

            // chờ 5s để mày nhìn kết quả trên màn hình chi tiết
            Thread.sleep(VIEW_WAIT);

            done = true;
            break;
        }

        if (!done) {
            throw new SkipException("Không tìm thấy hóa đơn có nút 'Đánh dấu Đã TT' trong " + limit + " dòng đầu.");
        }
    }
}

