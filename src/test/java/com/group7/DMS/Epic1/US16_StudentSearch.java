package com.group7.DMS.Epic1;


import java.time.Duration;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class US16_StudentSearch {
    WebDriver driver;
    WebDriverWait wait;

    // ===== Locators =====
    By usernameInput = By.id("username");
    By passwordInput = By.id("password");
    By loginBtn = By.cssSelector("button.btn-login");
    By studentMenu = By.xpath("//a[contains(@href,'/admin/students')]");
    By searchInput = By.id("searchName");
    By searchBtn = By.cssSelector(".filter-row button[type='submit']");
    By studentRows = By.cssSelector("table tbody tr");
    By statusSelect = By.id("statusFilter");    // dropdown trạng thái

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Truy cập login
        driver.get("http://localhost:8080/DMS/login");
    }

    @AfterMethod
    public void teardown() {
        try {
            // Dừng 5 giây trước khi thoát
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (driver != null) driver.quit();
    }

    private void type(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    private void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); window.scrollBy(0, -100);", element);
        try { Thread.sleep(200); } catch (InterruptedException e) {}
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    @Test
    public void TC_1_6_01_SearchStudentByName() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Truy cập trang danh sách sinh viên
        click(studentMenu);
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Nhập tên sinh viên "Trân" vào ô tìm kiếm
        String keyword = "Trân";
        type(searchInput, keyword);

        // 4️⃣ Click nút Tìm kiếm
        click(searchBtn);

        // 5️⃣ Chờ bảng load kết quả
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));

        // 6️⃣ Kiểm tra tất cả dòng có tên chứa "Trân"
        List<WebElement> rows = driver.findElements(studentRows);
        Assert.assertTrue(rows.size() > 0, "Không tìm thấy sinh viên nào với từ khóa: " + keyword);

        for (WebElement row : rows) {
            String fullName = row.findElement(By.xpath("./td[2]")).getText();
            Assert.assertTrue(fullName.contains(keyword),
                    "Dòng sinh viên không chứa tên '" + keyword + "': " + fullName);
        }

        System.out.println("TC_1.6.01: Tìm kiếm sinh viên theo tên '" + keyword + "' hiển thị thành công " + rows.size() + " kết quả.");
    }
    @Test
    public void TC_1_6_02_SearchStudentById() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Truy cập trang danh sách sinh viên
        click(studentMenu);
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Nhập mã sinh viên cần tìm
        String studentId = "4551050175";
        type(searchInput, studentId);

        // 4️⃣ Click nút Tìm kiếm
        click(searchBtn);

        // 5️⃣ Chờ bảng load kết quả
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));

        // 6️⃣ Kiểm tra tất cả dòng có mã sinh viên đúng
        List<WebElement> rows = driver.findElements(studentRows);
        Assert.assertTrue(rows.size() > 0, "Không tìm thấy sinh viên nào với mã: " + studentId);

        for (WebElement row : rows) {
            String id = row.findElement(By.xpath("./td[1]")).getText();
            Assert.assertEquals(id, studentId,
                    "Dòng sinh viên không có mã '" + studentId + "': " + id);
        }

        System.out.println("TC_1.6.02: Tìm kiếm sinh viên theo mã '" + studentId + "' hiển thị thành công " + rows.size() + " kết quả.");
    }
    @Test
    public void TC_1_6_03_SearchStudentByPendingStatus() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Truy cập trang danh sách sinh viên
        click(studentMenu);
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Chọn trạng thái "Chờ duyệt" trong filter
        Select statusDropdown = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect)));
        statusDropdown.selectByVisibleText("Chờ duyệt");

        // 4️⃣ Click nút Tìm kiếm
        click(searchBtn);

        // 5️⃣ Chờ bảng load kết quả
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));

        // 6️⃣ Kiểm tra tất cả dòng có trạng thái "Chờ duyệt"
        List<WebElement> rows = driver.findElements(studentRows);
        Assert.assertTrue(rows.size() > 0, "Không tìm thấy sinh viên nào với trạng thái 'Chờ duyệt'");

        for (WebElement row : rows) {
            String statusText = row.findElement(By.xpath("./td[5]//span")).getText();
            Assert.assertEquals(statusText, "Chờ duyệt", "Dòng sinh viên không có trạng thái 'Chờ duyệt': " + statusText);
        }

        System.out.println("TC_1.6.03: Tìm kiếm sinh viên theo trạng thái 'Chờ duyệt' hiển thị thành công " + rows.size() + " kết quả.");
    }
    @Test
    public void TC_1_6_04_SearchByStatusApproved() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Vào trang Quản lý Sinh viên
        click(studentMenu);
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Chọn trạng thái "Đã duyệt"
        Select statusDropdown = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect)));
        statusDropdown.selectByVisibleText("Đã duyệt");

        // 4️⃣ Nhấn nút Tìm kiếm
        click(searchBtn);

        // 5️⃣ Kiểm tra tất cả dòng đều là trạng thái "Đã duyệt"
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));
        for (WebElement row : driver.findElements(studentRows)) {
            String statusText = row.findElement(By.cssSelector("td:nth-child(5) .status-badge")).getText().trim();
            Assert.assertEquals(statusText, "Đã duyệt", "Có hồ sơ không phải trạng thái Đã duyệt!");
        }
        System.out.println("TC_1.6.04: Tìm kiếm theo trạng thái 'Đã duyệt' thành công.");
    }

    @Test
    public void TC_1_6_05_SearchNoResult() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Vào trang Quản lý Sinh viên
        click(studentMenu);
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Nhập từ khóa không tồn tại
        type(searchInput, "w"); // từ khóa không có
        click(searchBtn);

        // 4️⃣ Kiểm tra thông báo hoặc bảng trống
        By emptyState = By.cssSelector(".empty-state");
        boolean noResult = wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(emptyState),
            ExpectedConditions.numberOfElementsToBe(studentRows, 0)
        ));
        Assert.assertTrue(noResult, "Hệ thống chưa hiển thị thông báo 'Không tìm thấy hồ sơ' hoặc bảng trống!");
        System.out.println("TC_1.6.05: Tìm kiếm không có kết quả hiển thị thành công.");
    }
    @Test
    public void TC_1_6_06_ResetFilterToAll() throws InterruptedException {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Truy cập trang danh sách sinh viên
        driver.get("http://localhost:8080/DMS/admin/students");

        // 3️⃣ Thực hiện một tìm kiếm/lọc trước đó (ví dụ nhập từ khóa "Trân")
        type(searchInput, "Trân");
        click(searchBtn);

        // 4️⃣ Chờ bảng load
        By studentRows = By.cssSelector("table tbody tr");
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));

        // 5️⃣ Reset bộ lọc: xóa ô tìm kiếm và chọn "Tất cả" trong dropdown trạng thái
        type(searchInput, ""); // xóa từ khóa

        By statusSelect = By.id("statusFilter");

        boolean success = false;
        for (int i = 0; i < 3; i++) { // thử tối đa 3 lần để tránh StaleElementReference
            try {
                Select statusDropdown = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect)));
                statusDropdown.selectByVisibleText("Tất cả");
                success = true;
                break;
            } catch (StaleElementReferenceException e) {
                // Nếu element cũ stale, đợi 0.5s và thử lại
                Thread.sleep(500);
            }
        }
        if (!success) {
            Assert.fail("Không thể chọn 'Tất cả' trong dropdown trạng thái vì element stale.");
        }

        // 6️⃣ Nhấn nút Tìm kiếm để reload bảng
        click(searchBtn);

        // 7️⃣ Chờ bảng load lại
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));

        // 8️⃣ Kiểm tra bảng không trống (hiển thị toàn bộ dữ liệu)
        int rowCount = driver.findElements(studentRows).size();
        Assert.assertTrue(rowCount > 0, "Bảng không hiển thị dữ liệu sau khi reset bộ lọc.");

        System.out.println("TC_1.6.06: Reset bộ lọc về 'Tất cả' thành công. Số hồ sơ hiển thị: " + rowCount);
    }

    @Test
    public void TC_1_6_07_FilterSpecialCondition() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        click(loginBtn);

        // 2️⃣ Truy cập trang Quản lý Sinh viên
        click(studentMenu);
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Chọn hoàn cảnh “Sổ hộ nghèo/cận nghèo”
        By specialSelect = By.id("specialFilter"); // ❗ THAY đúng id của bạn
        Select specialDropdown = new Select(wait.until(
            ExpectedConditions.visibilityOfElementLocated(specialSelect)
        ));
        specialDropdown.selectByVisibleText("Sổ hộ nghèo/cận nghèo");

        // 4️⃣ Nhấn nút Tìm kiếm
        click(searchBtn);

        // 5️⃣ Chờ bảng load dữ liệu
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentRows));
        List<WebElement> rows = driver.findElements(studentRows);

        Assert.assertTrue(rows.size() > 0,
                "Không tìm thấy sinh viên nào thuộc diện 'Sổ hộ nghèo/cận nghèo'!");

        // 6️⃣ Kiểm tra tất cả dòng đều có hoàn cảnh đặc biệt đúng
        for (WebElement row : rows) {
            String specialText = row.findElement(By.xpath("./td[6]")).getText().trim();
            Assert.assertEquals(
                    specialText,
                    "Sổ hộ nghèo/cận nghèo",
                    "Có hồ sơ không thuộc diện Sổ hộ nghèo/cận nghèo: " + specialText
            );
        }

        System.out.println("TC_1.6.07: Lọc hoàn cảnh 'Sổ hộ nghèo/cận nghèo' thành công. Tổng dòng: " + rows.size());
    }

}
