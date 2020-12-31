package offer.compass.amazondeal.schedulers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.services.AmazonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class TodaysDealScheduler {

    @Autowired
    private AmazonServiceImpl amazonService;

    public void getTodaysDealProducts() throws Exception {
        log.info(":: Get today's deal scheduler is started at " + LocalDateTime.now());
        //1.load departments
        amazonService.loadDepartments();
        log.info("Departments loaded successfully at "+ LocalDateTime.now());
        //2.get all today's deal urls
        amazonService.getUrlsByDepartment();
        log.info("Get urls by departments are done successfully at " + LocalDateTime.now());
        //3.get price history details
        amazonService.getPriceHistoryByUrls();
        log.info("Get price history urls are done successfully at " + LocalDateTime.now());
        log.info(":: Today's deal scheduler task is completed at " + LocalDateTime.now());
    }
}
