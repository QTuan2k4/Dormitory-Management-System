package com.group7.DMS.Epic1;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;

public class U13 {

    private WebDriver driver;

    private final String BASE_LOGIN_URL    = "http://localhost:8080/login";
    private final String STUDENTS_LIST_URL = "http://localhost:8080/admin/students";

   
    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin123";

   
    private final String MSV_PROCESSED = "4551050260";

    private By usernameInput = By.id("username");
    private By passwordInput = By.id("password");
    private By loginButton   = By.xpath("//form[@method='post']//button[@type='submit']");

    // ==== LOCATOR FILTER / SEARCH ====
    private By searchInput   = By.id("searchName");   // input Mã sinh viên / Họ và tên
    private By searchButton  = By.xpath("//form[@class='filter-row']//button[@type='submit']");

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Thread.sleep(500);
        if (driver != null) {
            driver.quit();
        }
    }

 
    private void loginAsAdmin() throws Exception {
        driver.get(BASE_LOGIN_URL);
        Thread.sleep(500);

        driver.findElement(usernameInput).clear();
        driver.findElement(passwordInput).clear();

        driver.findElement(usernameInput).sendKeys(ADMIN_USERNAME);
        driver.findElement(passwordInput).sendKeys(ADMIN_PASSWORD);
        driver.findElement(loginButton).click();

        Thread.sleep(1000);
    }

    // ===============================================================
    // HELPER: Mở danh sách hồ sơ + search theo MSV
    // ===============================================================
    private WebElement searchAndGetRowByStudentCode(String studentCode) throws Exception {
        driver.get(STUDENTS_LIST_URL);
        Thread.sleep(1000);

        WebElement searchBox = driver.findElement(searchInput);
        searchBox.clear();
        searchBox.sendKeys(studentCode);

        driver.findElement(searchButton).click();
        Thread.sleep(1000);

        // <td> đầu tiên là Mã Sinh Viên
        return driver.findElement(By.xpath(
                "//table/tbody/tr[td[1][normalize-space()='" + studentCode + "']]"
        ));
    }

    // ===============================================================
    // U1.3 – TC_1.3.05
    // Kiểm tra ẩn/vô hiệu hóa nút "Duyệt" khi hồ sơ đã được xử lý
    // ===============================================================
    @Test
    public void TC_1_3_05_HideApproveButton_WhenProcessed() throws Exception {
        loginAsAdmin();

        WebElement row = searchAndGetRowByStudentCode(MSV_PROCESSED);

        // 1. Luôn phải có nút Xem
        WebElement viewButton = row.findElement(By.cssSelector(".btn-view"));
        Assert.assertTrue(viewButton.isDisplayed(), "Dòng msv phải hiển thị nút 'Xem'");

        // 2. Trạng thái phải là ĐÃ DUYỆT hoặc TỪ CHỐI (tiền điều kiện của test case)
        WebElement statusBadge = row.findElement(By.cssSelector(".status-badge"));
        String statusText = statusBadge.getText().trim();
        System.out.println("TC_1.3.05 - Status text (MSV " + MSV_PROCESSED + "): " + statusText);

        boolean processed =
                statusText.equalsIgnoreCase("Đã duyệt")
                        || statusText.equalsIgnoreCase("Từ chối")
                        || statusText.equalsIgnoreCase("ĐÃ DUYỆT")
                        || statusText.equalsIgnoreCase("ĐÃ TỪ CHỐI");

        if (!processed) {
            Assert.fail(
                    "Tiền điều kiện KHÔNG ĐÚNG: hồ sơ " + MSV_PROCESSED +
                    " đang là '" + statusText +
                    "'. Cần set dữ liệu về 'Đã duyệt' hoặc 'Từ chối' rồi hãy chạy TC_1.3.05."
            );
        }

        // 3. Nút "Duyệt hồ sơ" phải ẩn hoặc vô hiệu hóa
        boolean hasApproveButton = row.findElements(By.cssSelector(".btn.btn-sm.btn-success")).size() > 0;

        if (hasApproveButton) {
            WebElement approveBtn = row.findElement(By.cssSelector(".btn.btn-sm.btn-success"));
            boolean disabled = !approveBtn.isEnabled()
                    || "true".equalsIgnoreCase(approveBtn.getAttribute("disabled"));
            Assert.assertTrue(
                    disabled,
                    "Nút 'Duyệt hồ sơ' tồn tại nhưng phải bị vô hiệu hóa khi hồ sơ đã được xử lý"
            );
        } else {
            // Không có nút luôn → đúng yêu cầu “ẩn”
            Assert.assertTrue(true, "Nút 'Duyệt hồ sơ' đã bị ẩn khỏi giao diện khi hồ sơ đã xử lý");
        }
    }

    // ===============================================================
    // U1.3 – TC_1.3.06
    // Kiểm tra ẩn/vô hiệu hóa nút "Từ chối" khi hồ sơ đã được xử lý
    // ===============================================================
    @Test
    public void TC_1_3_06_HideRejectButton_WhenProcessed() throws Exception {
        loginAsAdmin();

        WebElement row = searchAndGetRowByStudentCode(MSV_PROCESSED);

        // 1. Status phải là ĐÃ DUYỆT hoặc TỪ CHỐI
        WebElement statusBadge = row.findElement(By.cssSelector(".status-badge"));
        String statusText = statusBadge.getText().trim();
        System.out.println("TC_1.3.06 - Status text (MSV " + MSV_PROCESSED + "): " + statusText);

        boolean processed =
                statusText.equalsIgnoreCase("Đã duyệt")
                        || statusText.equalsIgnoreCase("Từ chối")
                        || statusText.equalsIgnoreCase("ĐÃ DUYỆT")
                        || statusText.equalsIgnoreCase("ĐÃ TỪ CHỐI");

        if (!processed) {
            Assert.fail(
                    "Tiền điều kiện KHÔNG ĐÚNG: hồ sơ " + MSV_PROCESSED +
                    " đang là '" + statusText +
                    "'. Cần set dữ liệu về 'Đã duyệt' hoặc 'Từ chối' rồi hãy chạy TC_1.3.06."
            );
        }

        // 2. Nút "Từ chối hồ sơ" phải ẩn hoặc vô hiệu hóa
        boolean hasRejectButton = row.findElements(By.cssSelector("button.btn-reject")).size() > 0;

        if (hasRejectButton) {
            WebElement rejectBtn = row.findElement(By.cssSelector("button.btn-reject"));
            boolean disabled = !rejectBtn.isEnabled()
                    || "true".equalsIgnoreCase(rejectBtn.getAttribute("disabled"));
            Assert.assertTrue(
                    disabled,
                    "Nút 'Từ chối hồ sơ' tồn tại nhưng phải bị vô hiệu hóa khi hồ sơ đã được xử lý"
            );
        } else {
            Assert.assertTrue(true, "Nút 'Từ chối hồ sơ' đã bị ẩn khỏi giao diện khi hồ sơ đã xử lý");
        }
    }
}
