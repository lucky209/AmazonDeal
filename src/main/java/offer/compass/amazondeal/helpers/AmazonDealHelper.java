package offer.compass.amazondeal.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.PropertyConstants;
import offer.compass.amazondeal.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.List;

@Component
@Slf4j
public class AmazonDealHelper {

    @Autowired
    private PropertiesRepo propertiesRepo;
    @Autowired
    private TodaysDealUrlRepo todaysDealUrlRepo;
    @Autowired
    private PriceHistoryRepo priceHistoryRepo;
    @Autowired
    private ScheduledDepartmentRepo scheduledDepartmentRepo;
    @Autowired
    private DealOfTheDayRepo dealOfTheDayRepo;

    public int getMaxThreads(int searchPerPage, int totalUrls) {
        if (totalUrls > searchPerPage)
            return Integer.parseInt(propertiesRepo.findByPropName(PropertyConstants.PRICE_HISTORY_POOL_SIZE).getPropValue());
        else
            return 1;
    }

    public List<ScheduledDepartment> getScheduledDepartments() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return scheduledDepartmentRepo.findByDayNo(day);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteTodaysDealAllRecords() {
        todaysDealUrlRepo.deleteAllRecords();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteAllPriceHistoryRecords() {
        Calendar calendar = Calendar.getInstance();
        if (Calendar.SUNDAY == calendar.get(Calendar.DAY_OF_WEEK)) {
            priceHistoryRepo.deleteAllRecords();
            log.info("All price history urls are deleted...");
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteDealOfTheDayAllRecords() {
        dealOfTheDayRepo.deleteAllNonPrimeRecords();
    }
}
