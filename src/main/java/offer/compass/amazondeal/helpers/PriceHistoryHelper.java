package offer.compass.amazondeal.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
import offer.compass.amazondeal.constants.PropertyConstants;
import offer.compass.amazondeal.entities.*;
import offer.compass.amazondeal.models.BitlyRequest;
import offer.compass.amazondeal.models.BitlyResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class PriceHistoryHelper {

    private static final String BITLY_URL = "https://api-ssl.bitly.com/v4/shorten";

    @Value("${bitly.access.token}")
    private String accessToken;

    @Autowired
    private PriceHistoryRepo priceHistoryRepo;
    @Autowired
    private TodaysDealUrlRepo todaysDealUrlRepo;
    @Autowired
    private DealOfTheDayRepo dealOfTheDayRepo;
    @Autowired
    private FileHelper fileHelper;
    @Autowired
    private PropertiesRepo propertiesRepo;

    public void searchWithUrl(WebDriver browser, String url) throws InterruptedException {
        browser.get(PriceHistoryConstants.URL);
        this.enterUrlAndSearch(browser, url);
    }

    private void enterUrlAndSearch(WebDriver browser, String url) throws InterruptedException {
        browser.findElement(By.xpath(PriceHistoryConstants.PRICE_HISTORY_URL_XPATH)).sendKeys(url);
        browser.findElement(By.id(PriceHistoryConstants.PRICE_HISTORY_TRACK_SEARCH_ID)).click();
        Thread.sleep(1000);
    }

    public void savePriceHistoryDetails(WebDriver browser, String url, boolean isDOTDEnabled) {
        //get lowest price
        Integer lowestPrice = this.getLowestPrice(browser);
        //get highest price
        Integer highestPrice = this.getHighestPrice(browser);
        //get drop chances
        String dropChances = this.getDropChances(browser);
        //get product name
        String prodName = this.getProductName(browser);
        //get current price
        Integer currentPrice;
        DealOfTheDay dealOfTheDay;
        TodaysDealUrl todaysDealUrl;
        if (isDOTDEnabled) {
            dealOfTheDay = dealOfTheDayRepo.findByUrl(url);
            currentPrice = dealOfTheDay.getPrice();
        } else {
            todaysDealUrl = todaysDealUrlRepo.findByUrl(url);
            currentPrice = todaysDealUrl.getPrice();
        }
        //check good offer
        boolean isGoodOffer = this.isGoodOfferProduct(lowestPrice, highestPrice, currentPrice);
        //check existing product
        boolean isExistingProduct = this.isExistingProduct(prodName, currentPrice);
        //if good offer and not existing product take SS
        if (isGoodOffer && !isExistingProduct) {
            //shorten the url
            String shortUrl = null;
            if (propertiesRepo.findByPropName(PropertyConstants.SHORTEN_URL).isEnabled())
                shortUrl = this.getShortenUrl(url);
            //save in DB
            this.saveInDB(lowestPrice, highestPrice, currentPrice, url,
                    "amazon", dropChances, prodName, true, shortUrl, isDOTDEnabled);
        }
    }

    private Integer getLowestPrice(WebDriver browser) {
        return this.convertStringPriceToInteger(browser.findElement(By.id(PriceHistoryConstants.LOWEST_PRICE_ID))
                .getText().trim());
    }

    private Integer getHighestPrice(WebDriver browser) {
        return this.convertStringPriceToInteger(browser.findElement(By.id(PriceHistoryConstants.HIGHEST_PRICE_ID))
                .getText().trim());
    }

    private String getDropChances(WebDriver browser) {
        return browser.findElement(By.id(PriceHistoryConstants.DROP_CHNACES_ID))
                .getText();
    }

    public String getProductName(WebDriver browser) {
        return browser.findElement(By.id(PriceHistoryConstants.PRODUCT_NAME_ID)).getText();
    }

    private boolean isGoodOfferProduct(Integer lowestPrice, Integer highestPrice, Integer currentPrice) {
        int lowestDel = currentPrice - lowestPrice;
        int highestDel = highestPrice - currentPrice;
        if (lowestDel < highestDel) {
            int midVal = (highestPrice - lowestPrice)/4;
            return currentPrice <= (lowestPrice + midVal);
        }
        return false;
    }

    private void saveInDB(Integer lowestPrice, Integer highestPrice,
                          Integer currentPrice, String url, String site,
                          String dropChances, String prodName, boolean isGoodOffer, String shortUrl, boolean isDOTDEnabled) {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setHighestPrice(highestPrice);
        priceHistory.setLowestPrice(lowestPrice);
        priceHistory.setCurrentPrice(currentPrice);
        priceHistory.setUrl(url);
        priceHistory.setSite(site);
        priceHistory.setDropChances(dropChances);
        priceHistory.setProductName(prodName);
        priceHistory.setGoodOffer(isGoodOffer);
        priceHistory.setShortUrl(shortUrl);
        priceHistory.setDotd(isDOTDEnabled);
        priceHistoryRepo.save(priceHistory);
    }

    private String getScreenshotName(String prodName, Integer currentPrice) {
        String screenshotName;
        String[] array = prodName.split(Constants.UTIL_SINGLE_SPACE);
        if (array.length > 3) {
            screenshotName = array[0] + Constants.UTIL_SINGLE_SPACE +
                    array[1] + Constants.UTIL_SINGLE_SPACE + array[2]
                    + Constants.UTIL_HYPHEN;
            if (currentPrice != null) {
                screenshotName = screenshotName + currentPrice ;
            }
        } else {
            screenshotName = prodName;
        }
        screenshotName = screenshotName.replaceAll("[^\\w\\s]", Constants.UTIL_EMPTY_QUOTE);
        screenshotName = screenshotName + "-" + System.currentTimeMillis();
        screenshotName = screenshotName + Constants.IMAGE_FORMAT;
        return screenshotName;
    }


    private Integer convertStringPriceToInteger(String price) {
        return Integer.parseInt(price.replace(Constants.UTIL_RUPEE, Constants.UTIL_EMPTY_QUOTE)
                .replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim());
    }

    public void drawBorderById(WebDriver browser, String id){
        boolean isElementAvailable = !browser.findElements(By.id(id)).isEmpty();
        if (isElementAvailable) {
            JavascriptExecutor jse = (JavascriptExecutor) browser;
            jse.executeScript(Constants.DRAW_RED_BORDER_SCRIPT, browser.findElement(By.id(id)));
        }
    }

    public synchronized void takeAmazonProductScreenShot(WebDriver browser, String dept, String prodName) throws IOException {
        String ssName = this.getScreenshotName(prodName, null);
        String folderPath = AmazonConstants.PATH_TO_SAVE_SS + dept + "\\";
        String pathToSave = folderPath + ssName;
        fileHelper.saveAmazonSS(browser, pathToSave, folderPath);
    }

    private boolean isExistingProduct(String prodName, Integer currentPrice) {
        PriceHistory existingEntity = priceHistoryRepo.findByProductNameAndCurrentPrice(prodName, currentPrice);
        return existingEntity != null;
    }

    public void drawBorderLowestAndHighestPriceElement(WebDriver browser, int lowestPrice, int highestPrice) {

        boolean isElementAvailable = !browser.findElements(By.id(PriceHistoryConstants.PRICE_HISTORY_REPLACE_DIV_ID)).isEmpty();
        if (isElementAvailable) {
            WebElement element = browser.findElement(By.id(PriceHistoryConstants.PRICE_HISTORY_REPLACE_DIV_ID));
            JavascriptExecutor jse = (JavascriptExecutor)browser;
            String lowestHighestHtml = AmazonConstants.PRICE_HISTORY_LOWEST_HIGHEST_PRICE_TABLE_HTML
                    .replace("$lowestPrice" , String.valueOf(lowestPrice))
                    .replace("$highestPrice" , String.valueOf(highestPrice));
            jse.executeScript("var ele=arguments[0]; ele.innerHTML = '" + lowestHighestHtml + "';", element);
            boolean isPHEleVisible = browser.findElement(By.id(PriceHistoryConstants.PRICE_HISTORY_DIV_ID)).isDisplayed();
            if (!isPHEleVisible) {
                ((JavascriptExecutor) browser).executeScript("window.scrollBy(0,10)");
            }
            this.drawBorderById(browser, PriceHistoryConstants.PRICE_HISTORY_DIV_ID);
        } else {
            log.info("PRICE_HISTORY_REPLACE_DIV is not available for the product " + lowestPrice + " " + highestPrice);
        }
    }

    private String getShortenUrl(String longUrl) {
        RestTemplate restTemplate = new RestTemplate();
        BitlyRequest bitlyRequest = new BitlyRequest();
        bitlyRequest.setLongUrl(longUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ accessToken);
        ResponseEntity<BitlyResponse> response = restTemplate.exchange(BITLY_URL,
                HttpMethod.POST, new HttpEntity<>(bitlyRequest, headers), BitlyResponse.class);
        return Objects.requireNonNull(response.getBody()).getLink();
    }

    public void getDescriptionString() {
        List<PriceHistory> priceHistoryList = priceHistoryRepo.getAllTodaysEntities();
        StringBuilder output = new StringBuilder();
        for (PriceHistory priceHistory : priceHistoryList) {
            output
                    .append(priceHistory.getProductName())
                    .append(" -- ")
                    .append(priceHistory.getShortUrl())
                    .append("\n");
        }
        log.info("Description string is\n" + output);
    }
}
