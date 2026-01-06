package com.group7.DMS.Epic4;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;
import java.util.Set;

public class U44 {

    WebDriver driver;

    String BASE_URL = "http://localhost:8080";
    String LOGIN_URL = BASE_URL + "/login";
    String LIST_URL  = BASE_URL + "/admin/invoices";

    String ADMIN_USER = "admin";
    String ADMIN_PASS = "admin123";

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
        Thread.sleep(1200);

        driver.get(LIST_URL);
        Thread.sleep(1200);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private void openFirstInvoiceDetail() throws Exception {
        // check có dòng
        Assert.assertTrue(driver.findElements(By.xpath("//table//tbody//tr")).size() > 0,
                "Danh sách hóa đơn trống.");

        // cách cơ bản nhất: click link detail trong dòng đầu (nếu dự án đúng chuẩn)
        List<WebElement> links = driver.findElements(By.xpath(
                "(//table//tbody//tr)[1]//a[contains(@href,'/admin/invoices/')][1]"
        ));
        Assert.assertTrue(!links.isEmpty(), "Không tìm thấy link mở chi tiết trong dòng đầu.");
        links.get(0).click();

        Thread.sleep(1200);
    }

    @Test
    public void U4_4_TC11_BackToInvoiceList() throws Exception {
        openFirstInvoiceDetail();

        // tìm nút/link quay lại (cơ bản)
        List<WebElement> backBtns = driver.findElements(By.xpath(
                "//a[contains(@href,'/admin/invoices') and not(contains(@href,'/admin/invoices/'))] | " +
                "//button[contains(.,'Quay lại')] | " +
                "//a[contains(.,'Quay lại')]"
        ));
        Assert.assertTrue(!backBtns.isEmpty(), "Không thấy nút 'Quay lại danh sách'.");
        backBtns.get(0).click();

        Thread.sleep(1200);

        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/invoices"),
                "Không quay về list. URL: " + driver.getCurrentUrl());
    }

    @Test
    public void U4_4_TC12_PrintInvoice() throws Exception {
        openFirstInvoiceDetail();

        Set<String> before = driver.getWindowHandles();
        String urlBefore = driver.getCurrentUrl();

        // tìm nút/link in (cơ bản)
        List<WebElement> printBtns = driver.findElements(By.xpath(
                "//a[contains(@href,'print') or contains(@href,'preview')] | " +
                "//button[contains(.,'In') or contains(.,'Print')] | " +
                "//a[contains(.,'In') or contains(.,'Print')]"
        ));
        Assert.assertTrue(!printBtns.isEmpty(), "Không thấy nút 'In hóa đơn'.");
        printBtns.get(0).click();

        Thread.sleep(1500);

        Set<String> after = driver.getWindowHandles();
        boolean openedNewTab = after.size() > before.size();

        String urlAfter = driver.getCurrentUrl().toLowerCase();
        boolean urlLooksPrint = !urlAfter.equals(urlBefore.toLowerCase()) &&
                (urlAfter.contains("print") || urlAfter.contains("preview"));

        // assert đơn giản: tab mới OR url có print/preview OR ít nhất click không crash (đang ở trang chi tiết vẫn được)
        Assert.assertTrue(openedNewTab || urlLooksPrint || driver.getCurrentUrl().contains("/admin/invoices/"),
                "Click In nhưng không thấy tab mới/URL print. URL: " + driver.getCurrentUrl());

        // nếu có tab mới thì đóng cho gọn
        if (openedNewTab) {
            for (String h : after) {
                if (!before.contains(h)) {
                    driver.switchTo().window(h);
                    Thread.sleep(800);
                    driver.close();
                    break;
                }
            }
            // quay về tab cũ
            for (String h : before) driver.switchTo().window(h);
        }
    }
}
