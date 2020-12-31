package offer.compass.amazondeal.services.MultiThreading;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.entities.TodaysDealUrl;
import offer.compass.amazondeal.entities.TodaysDealUrlRepo;
import offer.compass.amazondeal.helpers.BrowserHelper;
import offer.compass.amazondeal.services.ProdService;
import offer.compass.amazondeal.services.TodaysDealService;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;

@Slf4j
public class GetUrlsByDeptTask {

    private BrowserHelper browserHelper;
    private ProdService prodService;
    private TodaysDealService todaysDealService;
    private TodaysDealUrlRepo todaysDealUrlRepo;
    private WebDriver browser;


    public GetUrlsByDeptTask(BrowserHelper browserHelper, ProdService prodService,
                             TodaysDealService todaysDealService, TodaysDealUrlRepo todaysDealUrlRepo) {
        this.browserHelper = browserHelper;
        this.prodService = prodService;
        this.todaysDealService = todaysDealService;
        this.todaysDealUrlRepo = todaysDealUrlRepo;
    }

    public void getUrlsProcess(String deptName) throws Exception {
        try {
            //get browser
            browser = browserHelper.openBrowser(false, AmazonConstants.TODAYS_DEAL_URL);
            //click filters
            prodService.clickFilters(browser);
            //click the department
            boolean isClicked = todaysDealService.findAndClickSingleDepartment(browser, deptName);
            //save Urls in today's deal entity
            if (isClicked) {
                HashMap<String, Integer> urlsAndPriceMap = todaysDealService.getAllPagesUrlsAndPrice(browser);
                log.info("::: Total urls found in the department " + deptName + " is " + urlsAndPriceMap.size());
                //save products
                urlsAndPriceMap.forEach((url,price) -> this.saveTodaysDealUrls(url, price, deptName));
            }
        } catch (Exception ex) {
            //close the browser
            browser.quit();
            throw new Exception("Error occurred " + ex.getMessage());
        }
        //reset the count
        AmazonConstants.TODAYS_DEAL_PRODUCTS_COUNT = 0;
    }

    private void saveTodaysDealUrls(String url, Integer price, String department) {
        TodaysDealUrl todaysDealUrl = new TodaysDealUrl();
        todaysDealUrl.setUrl(url);
        todaysDealUrl.setDept(department);
        todaysDealUrl.setPrice(price);
        todaysDealUrlRepo.save(todaysDealUrl);
    }

    public void tearDown() {
        browser.quit();
    }
}
