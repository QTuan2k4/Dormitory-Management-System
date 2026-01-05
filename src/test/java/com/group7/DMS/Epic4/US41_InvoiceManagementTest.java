package com.group7.DMS.Epic4;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class US41_InvoiceManagementTest {

    WebDriver driver;
    WebDriverWait wait;

    //SETUP
    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        loginAsAdmin();
    }

    private void slow() throws InterruptedException {
        Thread.sleep(1500); 
    }

    // LOGIN 
    public void loginAsAdmin() {
        driver.get("http://localhost:8080/DMS/login");
        driver.findElement(By.id("username")).sendKeys("1");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }
    private void selectBuilding(String buildingName) throws InterruptedException {

        // 1. Chờ element tồn tại trong DOM
        WebElement buildingInput =
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.name("buildingId")));

        // 2. Scroll tới element
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", buildingInput);
        Thread.sleep(1000);

        // 3. Click bằng JS (ổn định hơn click thường)
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", buildingInput);

        // 4. Chờ option hiện ra
        WebElement option =
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[normalize-space(text())='" + buildingName + "']")));

        Thread.sleep(500);

        // 5. Click option bằng JS
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", option);

        Thread.sleep(1000);
    }


    //TC_9.1.01
    @Test(priority = 1)
    public void TC_9_1_01_verifyCreateInvoiceButton() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices");
        slow();

        WebElement btnCreate =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.linkText("Tạo Hóa Đơn")));

        Assert.assertTrue(btnCreate.isDisplayed(),
                "Không hiển thị nút Tạo hóa đơn");
    }

    //TC_9.1.02
    @Test(priority = 2)
    public void TC_9_1_02_verifyBulkCreateButton() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices");
        slow();

        WebElement btnBulk =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.linkText("Tạo Hàng Loạt")));

        Assert.assertTrue(btnBulk.isDisplayed(),
                " Không hiển thị nút Tạo hóa đơn hàng loạt");
    }

    //TC_9.1.03 
    @Test(priority = 3)
    public void TC_9_1_03_verifyBuildingListOnCreateInvoice() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices/select-room");
        slow();

        List<WebElement> buildings =
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector(".card h5")));

        Assert.assertTrue(buildings.size() > 0,
                "Không hiển thị danh sách tòa nhà");
    }

    //TC_9.1.04
    @Test(priority = 4)
    public void TC_9_1_04_verifyRoomListAfterSelectBuildingC5() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices/select-room");

        WebElement buildingC5 =
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//h5[contains(text(),'C5')]")));

        slow();
        buildingC5.click();

        List<WebElement> rooms =
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector(".room-card")));

        Assert.assertTrue(rooms.size() > 0,
                "❌ Không hiển thị danh sách phòng của tòa C5");
    }

    //TC_9.1.05
    @Test(priority = 5)
    public void TC_9_1_05_verifyInvoiceFormAfterSelectRoom() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices/select-room");

        WebElement buildingC5 =
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//h5[contains(text(),'C5')]")));
        buildingC5.click();

        WebElement room =
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".room-card")));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", room);
        Thread.sleep(1000);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", room);

        WebElement electricity =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.name("electricityUsage")));
        WebElement water =
                driver.findElement(By.name("waterUsage"));

        Assert.assertTrue(electricity.isDisplayed());
        Assert.assertTrue(water.isDisplayed());
    }

    //TC_9.1.06
    @Test(priority = 6)
    public void TC_9_1_06_verifyBulkCreateFormDisplayed() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices/bulk-create");
        slow();

        WebElement submitBtn =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("button[type='submit']")));

        Assert.assertTrue(submitBtn.isDisplayed(),
                "❌ Không hiển thị form tạo hóa đơn hàng loạt");
    }

    //  TC_9.1.07 
    @Test(priority = 7)
    public void TC_9_1_07_verifyBulkCreateInvoiceSuccess() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices/bulk-create");

        selectBuilding("C5");

        driver.findElement(By.name("month")).sendKeys("3");
        driver.findElement(By.name("year")).sendKeys("2026");

        driver.findElement(By.name("confirm")).click();
        Thread.sleep(800);

        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/div[1]/div[2]/form/div[5]/button")).click();

        WebElement successMsg =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.className("alert-success")));

        Assert.assertTrue(successMsg.isDisplayed());
    }


    //  TC_9.1.08 
    @Test(priority = 8)
    public void TC_9_1_08_verifyBulkCreateInvoiceDuplicateFail() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices/bulk-create");

        selectBuilding("C5");

        driver.findElement(By.name("month")).sendKeys("1");
        driver.findElement(By.name("year")).sendKeys("2026");

        driver.findElement(By.name("confirm")).click();
        Thread.sleep(800);

        driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/div[1]/div[2]/form/div[5]/button")).click();

        WebElement errorMsg =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.className("alert-danger")));

        Assert.assertTrue(errorMsg.getText().contains("đã tồn tại"));
    }

    // TC_9.1.09
    @Test(priority = 9)
    public void TC_9_1_09_verifyBuildingWithoutRoom() throws InterruptedException {
        driver.get("http://localhost:8080/DMS/admin/invoices/select-room");

        WebElement buildingC6 =
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//h5[contains(text(),'C6')]")));

        slow();
        buildingC6.click();

        WebElement emptyMsg =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(),'Không có phòng')]")));

        Assert.assertTrue(emptyMsg.isDisplayed(),
                " Không hiển thị thông báo tòa không có phòng");
    }

    //  TEARDOWN 
    @AfterMethod
    public void tearDown() throws InterruptedException {
        Thread.sleep(3000);
        driver.quit();
    }
}
