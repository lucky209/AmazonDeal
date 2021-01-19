package offer.compass.amazondeal.helpers;

import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.entities.DealOfTheDay;
import offer.compass.amazondeal.entities.DealOfTheDayRepo;
import offer.compass.amazondeal.services.ProdService;
import offer.compass.amazondeal.services.TodaysDealService;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
public class AmazonPrimeDealHelper {

    @Autowired
    private DealOfTheDayRepo dealOfTheDayRepo;
    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private ProdService prodService;    
    @Autowired
    private TodaysDealService todaysDealService;
    @Autowired
    private AmazonDealOfTheDayHelper amazonDealOfTheDayHelper;

    public void saveDOTDEntity(DealOfTheDay dealOfTheDay) {
        dealOfTheDayRepo.save(dealOfTheDay);
    }

    public int getTotalCountOfPrimeDeals() {
        return dealOfTheDayRepo.countOfPrimeDeals();
    }

    public WebDriver openBrowser(boolean maximize) throws InterruptedException {
        return browserHelper.openBrowser(maximize, AmazonConstants.TODAYS_DEAL_URL);
    }

    public void clickFilters(WebDriver browser) throws Exception {
        prodService.clickFilters(browser);
    }

    public boolean findAndClickSingleDepartment(WebDriver browser, String dept) throws InterruptedException {
        return todaysDealService.findAndClickSingleDepartment(browser, dept);
    }

    public List<String> getAllPagesPrimeUrls(WebDriver browser) throws InterruptedException {
        return todaysDealService.getAllPagesPrimeUrls(browser);
    }

    public List<DealOfTheDay> fetchSingleTabProductUrls(WebDriver browser, boolean isPrime) throws Exception {
        return amazonDealOfTheDayHelper
                .fetchSingleTabProductUrls(browser, isPrime);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<DealOfTheDay> findAllPrimeDealUrls() {
        return dealOfTheDayRepo.findAllPrimeDealUrls();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteTodaysDealPrimeExclusiveRecords() {
        dealOfTheDayRepo.deleteAllPrimeRecords();
    }
}
