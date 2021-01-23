package offer.compass.amazondeal.services.MultiThreading;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
import offer.compass.amazondeal.entities.PriceHistory;
import offer.compass.amazondeal.helpers.BrowserHelper;
import offer.compass.amazondeal.helpers.PriceHistoryHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class PriceHistoryScreenshot extends Thread {

    private List<PriceHistory> priceHistoryList;
    private BrowserHelper browserHelper;
    private PriceHistoryHelper priceHistoryHelper;

    public PriceHistoryScreenshot(List<PriceHistory> priceHistoryList,
                                  BrowserHelper browserHelper, PriceHistoryHelper priceHistoryHelper) {
        this.priceHistoryList = priceHistoryList;
        this.browserHelper = browserHelper;
        this.priceHistoryHelper = priceHistoryHelper;
    }

    @SneakyThrows
    @Override
    public void run() {
        //open tabs
        WebDriver browser = browserHelper.openBrowser(true);
        IntStream.range(1,priceHistoryList.size()).forEach(count -> ((JavascriptExecutor) browser)
                .executeScript(Constants.NEW_TAB_SCRIPT));
        log.info("Opened {} tabs", priceHistoryList.size());
        //get urls
        List<String> tabs = new ArrayList<>(browser.getWindowHandles());
        for (int i=0;i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            browser.get(priceHistoryList.get(i).getUrl());
            Thread.sleep(1000);
        }
        //draw borders
        for (int i=0;i<tabs.size();i++) {
            PriceHistory priceHistory = priceHistoryList.get(i);
            browser.switchTo().window(tabs.get(i));
            priceHistoryHelper.drawBorderById(browser, AmazonConstants.TODAYS_DEAL_REGULAR_PRICE_DIV_ID);
            priceHistoryHelper.drawBorderById(browser, AmazonConstants.TODAYS_DEAL_REVIEW_ID);
            priceHistoryHelper.drawBorderLowestAndHighestPriceElement(browser,
                    priceHistory.getLowestPrice(), priceHistory.getHighestPrice());
            Thread.sleep(250);
        }
        //process urls
        for (int i=0;i<tabs.size();i++) {
            PriceHistory priceHistory = priceHistoryList.get(i);
            browser.switchTo().window(tabs.get(i));
            try {
                //4.Now take SS
                String dept = this.getDepartment(browser, priceHistory.isDotd());
                priceHistoryHelper.takeAmazonProductScreenShot(browser, dept ,
                        priceHistory.getProductName());
                Thread.sleep(500);
            } catch (Exception ex) {
                log.info("Exception occurred for the url " + browser.getCurrentUrl());
                //todo create exception handling
            }
            PriceHistoryConstants.SCREENSHOT_PROCESSED++;
        }
        log.info("Total processed screenshots are " + PriceHistoryConstants.SCREENSHOT_PROCESSED);
        log.info("Quitting the browser...");
        browser.quit();
    }

    private String getDepartment(WebDriver browser, boolean dotd) {
        boolean isEleAvail = !browser.findElements(By.cssSelector(PriceHistoryConstants.DEPT_CSS_CLASS)).isEmpty();
        if (isEleAvail) {
            if (dotd)
                return "DOTD-" + this.constructDeptPath(browser);
            else
                return this.constructDeptPath(browser);
        }
        return "Unknown";
    }

    private String constructDeptPath(WebDriver browser) {
        List<WebElement> deptElements = browser.findElement(By.cssSelector(PriceHistoryConstants.DEPT_CSS_CLASS))
                .findElements(By.tagName(Constants.TAG_LI));
        StringBuilder deptPath = new StringBuilder();
        if (deptElements.size() > 3) {
            for (int i=0;i<3;i++) {
                String subDept = deptElements.get(i).getText().trim();
                if(!subDept.equals("›")) {
                    deptPath.append(subDept);
                    if (i!=2) {
                        deptPath.append("\\");
                    }
                }
            }
        } else {
            for (int i = 0; i < deptElements.size(); i++) {
                String subDept = deptElements.get(i).getText().trim();
                if (!subDept.equals("›")) {
                    deptPath.append(subDept);
                    if (i != (deptElements.size() - 1)) {
                        deptPath.append("\\");
                    }
                }
            }
        }
        return deptPath.toString();
    }
}
