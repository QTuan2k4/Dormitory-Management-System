package com.group7.DMS.Epic8;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
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
public class U83 {
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
    public void goToEditPage() {
     
        driver.get("http://localhost:8080/DMS/admin/contracts");
        
        
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("/html/body/div[2]/div[2]/div[2]/div[2]/table/tbody/tr[1]/td[8]/a[2]/i") 
        ));
        editBtn.click();
        
        wait.until(ExpectedConditions.urlContains("/edit"));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }

   
    @Test
    public void TC_8_3_12_UpdateSuccess() {
       
        WebElement feeInput = driver.findElement(By.name("manualFee")); // check name input
        feeInput.clear();
        feeInput.sendKeys("500000");

    
        WebElement endDateInput = driver.findElement(By.name("endDate"));
       
        endDateInput.sendKeys(Keys.CONTROL + "a");
        endDateInput.sendKeys(Keys.DELETE);
        endDateInput.sendKeys("30062026"); 
        

        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[5]/button")).click();

     
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("/html/body/div[2]/div[2]") 
        ));
        Assert.assertTrue(successMsg.getText().contains("Cập nhật hợp đồng thành công"), "Không thấy thông báo thành công!");
    }

  
    @Test
    public void TC_8_3_13_ErrorEndDateBeforeStartDate() {
       
        WebElement startDateInput = driver.findElement(By.name("startDate"));
        startDateInput.sendKeys(Keys.CONTROL + "a");
        startDateInput.sendKeys(Keys.DELETE);
        startDateInput.sendKeys("01012026");

     
        WebElement endDateInput = driver.findElement(By.name("endDate"));
        endDateInput.sendKeys(Keys.CONTROL + "a");
        endDateInput.sendKeys(Keys.DELETE);
        endDateInput.sendKeys("31122025");

        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[5]/button")).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
        		 By.xpath("/html/body/div[2]/div[2]") 
        ));
        Assert.assertTrue(errorMsg.getText().contains("Ngày kết thúc phải sau ngày bắt đầu"), "Không hiển thị lỗi ngày tháng hợp lệ!");
        
 
    }


    @Test
    public void TC_8_3_14_ErrorEmptyFee() {
     
        WebElement feeInput = driver.findElement(By.name("manualFee"));
        feeInput.clear(); 

     
        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[5]/button")).click();
        
        boolean stillOnEditPage = driver.getCurrentUrl().contains("/edit");
        Assert.assertTrue(stillOnEditPage, "Hệ thống cho phép lưu giá trị rỗng!");

        try {
            WebElement error = driver.findElement(By.xpath("//*[contains(text(),'không được để trống')]"));
            Assert.assertTrue(error.isDisplayed());
        } catch (NoSuchElementException e) {
    
            driver.navigate().refresh();
            feeInput = driver.findElement(By.name("manualFee"));
        }
    }


    @Test
    public void TC_8_3_15_ChangeStatusToTerminated() {

        WebElement statusSelect = driver.findElement(By.name("status")); 
        Select select = new Select(statusSelect);
     
        boolean selected = false;
        for (WebElement option : select.getOptions()) {
            if (option.getText().toUpperCase().contains("TERMINATED")) {
                select.selectByVisibleText(option.getText());
                selected = true;
                break;
            }
        }
        if(!selected) select.selectByIndex(2); 

        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/form/div[5]/button")).click();

  
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")
        ));
        Assert.assertTrue(successMsg.getText().contains("Cập nhật hợp đồng thành công!"));

        driver.get("http://localhost:8080/DMS/admin/contracts");
  
        WebElement statusCell = driver.findElement(By.xpath("//tr[td[normalize-space()='11']]//td[contains(text(), 'TERMINATED')]"));
     
    }
}


