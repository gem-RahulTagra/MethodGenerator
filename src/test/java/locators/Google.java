package locators;

import annotation.LocatorType;
import net.thucydides.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Google extends PageObject {

    @LocatorType(value="input")
    public static By firstName= By.xpath("//*[@id='fname']");


    @LocatorType(value="input")
    public static By password=By.id("password");

}