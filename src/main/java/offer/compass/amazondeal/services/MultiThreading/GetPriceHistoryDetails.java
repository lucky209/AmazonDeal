package offer.compass.amazondeal.services.MultiThreading;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
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

    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        browser = browserHelper.openBrowser(true);
        IntStream.range(1,batchUrls.size()).forEach(count -> ((JavascriptExecutor) browser)
                .executeScript(Constants.NEW_TAB_SCRIPT));
        log.info("Opened {} tabs", batchUrls.size());
        List<String> tabs = new ArrayList<>(browser.getWindowHandles());
        for (int i=0; i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            try {
                priceHistoryHelper.searchWithUrl(browser, batchUrls.get(i));
            } catch (InterruptedException e) {
                log.info("Exception occurred. Exception is " + e.getMessage());
                log.info("So retrying the search again...");
                try {
                    priceHistoryHelper.searchWithUrl(browser, batchUrls.get(i));
                } catch (InterruptedException ex) {
                    log.info("Exception occurred again. Exception is " + ex.getMessage());
                    log.info("So continuing with next tab...");
                }
            }
        }
        log.info("Amazon urls searched successfully for {} tabs ", batchUrls.size());
        //now back to 1st tab and collect all details and so on
        for (int i=0; i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            try {
                priceHistoryHelper.savePriceHistoryDetails(browser, batchUrls.get(i));
            } catch (Exception e) {
                try {
                    log.info("Exception occurred. Exception is " + e.getMessage());
                    log.info("So retrying the from the start...");
                    //one time retry
                    priceHistoryHelper.searchWithUrl(browser, batchUrls.get(i));
                    priceHistoryHelper.savePriceHistoryDetails(browser, batchUrls.get(i));
                } catch (Exception ex) {
                    log.info("Exception occurred again. Exception is " + ex.getMessage());
                    log.info("So continuing with next tab...");
                }
            }
        }
        log.info("Processed the urls. Quitting the browser.");
        browser.quit();
        PriceHistoryConstants.URLS_PROCESSED = PriceHistoryConstants.URLS_PROCESSED + batchUrls.size();
        log.info("Today's deal processed urls so far is " + PriceHistoryConstants.URLS_PROCESSED);
    }
}
