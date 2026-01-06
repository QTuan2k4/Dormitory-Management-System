package com.group7.DMS.Epic4;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class U43{

    WebDriver driver;

    String BASE_URL = "http://localhost:8080";
    String INVOICE_URL = BASE_URL + "/admin/invoices";

    String ADMIN_USER = "admin";
    String ADMIN_PASS = "admin123";

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        driver.get(BASE_URL + "/login");
        Thread.sleep(800);

        driver.findElement(By.xpath("//*[@id='username' or @name='username']")).sendKeys(ADMIN_USER);
        driver.findElement(By.xpath("//*[@id='password' or @name='password']")).sendKeys(ADMIN_PASS);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(1200);

        driver.get(INVOICE_URL);
        Thread.sleep(1200);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Thread.sleep(5000); // cho bạn nhìn kết quả 5 giây
        if (driver != null) driver.quit();
    }

    @Test
    public void TC_FilterStatus_Paid() throws Exception {
        new Select(driver.findElement(By.xpath("//select[contains(@id,'status') or contains(@name,'status')]")))
                .selectByVisibleText("Đã TT");

        clickFilterButton();
        Thread.sleep(1200);

        List<WebElement> rows = driver.findElements(By.xpath("//table/tbody/tr[count(td)>1]"));
        Assert.assertTrue(rows.size() > 0, "Không có dòng dữ liệu sau khi lọc Đã TT");

        for (WebElement r : rows) {
            String status = r.findElement(By.xpath("./td[8]")).getText().trim();
            Assert.assertTrue(status.equals("Đã TT") || status.toLowerCase().contains("paid"),
                    "Dòng không phải Đã TT: " + status);
        }
    }

    @Test
    public void TC_FilterStatus_Unpaid() throws Exception {
        new Select(driver.findElement(By.xpath("//select[contains(@id,'status') or contains(@name,'status')]")))
                .selectByVisibleText("Chưa TT");

        clickFilterButton();
        Thread.sleep(1200);

        List<WebElement> rows = driver.findElements(By.xpath("//table/tbody/tr[count(td)>1]"));
        Assert.assertTrue(rows.size() > 0, "Không có dòng dữ liệu sau khi lọc Chưa TT");

        for (WebElement r : rows) {
            String status = r.findElement(By.xpath("./td[8]")).getText().trim();
            Assert.assertTrue(status.equals("Chưa TT") || status.toLowerCase().contains("unpaid"),
                    "Dòng không phải Chưa TT: " + status);
        }
    }

    // ===== THÊM TEST LỌC QUÁ HẠN =====
    @Test
    public void TC_FilterStatus_Overdue() throws Exception {
        new Select(driver.findElement(By.xpath("//select[contains(@id,'status') or contains(@name,'status')]")))
                .selectByVisibleText("Quá hạn");

        clickFilterButton();
        Thread.sleep(1200);

        List<WebElement> rows = driver.findElements(By.xpath("//table/tbody/tr[count(td)>1]"));
        Assert.assertTrue(rows.size() > 0, "Không có dòng dữ liệu sau khi lọc Quá hạn");

        for (WebElement r : rows) {
            String status = r.findElement(By.xpath("./td[8]")).getText().trim();
            String st = status.toLowerCase();
            Assert.assertTrue(status.equals("Quá hạn") || st.contains("quá hạn") || st.contains("qua han") || st.contains("overdue"),
                    "Dòng không phải Quá hạn: " + status);
        }
    }

    private void clickFilterButton() {
        List<WebElement> btn = driver.findElements(By.xpath("//button[contains(normalize-space(.),'Lọc') or @type='submit']"));
        if (!btn.isEmpty()) btn.get(0).click();
    }
}
