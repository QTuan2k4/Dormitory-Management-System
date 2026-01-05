package com.group7.DMS.Epic3;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class US31_RoomListTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        loginAsAdmin();
        driver.get("http://localhost:8080/DMS/admin/rooms");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table")));
    }

    // LOGIN
    public void loginAsAdmin() {
        driver.get("http://localhost:8080/DMS/login");
        driver.findElement(By.id("username")).sendKeys("1");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    //  TC_3.1.02
    @Test
    public void TC_3_1_02_searchRoomByRoomNumber() {
        WebElement searchBox = driver.findElement(By.name("roomNumber"));
        searchBox.clear();
        searchBox.sendKeys("01");

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> roomNumbers =
                driver.findElements(By.cssSelector("tbody tr td:nth-child(2)"));

        Assert.assertTrue(roomNumbers.size() > 0, "Không có phòng nào được hiển thị");

        for (WebElement room : roomNumbers) {
            Assert.assertTrue(
                    room.getText().contains("01"),
                    "Phòng không khớp điều kiện tìm kiếm: " + room.getText()
            );
        }
    }

    // =TC_3.1.03
    @Test
    public void TC_3_1_03_searchRoomWithInvalidKeyword() {
        driver.findElement(By.name("roomNumber")).sendKeys("abc");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement emptyRow =
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("tbody tr td")));

        Assert.assertTrue(
                emptyRow.getText().contains("Không tìm thấy phòng"),
                "Không hiển thị thông báo không tìm thấy phòng"
        );
    }

    // TC_3.1.04 
    @Test
    public void TC_3_1_04_filterRoomByBuilding() {
        driver.get("http://localhost:8080/DMS/admin/rooms");

        driver.findElement(By.name("buildingId")).sendKeys("C5");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));

        //Nếu không có phòng → PASS
        if (rows.size() == 1 && rows.get(0).getText().contains("Không tìm thấy")) {
            System.out.println(" Không có phòng thuộc tòa C5 → PASS");
            Assert.assertTrue(true);
            return;
        }

        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() < 7) continue;

            String building = cols.get(0).getText().trim();
            Assert.assertEquals(building, "C5", "Phòng không thuộc tòa C5");
        }
    }


    // TC_3.1.05 
    @Test
    public void TC_3_1_05_filterRoomByStatus() {
        driver.get("http://localhost:8080/DMS/admin/rooms");

        // Reset filter
        driver.findElement(By.name("roomNumber")).clear();
        driver.findElement(By.name("buildingId")).sendKeys("");
        driver.findElement(By.name("priceFrom")).clear();
        driver.findElement(By.name("priceTo")).clear();

        // Filter theo trạng thái
        driver.findElement(By.name("status")).sendKeys("AVAILABLE");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));

        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() < 7) continue;

            String statusText = cols.get(6).getText().trim();

            Assert.assertEquals(
                statusText,
                "Còn chỗ",
                "BUG: Hệ thống vẫn hiển thị phòng không phải Còn chỗ"
            );
        }
    }

    //  TC_3.1.06 
    @Test
    public void TC_3_1_06_filterRoomByMultipleConditions() {
        driver.get("http://localhost:8080/DMS/admin/rooms");

        // Nhập số phòng
        driver.findElement(By.name("roomNumber")).sendKeys("1");

        // Chọn tòa nhà (ví dụ C5)
        WebElement buildingDropdown = driver.findElement(By.name("buildingId"));
        buildingDropdown.sendKeys("C5");

        // Chọn trạng thái Còn chỗ
        WebElement statusDropdown = driver.findElement(By.name("status"));
        statusDropdown.sendKeys("AVAILABLE");

        // Click Lọc
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));

        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() == 0) continue;

            String roomNumber = cols.get(1).getText().trim();
            String building = cols.get(0).getText().trim();
            String status = cols.get(6).getText().trim();

            Assert.assertTrue(roomNumber.contains("1"),
                    "Số phòng không đúng điều kiện: " + roomNumber);

            Assert.assertEquals(building, "C5",
                    "Tòa nhà không đúng");

            Assert.assertTrue(
                    status.equalsIgnoreCase("Còn chỗ") ||
                    status.equalsIgnoreCase("AVAILABLE"),
                    "Trạng thái không đúng: " + status
            );
        }
    }

    //TC_3.1.07
    @Test
    public void TC_3_1_07_resetFilter() {
        driver.findElement(By.name("roomNumber")).sendKeys("01");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement resetBtn =
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("a.btn-outline-secondary")));
        resetBtn.click();

        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/DMS/admin/rooms"));

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        Assert.assertTrue(rows.size() > 0, "Danh sách phòng không được reset");
    }

    //  TC_3.1.08
    @Test
    public void TC_3_1_08_filterRoomByPriceRange() {
        driver.findElement(By.name("minPrice")).sendKeys("25000000");
        driver.findElement(By.name("maxPrice")).sendKeys("30000000");

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr")));

        List<WebElement> priceCells =
                driver.findElements(By.cssSelector("tbody tr td:nth-child(6)"));

        for (WebElement priceCell : priceCells) {
            String priceText = priceCell.getText()
                    .replace("VNĐ", "")
                    .replace(",", "")
                    .trim();

            long price = Long.parseLong(priceText);

            Assert.assertTrue(
                    price >= 25_000_000 && price <= 30_000_000,
                    "Giá phòng ngoài khoảng cho phép: " + price
            );
        }
    }

    //TEARDOWN
    @AfterClass
    public void tearDown() throws InterruptedException {
        Thread.sleep(3000);
        driver.quit();
    }
}
