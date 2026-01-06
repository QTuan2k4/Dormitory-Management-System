package com.group7.DMS.epic2;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class U26 {

    WebDriver driver;

    String loginUrl          = "http://localhost:8080/login";
    String adminBuildingsUrl = "http://localhost:8080/admin/buildings";

    private By usernameInput = By.id("username");
    private By passwordInput = By.id("password");
    private By loginButton   = By.xpath("//form[@method='post']//button[@type='submit']");

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        driver.get(loginUrl);
        Thread.sleep(800);

        driver.findElement(usernameInput).clear();
        driver.findElement(passwordInput).clear();
        driver.findElement(usernameInput).sendKeys("admin");
        driver.findElement(passwordInput).sendKeys("admin123");
        driver.findElement(loginButton).click();

        Thread.sleep(1500);
        Assert.assertTrue(
                driver.getCurrentUrl().contains("/admin"),
                "Login admin thất bại, không vào được khu vực /admin"
        );

        // Mở trang tòa nhà
        driver.get(adminBuildingsUrl);
        Thread.sleep(800);
    }

    /**
     * U2.6_TC10 - Lọc theo trạng thái + tìm kiếm theo Mã/Tên "A1".
     */
    @Test
    public void TC_U2_6_10_filterByStatusAndSearch() throws Exception {
        int beforeCount = driver.findElements(By.cssSelector("tbody tr")).size();

        WebElement searchInput = driver.findElement(By.name("name"));
        searchInput.clear();
        searchInput.sendKeys("A1");

        Select statusSelect = new Select(driver.findElement(By.name("status")));
        statusSelect.selectByValue("ACTIVE");

        WebElement filterButton = driver.findElement(
                By.xpath("//form[@method='GET']//button[@type='submit']")
        );
        filterButton.click();
        Thread.sleep(1500);

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));

        if (rows.size() == 1 && rows.get(0).findElements(By.cssSelector("td")).size() == 1) {
            Assert.fail("Không có tòa nhà nào khớp với điều kiện lọc (Hoạt động + chứa 'A1').");
        }

        for (WebElement row : rows) {
            List<WebElement> tds = row.findElements(By.cssSelector("td"));
            if (tds.size() < 4) continue;

            String code       = tds.get(0).getText().trim();
            String name       = tds.get(1).getText().trim();
            String statusText = tds.get(3).getText().trim();

            boolean containsA1 = code.contains("A1") || name.contains("A1");
            Assert.assertTrue(
                    containsA1,
                    "Mã hoặc Tên phải chứa 'A1' nhưng thấy: code=" + code + ", name=" + name
            );
            Assert.assertTrue(
                    statusText.contains("Hoạt động"),
                    "Trạng thái phải là 'Hoạt động' nhưng thấy: " + statusText
            );
        }
    }

    /**
     * U2.6_TC11 - Reset bộ lọc và tìm kiếm.
     */
    @Test
    public void TC_U2_6_11_resetFilterAndSearch() throws Exception {
        // Tạo trạng thái đã lọc trước
        WebElement searchInput = driver.findElement(By.name("name"));
        searchInput.clear();
        searchInput.sendKeys("A1");

        Select statusSelect = new Select(driver.findElement(By.name("status")));
        statusSelect.selectByValue("ACTIVE");

        WebElement filterButton = driver.findElement(
                By.xpath("//form[@method='GET']//button[@type='submit']")
        );
        filterButton.click();
        Thread.sleep(1200);

        List<WebElement> filteredRows = driver.findElements(By.cssSelector("tbody tr"));
        int filteredCount = filteredRows.size();

        // Bấm nút reset (icon redo)
        WebElement resetButton = driver.findElement(
                By.xpath("//a[contains(@class,'btn-outline-secondary') and .//i[contains(@class,'fa-redo')]]")
        );
        resetButton.click();
        Thread.sleep(1500);

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(
                currentUrl.contains("/admin/buildings"),
                "Sau khi reset phải quay lại /admin/buildings."
        );

        WebElement searchInputAfter = driver.findElement(By.name("name"));
        String searchValueAfter = searchInputAfter.getAttribute("value");
        Assert.assertTrue(
                searchValueAfter == null || searchValueAfter.isEmpty(),
                "Ô tìm kiếm phải trống sau khi reset."
        );

        Select statusSelectAfter = new Select(driver.findElement(By.name("status")));
        String selectedStatusAfter = statusSelectAfter.getFirstSelectedOption().getAttribute("value");
        Assert.assertTrue(
                selectedStatusAfter == null || selectedStatusAfter.isEmpty(),
                "Dropdown trạng thái phải không chọn gì sau khi reset."
        );

        List<WebElement> rowsAfterReset = driver.findElements(By.cssSelector("tbody tr"));
        int countAfterReset = rowsAfterReset.size();
        Assert.assertTrue(
                countAfterReset >= filteredCount,
                "Sau khi reset, số dòng phải >= số dòng sau khi lọc."
        );
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Thread.sleep(800);
        if (driver != null) driver.quit();
    }
}
