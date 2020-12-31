package offer.compass.amazondeal.controllers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.entities.Department;
import offer.compass.amazondeal.services.AmazonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
public class AmazonController {

    @Autowired
    private AmazonService amazonService;

    @GetMapping("amazon/load-departments")
    public List<Department> loadDepartments() throws InterruptedException {
        log.info("::: Request received to load departments");
        return amazonService.loadDepartments();
    }

    @GetMapping("amazon/get-urls-by-dept")
    public boolean getProductsByDepartment() throws Exception {
        log.info("::: Request received to get products by departments");
        return amazonService.getUrlsByDepartment();
    }

    @GetMapping("/price-history/get-prices-by-urls")
    public boolean getPriceHistoryByUrls() throws InterruptedException, IOException {
        log.info("::: Request received to get price history by urls");
        return amazonService.getPriceHistoryByUrls();
    }
}
