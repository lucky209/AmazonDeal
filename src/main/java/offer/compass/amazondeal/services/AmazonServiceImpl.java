package offer.compass.amazondeal.services;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.PropertyConstants;
import offer.compass.amazondeal.entities.*;
import offer.compass.amazondeal.helpers.*;
import offer.compass.amazondeal.services.MultiThreading.*;
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
    private DealOfTheDayRepo dealOfTheDayRepo;
    @Autowired
    private AmazonPrimeDealHelper primeDealHelper;
    @Autowired
    private DOTDHelper dotdHelper;

    @Override
    @Transactional
    public boolean getUrlsByDepartment() throws InterruptedException {
        //delete last run records
        amazonDealHelper.deleteTodaysDealAllRecords();
        //initialize variables
        int maxThreads = Integer.parseInt(propertiesRepo.findByPropName(PropertyConstants.POOL_SIZE).getPropValue());
        List<String> departments = amazonDealHelper.getScheduledDepartments().stream()
                .map(ScheduledDepartment::getDept).sorted((String::compareTo))
                .collect(Collectors.toList());
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
    public boolean getDealOfTheDayUrls() throws Exception {
        //delete last run records
        amazonDealHelper.deleteDealOfTheDayAllRecords();
        WebDriver browser = browserHelper.openBrowser(true, AmazonConstants.TODAYS_DEAL_URL);
        //get total pages
        int totalPages = dotdHelper.getTotalPagesOfDOTDUrl(browser);
        List<String> mainUrls = dotdHelper.fetchDOTDUrls(browser, totalPages);
        log.info("Total main urls found " + mainUrls.size());
        browser.quit();
        //thread service
        int maxThreads = Integer.parseInt(propertiesRepo.findByPropName(PropertyConstants.DOTD_POOL_SIZE).getPropValue());
        ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
        for (List<String> partitionedUrls : Lists.partition(mainUrls, 5)) {
            Thread thread = new GetDealOfTheDayUrls(partitionedUrls, dotdHelper, browserHelper, dealOfTheDayRepo);
            pool.execute(thread);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.HOURS);
        log.info("Completed the get urls from Deal of the day process...");
        log.info("Total Deal of the urls are " + dealOfTheDayRepo.findAll().size());
        return true;
    }

    @Override
    public boolean getPrimeExclusiveUrls() throws InterruptedException {
        //delete last prime records
        primeDealHelper.deleteTodaysDealPrimeExclusiveRecords();
        //initialize variables
        int maxThreads = Integer.parseInt(propertiesRepo.findByPropName(PropertyConstants.POOL_SIZE).getPropValue());
        List<String> departments = amazonDealHelper.getScheduledDepartments().stream()
                .map(ScheduledDepartment::getDept).sorted((String::compareTo))
                .collect(Collectors.toList());
        //limiting the threads
        ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
        for (String department : departments) {
            Thread thread = new GetPrimeUrlsByDeptTask(department, primeDealHelper);
            pool.execute(thread);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.HOURS);
        log.info("Completed the get prime deals By Department process...");
        log.info("Total today's deal urls are " + primeDealHelper.findAllPrimeDealUrls().size());
        return true;
    }
}
