package com.group7.DMS.Epic2;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class U25 {

    WebDriver driver;

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

     
        driver.get("http://localhost:8080/login");
        Thread.sleep(800);

      
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123"); // đổi pass thì sửa chỗ này
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(1200);

   
        driver.get("http://localhost:8080/admin/buildings");
        Thread.sleep(800);
    }

    // ==========================================================
    // U2.5_TC08 – Xóa thành công tòa nhà không còn phòng hoạt động
    // ==========================================================
    @Test
    public void TC_U2_5_08_deleteBuilding_noActiveRooms() throws Exception {

        // ---------- 1. ĐẢM BẢO CÓ TÒA A06 (PRECONDITION) ----------
        ensureBuildingA06Exists();

        // Reload lại danh sách cho chắc
        driver.get("http://localhost:8080/admin/buildings");
        Thread.sleep(800);

        // Tìm dòng có TÊN Tòa Nhà = "A06" (cột 2)
        List<WebElement> rowsA06 = driver.findElements(
                By.xpath("//table//tr[td[2][contains(normalize-space(), 'A06')]]")
        );

        // Nếu vẫn không có thì fail thật
        Assert.assertTrue(
                !rowsA06.isEmpty(),
                "Precondition: phải có tòa nhà A06 để test xóa."
        );

        WebElement rowA06 = rowsA06.get(0);

        // Lưu lại mã + tên để log
        String codeA06 = rowA06.findElement(By.xpath("td[1]")).getText().trim();
        String nameA06 = rowA06.findElement(By.xpath("td[2]")).getText().trim();
        System.out.println("Sẽ xóa tòa nhà mã = " + codeA06 + ", tên = " + nameA06);

        // ---------- 2. THỰC HIỆN XÓA ----------
        WebElement deleteBtn = rowA06.findElement(
                By.xpath(".//a[@title='Xóa']")
        );
        deleteBtn.click();

        // Handle JS confirm
        try {
            Alert confirm = driver.switchTo().alert();
            confirm.accept();
        } catch (NoAlertPresentException ex) {
            // nếu không có alert thì bỏ qua
        }

        Thread.sleep(1500);

        // ---------- 3. KIỂM TRA THÔNG BÁO THÀNH CÔNG ----------
        WebElement successAlert = driver.findElement(
                By.cssSelector(".alert.alert-success")
        );
        String successText = successAlert.getText().trim();
        System.out.println("Thông báo sau khi xóa: " + successText);

        String successLower = successText.toLowerCase();
        Assert.assertTrue(
                successLower.contains("xóa") || successLower.contains("đã được lưu thành công")
                        || successLower.contains("thành công"),
                "Thông báo sau khi xóa phải là thành công."
        );

        // ---------- 4. KIỂM TRA A06 BIẾN MẤT KHỎI DANH SÁCH ----------
        List<WebElement> rowsAfter = driver.findElements(
                By.xpath("//table//tr[td[2][contains(normalize-space(), 'A06')]]")
        );

        Assert.assertTrue(
                rowsAfter.isEmpty(),
                "Sau khi xóa, tòa nhà A06 phải biến mất khỏi danh sách."
        );
    }

    /**
     * Đảm bảo trong danh sách có tòa nhà tên A06.
     * Nếu chưa có thì tạo mới A06 với số tầng 0, trạng thái Hoạt động.
     */
    private void ensureBuildingA06Exists() throws Exception {
        // tìm A06 hiện có
        List<WebElement> rowsA06 = driver.findElements(
                By.xpath("//table//tr[td[2][contains(normalize-space(), 'A06')]]")
        );
        if (!rowsA06.isEmpty()) {
            System.out.println("Đã có tòa nhà A06 sẵn, dùng luôn để test xóa.");
            return;
        }

        System.out.println("Chưa có tòa nhà A06, tiến hành tạo mới để test.");

        // Nhấn nút "Thêm Tòa Nhà Mới"
        WebElement addBtn = driver.findElement(
                By.xpath("//a[contains(@href, '/admin/buildings/new')]")
        );
        addBtn.click();
        Thread.sleep(800);

        // Form thêm tòa nhà
        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.clear();
        nameInput.sendKeys("A06");

        WebElement floorsInput = driver.findElement(By.id("totalFloors"));
        floorsInput.clear();
        floorsInput.sendKeys("0");

        Select statusSelect = new Select(driver.findElement(By.id("status")));
        statusSelect.selectByValue("ACTIVE");

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(1500);

       
        driver.get("http://localhost:8080/admin/buildings");
        Thread.sleep(800);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
