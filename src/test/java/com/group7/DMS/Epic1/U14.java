package com.group7.DMS.Epic1;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class U14 {

    private WebDriver driver;

    private final String BASE_LOGIN_URL     = "http://localhost:8080/login";
    private final String STUDENTS_LIST_URL  = "http://localhost:8080/admin/students";
    private final String RESIDENT_LIST_URL  = "http://localhost:8080/admin/residents"; // TODO: sửa đúng nếu khác

    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin123";

    private final String STUDENT_CODE = "4551050260";

    private String assignedRoomText;

    // ==== LOGIN LOCATORS ====
    private By usernameInput = By.id("username");
    private By passwordInput = By.id("password");
    private By loginButton   = By.xpath("//form[@method='post']//button[@type='submit']");

    // ==== STUDENT LIST LOCATORS ====
    private By searchInput   = By.id("searchName");
    private By searchButton  = By.xpath("//form[@class='filter-row']//button[@type='submit']");

    private By successAlert  = By.cssSelector(".alert.alert-success");

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Thread.sleep(800);
        if (driver != null) {
            driver.quit();
        }
    }

    // ================== HELPER ==================
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

    private WebElement openListAndFindRowByStudentCode(String studentCode) throws Exception {
        driver.get(STUDENTS_LIST_URL);
        Thread.sleep(800);

        WebElement searchBox = driver.findElement(searchInput);
        searchBox.clear();
        searchBox.sendKeys(studentCode);

        driver.findElement(searchButton).click();
        Thread.sleep(800);

        return driver.findElement(By.xpath(
                "//table/tbody/tr[td[1][normalize-space()='" + studentCode + "']]"
        ));
    }

    private void openStudentDetailFromRow(WebElement row) throws Exception {
        WebElement viewBtn = row.findElement(By.cssSelector(".btn-view"));
        viewBtn.click();
        Thread.sleep(800);
    }

    // =====================================================================
    // U1.4 - TC_1.4.07
    // =====================================================================
    @Test
    public void TC_1_4_07_ApproveStudentAndUpdateStatus() throws Exception {
        loginAsAdmin();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));

        // B1: List + tìm dòng MSV
        WebElement row = openListAndFindRowByStudentCode(STUDENT_CODE);

        // B2: vào chi tiết sinh viên
        openStudentDetailFromRow(row);

        // B3: lấy trạng thái hiện tại
        WebElement statusBadgeDetail = driver.findElement(
                By.cssSelector(".detail-row .status-badge")
        );
        String statusBefore = statusBadgeDetail.getText().trim();
        System.out.println("TC_1.4.07 - Trạng thái ban đầu chi tiết (MSV "
                + STUDENT_CODE + "): " + statusBefore);

        // ============= NHÁNH 1: đã ĐÃ DUYỆT sẵn rồi =============
        if (statusBefore.equalsIgnoreCase("Đã duyệt") ||
            statusBefore.equalsIgnoreCase("ĐÃ DUYỆT")) {

            // Quay lại list check lại đúng 'Đã duyệt' là ok
            WebElement rowAfter = openListAndFindRowByStudentCode(STUDENT_CODE);
            WebElement statusBadgeList = rowAfter.findElement(By.cssSelector(".status-badge"));
            String statusAfter = statusBadgeList.getText().trim();
            System.out.println("TC_1.4.07 - Trạng thái (list) khi vào đã là Đã duyệt: " + statusAfter);

            Assert.assertTrue(
                    statusAfter.equalsIgnoreCase("Đã duyệt") ||
                    statusAfter.equalsIgnoreCase("ĐÃ DUYỆT"),
                    "Mã SV " + STUDENT_CODE + " phải đang ở trạng thái 'Đã duyệt'"
            );
            // Kết thúc test (đã đạt expected)
            return;
        }

        // ============= NHÁNH 2: đúng tiền điều kiện CHỜ DUYỆT =============
        if (!statusBefore.equalsIgnoreCase("Chờ duyệt")) {
            Assert.fail(
                    "Tiền điều kiện sai: Mã SV " + STUDENT_CODE +
                            " đang là '" + statusBefore +
                            "'. Cần 'Chờ duyệt' hoặc đã 'Đã duyệt'."
            );
        }

        // B4: click "Duyệt" để sang trang phân phòng
        By approveLinkLocator = By.xpath(
                "//div[contains(@class,'approval-section')]//a[contains(@class,'btn-success') and contains(.,'Duyệt')]"
        );

        WebElement approveLink = wait.until(
                ExpectedConditions.presenceOfElementLocated(approveLinkLocator)
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", approveLink
        );
        Thread.sleep(300);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(approveLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", approveLink);
        } catch (ElementClickInterceptedException e) {
            System.out.println("Click bị chặn, thử lại JS click lần 2");
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", approveLink
            );
            Thread.sleep(300);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", approveLink);
        }

        // B5: đợi trang /assign-room
        wait.until(ExpectedConditions.urlContains("/assign-room"));
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Phân phòng cho sinh viên')]")
        ));

        // B6: chọn tòa
        WebElement buildingSelectEl = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("(//select)[1]"))
        );
        Select buildingSelect = new Select(buildingSelectEl);
        buildingSelect.selectByIndex(1); // bỏ qua "-- Chọn tòa --"
        Thread.sleep(500);

        // B7: chọn phòng trống
        WebElement roomSelectEl = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("(//select)[2]"))
        );
        Select roomSelect = new Select(roomSelectEl);
        roomSelect.selectByIndex(1);
        assignedRoomText = roomSelect.getFirstSelectedOption().getText().trim();
        System.out.println("TC_1.4.07 - Phòng được chọn: " + assignedRoomText);

        // B8: click "Xác nhận phân phòng"
        By confirmAssignButton = By.xpath(
                "//button[contains(.,'Xác nhận phân phòng')]"
        );
        WebElement confirmBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(confirmAssignButton)
        );
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", confirmBtn
        );
        Thread.sleep(300);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);

        Thread.sleep(1500);

        if (!driver.findElements(successAlert).isEmpty()) {
            String alertText = driver.findElement(successAlert).getText();
            System.out.println("TC_1.4.07 - Alert success sau phân phòng: " + alertText);
            Assert.assertTrue(
                    alertText.toLowerCase().contains("thành công")
                            || alertText.toLowerCase().contains("phân phòng")
                            || alertText.toLowerCase().contains("duyệt"),
                    "Thông báo thành công phải liên quan tới phân phòng / duyệt hồ sơ"
            );
        }

        // B9: quay lại list và verify Đã duyệt
        WebElement rowAfter = openListAndFindRowByStudentCode(STUDENT_CODE);
        WebElement statusBadgeList = rowAfter.findElement(By.cssSelector(".status-badge"));
        String statusAfter = statusBadgeList.getText().trim();
        System.out.println("TC_1.4.07 - Trạng thái sau duyệt (list): " + statusAfter);

        Assert.assertTrue(
                statusAfter.equalsIgnoreCase("Đã duyệt") ||
                statusAfter.equalsIgnoreCase("ĐÃ DUYỆT"),
                "Sau khi phân phòng, trạng thái sinh viên phải chuyển sang 'Đã duyệt'"
        );
    }

    // =====================================================================
    // U1.4 - TC_1.4.08 (giữ nguyên, anh/chị sửa URL + locator nếu khác)
    // =====================================================================
    @Test(dependsOnMethods = "TC_1_4_07_ApproveStudentAndUpdateStatus")
    public void TC_1_4_08_StudentCreatedAfterApprove() throws Exception {
        loginAsAdmin();

        driver.get(RESIDENT_LIST_URL);
        Thread.sleep(1000);

        WebElement searchInputResident = driver.findElement(
                By.xpath("//input[contains(@placeholder,'Mã sinh viên') or contains(@placeholder,'Mã SV')]")
        );
        searchInputResident.clear();
        searchInputResident.sendKeys(STUDENT_CODE);
        searchInputResident.sendKeys(Keys.ENTER);
        Thread.sleep(1000);

        boolean exists = driver.findElements(
                By.xpath("//table//tr[td[contains(normalize-space(),'" + STUDENT_CODE + "')]]")
        ).size() > 0;

        Assert.assertTrue(
                exists,
                "Sau khi duyệt & phân phòng, sinh viên mã " + STUDENT_CODE +
                        " phải xuất hiện trong danh sách cư trú"
        );
    }
}
