package offer.compass.amazondeal.helpers;

import io.github.bonigarcia.wdm.WebDriverManager;
import offer.compass.amazondeal.constants.PropertyConstants;
import offer.compass.amazondeal.entities.PropertiesRepo;
import org.openqa.selenium.By;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BrowserHelper {

    @Autowired
    private PropertiesRepo propertiesRepo;

    public WebDriver openBrowser(boolean isMaximize) {
        return this.openChromeBrowser(isMaximize);
    }

    public WebDriver openBrowser(boolean isMaximize, String url) throws InterruptedException {
        WebDriver browser = this.openChromeBrowser(isMaximize);
        browser.get(url);
        Thread.sleep(3000);
        return browser;
    }

    public List<WebElement> getWebElemnetsByXpath(WebDriver browser, String xpath) {
        return browser.findElements(By.xpath(xpath));
    }

    private WebDriver openChromeBrowser(boolean isMaximize) {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (isMaximize)
            chromeOptions.addArguments("window-size=1358,727");
        chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        chromeOptions.setHeadless(propertiesRepo.findByPropName(PropertyConstants.HEADLESS_MODE).isEnabled());
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(chromeOptions);
    }
}
