package com.group7.DMS.Epic1;


import java.time.Duration;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class US13_StudentDetail {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("http://localhost:8080/DMS/login");
    }

    @AfterMethod
    public void teardown() {
        try {
            // Dừng 5 giây trước khi thoát
            Thread.sleep(5000);
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

    private void clickJS(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        wait.until(ExpectedConditions.elementToBeClickable(element));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // ===== Locators =====
    By usernameInput = By.id("username");
    By passwordInput = By.id("password");
    By loginBtn = By.cssSelector("button.btn-login");
    By studentMenu = By.xpath("//a[contains(@href,'/admin/students')]");

    @Test
    public void TC_1_3_01_ViewStudentDetail() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        clickJS(driver.findElement(loginBtn));

        // 2️⃣ Vào danh sách sinh viên
        clickJS(driver.findElement(studentMenu));
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Tìm và click nút "Xem" của học sinh
        String studentId = "4551050175";
        boolean found = false;

        do {
            // Lấy tất cả row hiện tại
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            for (WebElement row : rows) {
                if (row.getText().contains(studentId)) {
                    // Dùng xpath tương đối từ row
                    WebElement viewBtn = row.findElement(By.xpath(".//td[6]/div/a[1]"));
                    clickJS(viewBtn);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // Kiểm tra nút "Next" để sang trang tiếp theo
                List<WebElement> nextBtns = driver.findElements(By.xpath("//a[text()='Next']"));
                if (!nextBtns.isEmpty() && nextBtns.get(0).isEnabled()) {
                    clickJS(nextBtns.get(0));
                    // đợi table refresh
                    wait.until(ExpectedConditions.stalenessOf(rows.get(0)));
                } else break;
            }
        } while (!found);

        if (!found) throw new RuntimeException("Không tìm thấy studentId: " + studentId);

        // 4️⃣ Kiểm tra trang chi tiết load đúng
        wait.until(ExpectedConditions.urlMatches(".*/admin/students/\\d+$"));
        WebElement studentIdElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@class='detail-value' and text()='" + studentId + "']")));

        Assert.assertTrue(studentIdElement.isDisplayed(), "Chi tiết sinh viên chưa hiển thị!");
        System.out.println("TC US13: Chi tiết sinh viên " + studentId + " hiển thị thành công.");
    }
    
    @Test
    public void TC_1_3_02_CheckAttachments() {
        // 1️⃣ Đăng nhập Admin
        type(usernameInput, "1");
        type(passwordInput, "123456");
        clickJS(driver.findElement(loginBtn));

        // 2️⃣ Vào danh sách sinh viên
        clickJS(driver.findElement(studentMenu));
        wait.until(ExpectedConditions.urlContains("/admin/students"));

        // 3️⃣ Tìm và click nút "Xem" của học sinh
        String studentId = "4551050175";
        boolean found = false;

        do {
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            for (WebElement row : rows) {
                if (row.getText().contains(studentId)) {
                    WebElement viewBtn = row.findElement(By.xpath(".//td[6]/div/a[1]"));
                    clickJS(viewBtn);
                    found = true;
                    break;
                }
            }
            if (!found) {
                List<WebElement> nextBtns = driver.findElements(By.xpath("//a[text()='Next']"));
                if (!nextBtns.isEmpty() && nextBtns.get(0).isEnabled()) {
                    clickJS(nextBtns.get(0));
                    wait.until(ExpectedConditions.stalenessOf(rows.get(0)));
                } else break;
            }
        } while (!found);

        if (!found) throw new RuntimeException("Không tìm thấy studentId: " + studentId);

        // 4️⃣ Kiểm tra trang chi tiết load đúng
        wait.until(ExpectedConditions.urlMatches(".*/admin/students/\\d+$"));
        WebElement studentIdElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@class='detail-value' and text()='" + studentId + "']")));
        Assert.assertTrue(studentIdElement.isDisplayed(), "Chi tiết sinh viên chưa hiển thị!");

        // 5️⃣ Scroll xuống tới ảnh đính kèm
        WebElement attachmentImg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("body > div.main-content > div.content-wrapper > div:nth-child(2) > div:nth-child(9) > div:nth-child(1) > div > img")
        ));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", attachmentImg
        );

        // 6️⃣ Kiểm tra ảnh hiển thị
        Assert.assertTrue(attachmentImg.isDisplayed(), "Ảnh đính kèm không hiển thị!");

        System.out.println("TC US13.02: Sinh viên " + studentId + " có ảnh đính kèm với src: " 
                           + attachmentImg.getAttribute("src"));
    }



}
