package offer.compass.amazondeal.services.MultiThreading;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.entities.DealOfTheDay;
import offer.compass.amazondeal.entities.DealOfTheDayRepo;
import offer.compass.amazondeal.helpers.BrowserHelper;
import offer.compass.amazondeal.helpers.DOTDHelper;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class GetDealOfTheDayUrls extends Thread {
    private List<String> mainUrls;
    private DOTDHelper dotdHelper;
    private BrowserHelper browserHelper;
    private DealOfTheDayRepo dealOfTheDayRepo;

    public GetDealOfTheDayUrls(List<String> mainUrls, DOTDHelper dotdHelper, BrowserHelper browserHelper, DealOfTheDayRepo dealOfTheDayRepo) {
        this.mainUrls = mainUrls;
        this.dotdHelper = dotdHelper;
        this.browserHelper = browserHelper;
        this.dealOfTheDayRepo = dealOfTheDayRepo;
    }

    @SneakyThrows
    @Override
    public void run() {
        //open tabs
        WebDriver browser = browserHelper.openBrowser(true);
        IntStream.range(1,mainUrls.size()).forEach(count -> ((JavascriptExecutor) browser)
                .executeScript(Constants.NEW_TAB_SCRIPT));
        log.info("Opened {} tabs", mainUrls.size());
        //get urls
        List<String> tabs = new ArrayList<>(browser.getWindowHandles());
        for (int i=0;i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            browser.get(mainUrls.get(i));
        }
        //process urls
        for (String tab : tabs) {
            List<DealOfTheDay> dealOfTheDayList;
            browser.switchTo().window(tab);
            try {
                dealOfTheDayList = dotdHelper.fetchDOTDEntitiesByMainUrl(browser, false);
            } catch (Exception ex) {
                log.info("Exception occurred for the url " + browser.getCurrentUrl());
                log.info("Retrying again");
                try {
                    browser.get(browser.getCurrentUrl());
                    dealOfTheDayList = dotdHelper.fetchDOTDEntitiesByMainUrl(browser, false);
                } catch (Exception e) {
                    log.info("Exception occurred again. Moving to next tab");
                    continue;
                }
            }
            dealOfTheDayList.forEach(dealOfTheDay -> dealOfTheDayRepo.save(dealOfTheDay));
            log.info("Got {} entities in this tab. Saved entities so far is {}",
                    dealOfTheDayList.size(), dealOfTheDayRepo.findAll().size());
        }
        log.info("Quitting the browser...");
        browser.quit();
    }
}
