package offer.compass.amazondeal.schedulers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.entities.PriceHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class CleanupScheduler {

    @Autowired
    private PriceHistoryRepo priceHistoryRepo;

    public void cleanupPriceHistoryEntities() {
        log.info(":: Cleanup price history entities scheduler is started at " + LocalDateTime.now());
        priceHistoryRepo.deleteAllRecords();
        log.info(":: Cleanup price history entities is done successfully at " + LocalDateTime.now());
    }
}
