package offer.compass.amazondeal.controllers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.entities.Department;
import offer.compass.amazondeal.services.AmazonService;
import offer.compass.amazondeal.services.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class AmazonController {

    @Autowired
    private AmazonService amazonService;
    @Autowired
    private PriceHistoryService priceHistoryService;

    @GetMapping("amazon/load-departments")
    public List<Department> loadDepartments() throws InterruptedException {
        log.info("::: Request received to load departments");
        return amazonService.loadDepartments();
    }

    @GetMapping("amazon/all-deals")
    public boolean getAllDealsUrlsByDepartment() throws Exception {
        log.info("::: Request received to get products by departments");
        return amazonService.getUrlsByDepartment();
    }

    @GetMapping("/price-history/get-prices-by-urls")
    public boolean getPriceHistoryByUrls() throws InterruptedException {
        log.info("::: Request received to get price history by urls");
        return priceHistoryService.getPriceHistoryByUrls();
    }

    @GetMapping("/amazon/deal-of-the-day")
    public boolean getDealOfTheDayUrls() throws Exception {
        log.info("::: Request received to get deal of the day urls");
        return amazonService.getDealOfTheDayUrls();
    }

    @GetMapping("/amazon/prime-exclusive-deals")
    public boolean getPrimeExclusiveUrls() throws Exception {
        log.info("::: Request received to get deal of the day urls");
        return amazonService.getPrimeExclusiveUrls();
    }
}
