package offer.compass.amazondeal.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
import offer.compass.amazondeal.constants.PropertyConstants;
import offer.compass.amazondeal.entities.*;
import offer.compass.amazondeal.helpers.AmazonDealHelper;
import offer.compass.amazondeal.helpers.BrowserHelper;
import offer.compass.amazondeal.helpers.PriceHistoryHelper;
import offer.compass.amazondeal.services.MultiThreading.GetPriceHistoryDetails;
import offer.compass.amazondeal.services.MultiThreading.GetTodaysDealUrlsByDeptThread;
import offer.compass.amazondeal.services.MultiThreading.GetUrlsByDeptTask;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AmazonServiceImpl implements AmazonService {

    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private TodaysDealService todaysDealService;
    @Autowired
    private ProdService prodService;
    @Autowired
    private PropertiesRepo propertiesRepo;
    @Autowired
    private TodaysDealUrlRepo todaysDealUrlRepo;
    @Autowired
    private AmazonDealHelper amazonDealHelper;
    @Autowired
    private PriceHistoryHelper priceHistoryHelper;


    @Override
    public List<Department> loadDepartments() throws InterruptedException {
        WebDriver browser = browserHelper.openBrowser(false, AmazonConstants.TODAYS_DEAL_URL);
        return todaysDealService.loadDepartments(browser);
    }

    @Override
    @Transactional
    public boolean getUrlsByDepartment() throws InterruptedException {
        //delete last run records
        amazonDealHelper.deleteTodaysDealAllRecords();
        //initialize variables
        int maxThreads = Integer.parseInt(propertiesRepo.findByPropName(PropertyConstants.POOL_SIZE).getPropValue());
        List<String> departments = amazonDealHelper.getScheduledDepartments().stream()
                .map(ScheduledDepartment::getDept).collect(Collectors.toList());
        //limiting the threads
        ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
        for (String department : departments) {
            Thread thread = new GetTodaysDealUrlsByDeptThread(new GetUrlsByDeptTask(
                    browserHelper, prodService, todaysDealService,
                    todaysDealUrlRepo), department);
            pool.execute(thread);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.HOURS);
        log.info("Completed the getUrlsByDepartment process...");
        log.info("Total today's deal urls are " + todaysDealUrlRepo.findAll().size());
        return true;
    }

    @Override
    @Transactional
    public boolean getPriceHistoryByUrls() throws InterruptedException {
        //get urls from todays deal table
        List<TodaysDealUrl> todaysDealUrlList = todaysDealUrlRepo.findAll();
        List<String> urls = todaysDealUrlList.stream().map(TodaysDealUrl::getUrl).collect(Collectors.toList());
        int searchPerPage = Integer.parseInt(propertiesRepo.findByPropName(
                PropertyConstants.PRICE_HISTORY_SEARCH_PER_PAGE).getPropValue());
        searchPerPage = Math.min(urls.size(), searchPerPage);
        int maxThreads = amazonDealHelper.getMaxThreads(searchPerPage, urls.size());
        if (urls.size() > 0) {
            ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
            for (List<String> batchUrls : Lists.partition(urls, searchPerPage)) {
                Thread thread = new GetPriceHistoryDetails(
                        browserHelper, batchUrls, priceHistoryHelper);
                pool.execute(thread);
            }
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.HOURS);
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
