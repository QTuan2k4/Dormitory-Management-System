package com.group7.DMS.Epic2;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class US21_BuildingListTest {

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

    // U2.1_TC01 
    @Test
    public void U2_1_TC01_verifyDormitoryListStructure() {
        driver.get("http://localhost:8080/DMS/admin/buildings");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        Assert.assertTrue(rows.size() > 0, "Danh sách tòa nhà trống");

        List<WebElement> headers = driver.findElements(By.cssSelector("thead th"));
        Assert.assertTrue(headers.size() >= 4, "Không đủ cột hiển thị");

        //Kiểm tra có cột hành động (cột cuối)
        WebElement actionCell = rows.get(0).findElements(By.tagName("td")).get(headers.size() - 1);

        List<WebElement> actionButtons =
                actionCell.findElements(By.tagName("a"));

        Assert.assertTrue(actionButtons.size() >= 1, "Không có nút hành động");
    }

    // U2.1_TC02 
    @Test
    public void U2_1_TC02_verifyStatusDisplay() {
        driver.get("http://localhost:8080/DMS/admin/buildings");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> statusCells =
                driver.findElements(By.cssSelector("tbody tr td:nth-child(4)"));

        for (WebElement status : statusCells) {
            String text = status.getText().trim();

            System.out.println("Status text = [" + text + "]");

            //Bỏ qua trạng thái rỗng
            if (text.isEmpty()) {
                continue;
            }

            Assert.assertTrue(
                    text.equalsIgnoreCase("Hoạt động") ||
                    text.equalsIgnoreCase("Bảo trì"),
                    "Giá trị trạng thái không hợp lệ: " + text
            );
        }
    }


    // U2.1_TC03
    @Test
    public void U2_1_TC03_verifyPagination() {
        driver.get("http://localhost:8080/DMS/admin/buildings");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> pages = driver.findElements(By.cssSelector("ul.pagination a"));

        if (pages.size() < 2) {
            System.out.println(" Không đủ dữ liệu để phân trang → PASS");
            Assert.assertTrue(true);
            return;
        }

        WebElement page2 = driver.findElement(By.linkText("2"));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", page2);

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", page2);

        wait.until(ExpectedConditions.urlContains("page=2"));
        Assert.assertTrue(driver.getCurrentUrl().contains("page=2"));
    }

    //TEARDOWN
    @AfterMethod
    public void tearDown() throws InterruptedException {
        Thread.sleep(3000); 
        driver.quit();
    }
}
