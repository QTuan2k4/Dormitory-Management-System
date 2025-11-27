package com.group7.DMS.Epic1;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class U15 {

    private WebDriver driver;

  
    private final String BASE_LOGIN_URL              = "http://localhost:8080/login";
    private final String ADMIN_STUDENTS_URL         = "http://localhost:8080/admin/students";
    // Trang thông tin cá nhân sinh viên (file bạn vừa gửi)
    private final String STUDENT_PERSONAL_INFO_URL  = "http://localhost:8080/student/register-personal-info";

    // Admin
    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin123";

    // Sinh viên dùng cho các test U1.5
    private final String STUDENT_CODE_FOR_REJECT   = "4551050261";
    private final String STUDENT_LOGIN_USERNAME    = "4551050261";
    private final String STUDENT_LOGIN_PASSWORD    = "1234567";

    // ========== LOCATORS CHUNG ==========
    // Login
    private By usernameInput = By.id("username");
    private By passwordInput = By.id("password");
    private By loginButton   = By.xpath("//form[@method='post']//button[@type='submit']");

    // List Students (admin)
    private By searchInput   = By.id("searchName");
    private By searchButton  = By.xpath("//form[@class='filter-row']//button[@type='submit']");

    // Chi tiết sinh viên (admin)
    private By statusBadgeDetail   = By.cssSelector(".status-badge");
    private By rejectButtonInDetail = By.cssSelector("button.btn-action.btn-reject");

    // Modal lý do từ chối
    private By rejectReasonModal = By.xpath("//div[contains(@class,'modal') && contains(@class,'show')]");
    private By reasonTextarea    = By.xpath("//div[contains(@class,'modal') and contains(@class,'show')]//textarea");
    private By confirmRejectBtn  = By.xpath("//div[contains(@class,'modal') and contains(@class,'show')]//button[contains(.,'Xác nhận') or contains(.,'Từ chối')]");

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        if (driver != null) {
            driver.quit();
        }
    }

    // =========================================================
    // HELPER: Login admin
    // =========================================================
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

    // =========================================================
    // HELPER: Login sinh viên
    // =========================================================
    private void loginAsStudent(String username, String password) throws Exception {
        driver.get(BASE_LOGIN_URL);
        Thread.sleep(500);

        driver.findElement(usernameInput).clear();
        driver.findElement(passwordInput).clear();

        driver.findElement(usernameInput).sendKeys(username);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();

        Thread.sleep(1000);
    }

    // =========================================================
    // HELPER: Mở chi tiết hồ sơ sinh viên trên trang admin
    // =========================================================
    private void openStudentDetailByStudentCode(String studentCode) throws Exception {
        driver.get(ADMIN_STUDENTS_URL);
        Thread.sleep(1000);

        WebElement searchBox = driver.findElement(searchInput);
        searchBox.clear();
        searchBox.sendKeys(studentCode);

        driver.findElement(searchButton).click();
        Thread.sleep(1000);

        WebElement row = driver.findElement(By.xpath(
                "//table/tbody/tr[td[normalize-space()='" + studentCode + "']]"
        ));

        WebElement viewBtn = row.findElement(By.cssSelector(".btn-view"));
        viewBtn.click();

        Thread.sleep(1000);
    }

    // =========================================================
    // HELPER: Lấy text trạng thái trong trang chi tiết admin
    // =========================================================
    private String getStudentStatusTextInAdminDetail() {
        try {
            WebElement status = driver.findElement(statusBadgeDetail);
            return status.getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    // =========================================================
    // HELPER: Mở modal "Lý do từ chối" (giả sử trạng thái đang CHỜ DUYỆT)
    // =========================================================
    private void openRejectReasonModalFromDetail() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement rejectBtn;
        try {
            rejectBtn = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(rejectButtonInDetail)
            );
        } catch (TimeoutException e) {
            String statusText = getStudentStatusTextInAdminDetail();
            Assert.fail("Không tìm thấy nút 'Từ Chối Hồ Sơ'. Có thể hồ sơ đang ở trạng thái: '"
                    + statusText + "'. Cần set về 'Chờ duyệt' trước khi chạy TC_1.5.08 / TC_1.5.010.");
            return;
        }

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", rejectBtn
        );
        Thread.sleep(400);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(rejectBtn)).click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", rejectBtn);
        }

        try {
            WebElement modal = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(rejectReasonModal)
            );
            Assert.assertTrue(modal.isDisplayed(), "Modal nhập lý do từ chối phải hiển thị.");
        } catch (TimeoutException e) {
            Assert.fail("Sau khi bấm 'Từ Chối Hồ Sơ' nhưng modal lý do không hiển thị.");
        }
    }

    // =========================================================
    // TC_1.5.08 – Modal lý do từ chối khi bấm nút
    // =========================================================
    @Test
    public void TC_1_5_08_ShowRejectReasonModal_WhenClickReject() throws Exception {
        loginAsAdmin();

        openStudentDetailByStudentCode(STUDENT_CODE_FOR_REJECT);

        String statusText = getStudentStatusTextInAdminDetail();
        System.out.println("TC_1.5.08 - Trạng thái hiện tại (MSV "
                + STUDENT_CODE_FOR_REJECT + "): " + statusText);

        if (!statusText.equalsIgnoreCase("Chờ duyệt")) {
            Assert.fail("Tiền điều kiện sai: Hồ sơ sinh viên " + STUDENT_CODE_FOR_REJECT +
                    " đang ở trạng thái '" + statusText + "'. Cần chuyển về 'Chờ duyệt' rồi mới chạy TC_1.5.08.");
        }

        openRejectReasonModalFromDetail();
    }

    // =========================================================
    // TC_1.5.010 – Validate bắt buộc nhập lý do từ chối
    // =========================================================
    @Test
    public void TC_1_5_010_ValidateReasonRequired_WhenEmpty() throws Exception {
        loginAsAdmin();

        openStudentDetailByStudentCode(STUDENT_CODE_FOR_REJECT);

        String statusText = getStudentStatusTextInAdminDetail();
        System.out.println("TC_1.5.010 - Trạng thái hiện tại (MSV "
                + STUDENT_CODE_FOR_REJECT + "): " + statusText);

        if (!statusText.equalsIgnoreCase("Chờ duyệt")) {
            Assert.fail("Tiền điều kiện sai: Hồ sơ sinh viên " + STUDENT_CODE_FOR_REJECT +
                    " đang ở trạng thái '" + statusText + "'. Cần chuyển về 'Chờ duyệt' rồi mới chạy TC_1.5.010.");
        }

        openRejectReasonModalFromDetail();

        WebElement reasonBox = driver.findElement(reasonTextarea);
        reasonBox.clear();

        WebElement confirmBtn = driver.findElement(confirmRejectBtn);
        confirmBtn.click();
        Thread.sleep(800);

        String page = driver.getPageSource();
        System.out.println("TC_1.5.010 - Page sau khi bấm Xác nhận (empty reason):\n" + page);

        boolean hasErrorMsg = page.contains("Lý do từ chối là bắt buộc");
        Assert.assertTrue(
                hasErrorMsg,
                "Hệ thống phải hiển thị thông báo lỗi: 'Lý do từ chối là bắt buộc.'"
        );
    }

    // =========================================================
    // TC_1.5.09 – Sinh viên nhận được thông báo sau khi bị từ chối
    // -> Thông báo hiển thị trên trang /student/register-personal-info
    //    bằng alert-danger: "Hồ sơ đã bị từ chối. Hãy cập nhật..."
    // =========================================================
    @Test
    public void TC_1_5_09_StudentReceiveNotification_AfterRejected() throws Exception {
        // B1: Đăng nhập sinh viên
        loginAsStudent(STUDENT_LOGIN_USERNAME, STUDENT_LOGIN_PASSWORD);

        // B2: Vào đúng trang thông tin cá nhân (file HTML bạn gửi)
        driver.get(STUDENT_PERSONAL_INFO_URL);
        Thread.sleep(1000);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(7));

        // Đợi header "Thông tin cá nhân"
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h4[contains(normalize-space(),'Thông tin cá nhân')] | //div[@class='card-header']//h4[contains(normalize-space(),'Thông tin cá nhân')]")
            ));
        } catch (TimeoutException e) {
            String src = driver.getPageSource();
            System.out.println("TC_1.5.09 - Page source (không thấy header 'Thông tin cá nhân'):\n" + src);
            Assert.fail("Không thấy tiêu đề 'Thông tin cá nhân' trên trang /student/register-personal-info. Kiểm tra lại mapping.");
        }

        // B3: Tìm tất cả alert-danger trên trang
        java.util.List<WebElement> dangerAlerts =
                driver.findElements(By.cssSelector("div.alert.alert-danger"));

        if (dangerAlerts.isEmpty()) {
            String src = driver.getPageSource();
            System.out.println("TC_1.5.09 - Page source (không có alert-danger nào):\n" + src);
            Assert.fail("Không tìm thấy thông báo dạng alert-danger trên trang thông tin cá nhân sau khi hồ sơ bị từ chối.");
        }

        boolean foundRejectMessage = false;
        for (WebElement alert : dangerAlerts) {
            String text = alert.getText().trim();
            System.out.println("TC_1.5.09 - Found alert-danger: " + text);
            String lower = text.toLowerCase();
            if (lower.contains("hồ sơ đã bị từ chối")) {
                foundRejectMessage = true;
                break;
            }
        }

        Assert.assertTrue(
                foundRejectMessage,
                "Sinh viên phải thấy thông báo về việc hồ sơ bị từ chối trên trang thông tin cá nhân. " +
                        "Cần có alert-danger chứa text 'Hồ sơ đã bị từ chối...'"
        );
    }
}
