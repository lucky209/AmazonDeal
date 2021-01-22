package offer.compass.amazondeal.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
import offer.compass.amazondeal.constants.PropertyConstants;
import offer.compass.amazondeal.entities.*;
import offer.compass.amazondeal.helpers.AmazonDealHelper;
import offer.compass.amazondeal.helpers.BrowserHelper;
import offer.compass.amazondeal.helpers.PriceHistoryHelper;
import offer.compass.amazondeal.services.MultiThreading.GetPriceHistoryDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PriceHistoryServiceImpl implements PriceHistoryService {

    @Autowired
    private TodaysDealUrlRepo todaysDealUrlRepo;
    @Autowired
    private PropertiesRepo propertiesRepo;
    @Autowired
    private AmazonDealHelper amazonDealHelper;
    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private PriceHistoryHelper priceHistoryHelper;
    @Autowired
    private DealOfTheDayRepo dealOfTheDayRepo;

    @Override
    public boolean getPriceHistoryByUrls() throws InterruptedException {
        List<String> urls;
        boolean isDOTDEnabled = propertiesRepo.findByPropName(PropertyConstants.DOTD_ENABLED).isEnabled();
        if (isDOTDEnabled) {
            //get urls from dotd table
            urls = dealOfTheDayRepo.findAll().stream().map(DealOfTheDay::getUrl).collect(Collectors.toList());
            log.info("Fetched {} urls from the DOTD table", urls.size());
        } else {
            //get urls from todays deal table
            List<TodaysDealUrl> todaysDealUrlList = todaysDealUrlRepo.findAll();
            urls = todaysDealUrlList.stream().map(TodaysDealUrl::getUrl).collect(Collectors.toList());
            log.info("Fetched {} urls from the Today's deal table", urls.size());
        }
        int searchPerPage = Integer.parseInt(propertiesRepo.findByPropName(
                PropertyConstants.PRICE_HISTORY_SEARCH_PER_PAGE).getPropValue());
        searchPerPage = Math.min(urls.size(), searchPerPage);
        int maxThreads = amazonDealHelper.getMaxThreads(searchPerPage, urls.size());
        if (urls.size() > 0) {
            ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
            for (List<String> batchUrls : Lists.partition(urls, searchPerPage)) {
                Thread thread = new GetPriceHistoryDetails(
                        browserHelper, batchUrls, priceHistoryHelper, isDOTDEnabled);
                pool.execute(thread);
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.HOURS);
            log.info("Completed the getPriceHistoryByUrls process...");
            log.info("Total today's deal processed urls " + PriceHistoryConstants.URLS_PROCESSED);
            PriceHistoryConstants.URLS_PROCESSED = 0;
            priceHistoryHelper.getDescriptionString();
            return true;
        }
        log.info("No urls found to get details");
        return false;
    }
}
