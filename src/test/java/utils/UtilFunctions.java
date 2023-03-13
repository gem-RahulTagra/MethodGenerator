package utils;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.SerenityActions;
import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.webdriver.SerenityWebdriverManager;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.util.ArrayList;

public class UtilFunctions extends PageObject {

    public static boolean isFileDownloaded(String fileName, int timeoutSeconds) throws InterruptedException {
        WebDriver driver = SerenityWebdriverManager.inThisTestThread().getCurrentDriver();
        File dir = new File(System.getProperty("user.home") + "/Downloads");
        File[] files = dir.listFiles();
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < (timeoutSeconds * 1000)) {
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(fileName)) {
                    return true;
                }
            }
            driver.manage().timeouts().wait(10000);
        }
        return false;
    }

    public static void switchToTab(String tabName) {
        WebDriver driver = SerenityWebdriverManager.inThisTestThread().getCurrentDriver();
        String currentHandle = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
            String title = driver.getTitle();
            if (title.contains(tabName)) {
                return;
            }
        }
        driver.switchTo().window(currentHandle);
    }

    public static void switchToTab(int tabIndex) {
        WebDriver driver = SerenityWebdriverManager.inThisTestThread().getCurrentDriver();
        String currentHandle = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(currentHandle)) {
                driver.switchTo().window(handle);
                if (--tabIndex == 0) {
                    break;
                }
            }
        }
    }

    public static void closeCurrentTab() {
        WebDriver driver = SerenityWebdriverManager.inThisTestThread().getCurrentDriver();
        driver.close();
    }

    public static void hoverOverElement(WebElementFacade element) {
        Actions action = new Actions(Serenity.getDriver());
        WebElement webElement = element.getElement();
        action.moveToElement(webElement).perform();
    }

    public void rightClick(By locator) {
        try {
            $(locator).contextClick();
        } catch (Exception e) {
            Assert.fail("Could not do right click on element " + $(locator));
//            LOG.info("Could not do right click on element " + $(locator));
        }
    }

    public void rightClick(WebElementFacade element) {
        try {
            element.contextClick();
        } catch (Exception e) {
            Assert.fail("Could not do right click on element: " + element);
//            LOG.info("Could not do right click on element: " + element);
        }
    }

    public void acceptAlert() {
        try {
            getDriver().switchTo().alert().accept();
        } catch (Exception e) {
            Assert.fail("Could not do accept alert");
//            LOG.info("Could not do accept alert");
        }
    }

    public void scrollToElement(By locator) {
        try {
            ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", $(locator));
        } catch (Exception e) {
            Assert.fail("Could not scroll to element: " + $(locator));
//            LOG.info("Could not scroll to element: " + $(locator));
        }
    }

    public void scrollToElement(WebElementFacade element) {
        try {
            ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
        } catch (Exception e) {
            Assert.fail("Could not scroll to element: " + element);
//            LOG.info("Could not scroll to element: " + element);
        }
    }

    public void dragAndDrop(WebElementFacade fromElement, WebElementFacade toElement) {
        try {
            new SerenityActions(getDriver()).dragAndDrop(fromElement, toElement).build().perform();
        } catch (Exception e) {
            Assert.fail("Could not drag element: " + fromElement + " to: " + toElement);
//            LOG.info("Could not drag element: " + fromElement + " to: " + toElement);
        }
    }

    public void dragAndDrop(By fromElement, By toElement) {
        try {
            new SerenityActions(getDriver()).dragAndDrop($(fromElement), $(toElement)).build().perform();
        } catch (Exception e) {
            Assert.fail("Could not drag element: " + fromElement + " to: " + toElement);
//            LOGGER.info("Could not drag element: " + fromElement + " to: " + toElement);
        }
    }
    public void verifyTitle(String expectedTitle) {
        String title = getDriver().getTitle();
        if (!title.equals(expectedTitle)) {
            Assert.fail("Title of page does not match. Expected Title: " + expectedTitle + " Actual Title: " + title);
//            LOG.info("Title of page does not match. Expected Title: " + expectedTitle + " Actual Title: " + title);
        }
    }public void noOfTabs(String expectedTitle) {
        ArrayList<String> tabs = new ArrayList<String>(getDriver().getWindowHandles());
//        LOG.info("No of tabs: " + tabs.size());
    }

    public void changeFocusOfElement(WebElementFacade elementFacade)
    {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("arguments[0].focus();", elementFacade);
    }


    public void refreshPage()
    {
        getDriver().navigate().refresh();
//        LOG.info("Refresh Page");
    }

    public void sendInputToAlert(String inputText) {
        try
        {
            getDriver().switchTo().alert().sendKeys(inputText);
        }
        catch (Exception e)
        {
            Assert.fail("Could not enter text for alert");
//            LOG.info("Could not enter text for alert");
        }
    }


}
