package offer.compass.amazondeal.services.MultiThreading;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.entities.DealOfTheDay;
import offer.compass.amazondeal.helpers.AmazonPrimeDealHelper;
import org.openqa.selenium.WebDriver;

import java.util.List;

@Slf4j
public class GetPrimeUrlsByDeptTask extends Thread {

    private String dept;
    private WebDriver browser;
    private AmazonPrimeDealHelper primeDealHelper;

    public GetPrimeUrlsByDeptTask(String department, AmazonPrimeDealHelper primeDealHelper) {
        this.dept = department;
        this.primeDealHelper = primeDealHelper;
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            //get browser and open amazon todays deal url
            browser = primeDealHelper.openBrowser(false);
            //click filters
            primeDealHelper.clickFilters(browser);
            //click the department
            boolean isClicked = primeDealHelper.findAndClickSingleDepartment(browser, dept);
            //save Urls in today's deal entity
            if (isClicked) {
                //get main urls
                List<String> mainUrls = primeDealHelper.getAllPagesPrimeUrls(browser);
                //get individual urls and save in entities
                for (String mainUrl: mainUrls) {
                    browser.get(mainUrl);
                    Thread.sleep(2500);
                    List<DealOfTheDay> dealOfTheDayEntities = primeDealHelper
                            .fetchSingleTabProductUrls(browser, true);
                    log.info("found {} entities", dealOfTheDayEntities.size());
                    dealOfTheDayEntities.forEach(dealOfTheDay -> primeDealHelper.saveDOTDEntity(dealOfTheDay));
                    log.info("Total saved entities so far is {}", primeDealHelper.getTotalCountOfPrimeDeals());
                }
            }
        } catch (Exception ex) {
            //close the browser
            browser.quit();
            throw new Exception("Error occurred " + ex.getMessage());
        }
    }

}
