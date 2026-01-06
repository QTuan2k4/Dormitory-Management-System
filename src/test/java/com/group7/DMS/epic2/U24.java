package com.group7.DMS.epic2;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class U24 {

    WebDriver driver;

    @BeforeMethod
    public void setup() throws Exception {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        driver.get("http://localhost:8080/login");
        Thread.sleep(800);
        
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123"); 
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        Thread.sleep(1200);

        driver.get("http://localhost:8080/admin/buildings");
        Thread.sleep(800);
    }

    // ==========================================================
    // U2.4_TC07 – Sửa tòa nhà hợp lệ và KHÔNG sửa được Mã
    //  - Đổi A01 -> A1-Đã Sửa, trạng thái Bảo trì
    // ==========================================================
    @Test(priority = 1)
    public void TC_U2_4_07_editBuilding_cannotChangeCode_success() throws Exception {

        // 1. Tìm dòng có TÊN Tòa Nhà = "A01" (cột 2)
        WebElement rowA01 = driver.findElement(
                By.xpath("//table//tr[td[2][normalize-space()='A01']]")
        );

        // Lấy Mã tòa nhà trước khi sửa (cột 1)
        String buildingCodeBefore = rowA01.findElement(By.xpath("td[1]"))
                                          .getText().trim();

        // Nhấn nút "Sửa" trong dòng đó
        WebElement editBtn = rowA01.findElement(
                By.xpath(".//a[@title='Sửa']")
        );
        editBtn.click();
        Thread.sleep(800);

        
        WebElement idInput = driver.findElement(By.cssSelector("input[name='id']"));
        Assert.assertFalse(
                idInput.isDisplayed(),
                "Trường Mã Tòa Nhà (id) phải hidden, không được cho chỉnh sửa."
        );

        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.clear();
        nameInput.sendKeys("A1-Đã Sửa");  // theo test case trong Excel

        Select statusSelect = new Select(driver.findElement(By.id("status")));
        statusSelect.selectByValue("MAINTENANCE"); // Bảo trì

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(1500);

        // quay lại trang danh sách tòa nhà
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(
                currentUrl.contains("/admin/buildings"),
                "Sau khi sửa hợp lệ phải quay lại trang danh sách tòa nhà."
        );

        // 5. Kiểm tra lại: có dòng tên "A1-Đã Sửa"
        WebElement updatedRow = driver.findElement(
                By.xpath("//table//tr[td[2][normalize-space()='A1-Đã Sửa']]")
        );

        // Mã tòa nhà sau khi sửa phải giữ nguyên
        String buildingCodeAfter = updatedRow.findElement(By.xpath("td[1]"))
                                             .getText().trim();

        Assert.assertEquals(
                buildingCodeAfter,
                buildingCodeBefore,
                "Mã tòa nhà phải giữ nguyên sau khi sửa thông tin."
        );

        // Trạng thái hiển thị hợp lệ
        String statusText = updatedRow.findElement(By.xpath("td[4]")).getText().trim();
        Assert.assertTrue(
                statusText.contains("Bảo trì") || statusText.contains("Hoạt động"),
                "Trạng thái phải hiển thị hợp lệ (Bảo trì/Hoạt động)."
        );
    }

 
    // Đổi tên tòa nhà bị TRÙNG (A1-Đã Sửa -> A2)
    //  - A2 đã tồn tại => phải BÁO LỖI, đứng lại form (KHÔNG quay về list)
   
    @Test(priority = 2, dependsOnMethods = "TC_U2_4_07_editBuilding_cannotChangeCode_success")
    public void TC_U2_4_08_editBuilding_duplicateName_showsError() throws Exception {

     

       
        driver.get("http://localhost:8080/admin/buildings");
        Thread.sleep(800);

        // 2. Tìm dòng có TÊN Tòa Nhà = "A1-Đã Sửa"
        WebElement rowA1Edited = driver.findElement(
                By.xpath("//table//tr[td[2][normalize-space()='A1-Đã Sửa']]")
        );

        // Nhấn nút "Sửa"
        WebElement editBtn = rowA1Edited.findElement(
                By.xpath(".//a[@title='Sửa']")
        );
        editBtn.click();
        Thread.sleep(800);

        // 3. Đổi TÊN sang "A2" (đã tồn tại)
        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.clear();
        nameInput.sendKeys("A2");

        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(1500);

        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL sau khi đổi tên trùng A2 = " + currentUrl);

        // vẫn đang đứng ở form (good), KHÔNG quay về /admin/buildings list
        boolean stayOnForm =
                currentUrl.contains("/admin/buildings/save")
                || currentUrl.contains("/admin/buildings/edit");

        Assert.assertTrue(
                stayOnForm,
                "Tên tòa nhà trùng -> phải đứng lại form (URL /admin/buildings/save hoặc /edit), không được redirect về danh sách."
        );

        //  có thông báo lỗi (alert-danger) về trùng tên
        WebElement errorAlert = driver.findElement(
                By.cssSelector(".alert.alert-danger")
        );
        String errorText = errorAlert.getText().trim();
        System.out.println("Thông báo lỗi khi trùng tên: " + errorText);

        String errorLower = errorText.toLowerCase();

        // Chỉ cần message có "tên tòa nhà" + "đã được sử dụng / đã tồn tại / trùng"
        boolean msgOk =
                errorLower.contains("tên tòa nhà")
                && (
                        errorLower.contains("đã được sử dụng")
                        || errorLower.contains("được sử dụng cho tòa nhà khác")
                        || errorLower.contains("đã tồn tại")
                        || errorLower.contains("trùng")
                );

        Assert.assertTrue(
                msgOk,
                "Thông báo lỗi phải nói rõ tên tòa nhà đã được sử dụng / bị trùng."
        );
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
