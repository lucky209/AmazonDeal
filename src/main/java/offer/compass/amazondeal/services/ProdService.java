package offer.compass.amazondeal.services;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProdService {

    @Autowired
    private TodaysDealService todaysDealService;

    public void clickFilters(WebDriver browser) throws Exception {
        log.info("::: Clicking amazon filters...");
        todaysDealService.clickDiscountFilter(browser);
        todaysDealService.clickReviewFilter(browser);
        todaysDealService.clickActiveProducts(browser);
    }
}
