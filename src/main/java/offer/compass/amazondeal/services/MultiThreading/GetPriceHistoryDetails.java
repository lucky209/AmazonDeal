package offer.compass.amazondeal.services.MultiThreading;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.helpers.BrowserHelper;
import offer.compass.amazondeal.helpers.PriceHistoryHelper;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class GetPriceHistoryDetails extends Thread {

    private WebDriver browser;
    private BrowserHelper browserHelper;
    private List<String> batchUrls;
    private PriceHistoryHelper priceHistoryHelper;


    public GetPriceHistoryDetails(BrowserHelper browserHelper, List<String> batchUrls, PriceHistoryHelper priceHistoryHelper) {
        this.browserHelper = browserHelper;
        this.batchUrls = batchUrls;
        this.priceHistoryHelper = priceHistoryHelper;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        browser = browserHelper.openBrowser(true);
        IntStream.range(1,batchUrls.size()).forEach(count -> ((JavascriptExecutor) browser)
                .executeScript(Constants.NEW_TAB_SCRIPT));
        List<String> tabs = new ArrayList<>(browser.getWindowHandles());
        for (int i=0; i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            priceHistoryHelper.searchWithUrl(browser, batchUrls.get(i));
        }
        //now back to 1st tab and collect all details and so on
        for (int i=0; i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            try {
                priceHistoryHelper.savePriceHistoryDetails(browser, batchUrls.get(i));
            } catch (Exception e) {
                try {
                    //one time retry
                    priceHistoryHelper.searchWithUrl(browser, batchUrls.get(i));
                    priceHistoryHelper.savePriceHistoryDetails(browser, batchUrls.get(i));
                } catch (Exception ex) {
                    log.info("Exception occurred. Exception is " + ex.getMessage());
                }
            }
        }
        browser.quit();
    }
}
