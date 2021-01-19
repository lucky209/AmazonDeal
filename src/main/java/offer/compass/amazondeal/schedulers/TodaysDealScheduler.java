package offer.compass.amazondeal.schedulers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.helpers.AmazonDealHelper;
import offer.compass.amazondeal.services.AmazonServiceImpl;
import offer.compass.amazondeal.services.PriceHistoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
public class TodaysDealScheduler {

    @Autowired
    private AmazonServiceImpl amazonService;
    @Autowired
    private PriceHistoryServiceImpl priceHistoryService;
    @Autowired
    private AmazonDealHelper amazonDealHelper;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void getTodaysDealProducts() throws Exception {
        log.info(":: Today's deal scheduler is started at " + LocalDateTime.now());
        //clean up if it is SUNDAY
        amazonDealHelper.deleteAllPriceHistoryRecords();
        //1.get all today's deal urls
        amazonService.getUrlsByDepartment();
        log.info("Get urls by departments are done successfully at " + LocalDateTime.now());
        //2.get price history details
        priceHistoryService.getPriceHistoryByUrls();
        log.info("Get price history urls are done successfully at " + LocalDateTime.now());
        log.info(":: Today's deal scheduler task is completed at " + LocalDateTime.now());
    }
}
