package com.group7.DMS.Epic8;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class U81 {
	WebDriver driver;
    WebDriverWait wait;

   

    @BeforeClass
    public void setup() {

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("http://localhost:8080/DMS/login");
        
        driver.findElement(By.name("username")).sendKeys("4551050111");
        driver.findElement(By.name("password")).sendKeys("thao123");
        driver.findElement(By.xpath("/html/body/div/form/button")).click();
        
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    @BeforeMethod
    public void goToContractPage() {
        driver.get("http://localhost:8080/DMS/admin/contracts"); 
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }



  
    @Test
    public void TC_8_1_06_FilterByStudent() throws Exception {
        // 1. Chọn sinh viên từ dropdown (hoặc autocomplete)
        // Giả sử đây là thẻ <select> có id="filter-student"
        WebElement studentSelect = driver.findElement(By.name("studentId")); 
        Select select = new Select(studentSelect);
        
        // Chọn theo text hiển thị (Visible Text)
        select.selectByVisibleText("Nguyễn Thanh Thảo - 4551050199");
        
        // 2. Nhấn nút lọc
        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[4]/button")).click();

        Thread.sleep(3000);

        List<WebElement> cells = driver.findElements(By.xpath("/html/body/div[2]/div[2]/div[2]/div[2]/table"));

        Assert.assertEquals(cells.get(0).getText().trim(), "8", "Sai ID");
        Assert.assertTrue(cells.get(1).getText().contains("Nguyễn Thanh Thảo"), "Sai tên SV");
        Assert.assertEquals(cells.get(2).getText().trim(), "C2 - 201", "Sai Phòng");
        Assert.assertEquals(cells.get(5).getText().trim(), "2,700,000 VNĐ", "Sai Phí");
        Assert.assertEquals(cells.get(6).getText().trim(), "ACTIVE", "Sai Trạng thái");
    }

    @Test
    public void TC_8_1_07_FilterByRoom() {
     
        WebElement roomSelect = driver.findElement(By.name("roomId"));
        Select select = new Select(roomSelect);
        select.selectByVisibleText("C2 - 201");

        // 2. Nhấn nút lọc
        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[4]/button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tbody//tr")));

        // 3. Verify danh sách ID trả về phải có 10, 9, 8
        List<WebElement> idCells = driver.findElements(By.xpath("//table//tbody//tr/td[1]")); // Cột ID
        List<String> actualIds = new ArrayList<>();
        for (WebElement cell : idCells) {
            actualIds.add(cell.getText().trim());
        }

        List<String> expectedIds = Arrays.asList("10", "9", "8");
        Assert.assertTrue(actualIds.containsAll(expectedIds), "Kết quả lọc phòng thiếu ID mong đợi. Thực tế: " + actualIds);
    }

    @Test
    public void TC_8_1_08_FilterStatusActive() {
        filterAndVerifyStatus("ACTIVE");
    }

    @Test
    public void TC_8_1_09_FilterStatusInactive() {
        filterAndVerifyStatus("INACTIVE");
    }


    @Test
    public void TC_8_1_10_FilterStatusTerminated() {
        filterAndVerifyStatus("TERMINATED"); 
    }

    // Hàm hỗ trợ dùng chung cho test lọc trạng thái
    private void filterAndVerifyStatus(String statusToTest) {
        // 1. Chọn trạng thái
        WebElement statusSelect = driver.findElement(By.name("status")); 
        Select select = new Select(statusSelect);
        select.selectByVisibleText(statusToTest); 

        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[4]/button")).click();
        
     
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tbody//tr")));

      
        List<WebElement> statusCells = driver.findElements(By.xpath("//table//tbody//tr/td[7]")); // Giả sử cột trạng thái là cột số 7
        
        for (WebElement cell : statusCells) {
            String currentStatus = cell.getText().trim();
            Assert.assertEquals(currentStatus, statusToTest, "Phát hiện dòng có trạng thái sai!");
        }
    }
}
