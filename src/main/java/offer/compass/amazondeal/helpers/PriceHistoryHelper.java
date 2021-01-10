package offer.compass.amazondeal.helpers;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
import offer.compass.amazondeal.entities.PriceHistory;
import offer.compass.amazondeal.entities.PriceHistoryRepo;
import offer.compass.amazondeal.entities.TodaysDealUrl;
import offer.compass.amazondeal.entities.TodaysDealUrlRepo;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class PriceHistoryHelper {

    @Autowired
    private PriceHistoryRepo priceHistoryRepo;
    @Autowired
    private TodaysDealUrlRepo todaysDealUrlRepo;
    @Autowired
    private FileHelper fileHelper;

    public void searchWithUrl(WebDriver browser, String url) throws InterruptedException {
        browser.get(PriceHistoryConstants.URL);
        this.enterUrlAndSearch(browser, url);
    }

    private void enterUrlAndSearch(WebDriver browser, String url) throws InterruptedException {
        browser.findElement(By.xpath(PriceHistoryConstants.PRICE_HISTORY_URL_XPATH)).sendKeys(url);
        browser.findElement(By.id(PriceHistoryConstants.PRICE_HISTORY_TRACK_SEARCH_ID)).click();
        Thread.sleep(1000);
    }

    public void savePriceHistoryDetails(WebDriver browser, String url) throws IOException, InterruptedException {
        //get lowest price
        Integer lowestPrice = this.getLowestPrice(browser);
        //get highest price
        Integer highestPrice = this.getHighestPrice(browser);
        //get drop chances
        String dropChances = this.getDropChances(browser);
        //get product name
        String prodName = this.getProductName(browser);
        //get current price
        TodaysDealUrl todaysDealUrl = todaysDealUrlRepo.findByUrl(url);
        Integer currentPrice = todaysDealUrl.getPrice();
        //check good offer
        boolean isGoodOffer = this.isGoodOfferProduct(lowestPrice, highestPrice, currentPrice);
        //check existing product
        boolean isExistingProduct = this.isExistingProduct(prodName, currentPrice);
        //if good offer and not existing product take SS
        if (isGoodOffer && !isExistingProduct) {
            browser.get(url);
            Thread.sleep(2000);
            //1.draw border on regular price box
            this.drawBorderById(browser, AmazonConstants.TODAYS_DEAL_REGULAR_PRICE_DIV_ID);
            //2.draw border on reviews box
            this.drawBorderById(browser, AmazonConstants.TODAYS_DEAL_REVIEW_ID);
            //3.add an draw border on lowest element
            this.drawBorderLowestAndHighestPriceElement(browser, lowestPrice, highestPrice);
            //4.Now take SS
            this.takeAmazonProductScreenShot(browser, todaysDealUrl.getDept(), prodName);
            //5.save in DB
            this.saveInDB(lowestPrice, highestPrice, currentPrice, url,
                    "amazon", dropChances, prodName, true);
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

    private String getProductName(WebDriver browser) {
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

    private String getScreenshotName(String prodName, Integer currentPrice) {
        String screenshotName;
        String[] array = prodName.split(Constants.UTIL_SINGLE_SPACE);
        if (array.length > 3) {
            screenshotName = array[0] + Constants.UTIL_SINGLE_SPACE +
                    array[1] + Constants.UTIL_SINGLE_SPACE + array[2]
                    + Constants.UTIL_HYPHEN;
            if (currentPrice != null) {
                screenshotName = screenshotName + currentPrice + Constants.IMAGE_FORMAT;
            } else {
                screenshotName = screenshotName + Constants.IMAGE_FORMAT;
            }
        } else {
            screenshotName = prodName;
        }
        screenshotName = screenshotName.replace("\"", "");
        return screenshotName;
    }

    private void saveInDB(Integer lowestPrice, Integer highestPrice,
                          Integer currentPrice, String url, String site,
                          String dropChances, String prodName, boolean isGoodOffer) {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setHighestPrice(highestPrice);
        priceHistory.setLowestPrice(lowestPrice);
        priceHistory.setCurrentPrice(currentPrice);
        priceHistory.setUrl(url);
        priceHistory.setSite(site);
        priceHistory.setDropChances(dropChances);
        priceHistory.setProductName(prodName);
        priceHistory.setGoodOffer(isGoodOffer);
        priceHistoryRepo.save(priceHistory);
    }

    private Integer convertStringPriceToInteger(String price) {
        return Integer.parseInt(price.replace(Constants.UTIL_RUPEE, Constants.UTIL_EMPTY_QUOTE)
                .replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim());
    }

    private void drawBorderById(WebDriver browser, String id){
        boolean isElementAvailable = !browser.findElements(By.id(id)).isEmpty();
        if (isElementAvailable) {
            JavascriptExecutor jse = (JavascriptExecutor) browser;
            jse.executeScript(Constants.DRAW_RED_BORDER_SCRIPT, browser.findElement(By.id(id)));
        }
    }

    private void takeAmazonProductScreenShot(WebDriver browser, String dept, String prodName) throws IOException {
        String ssName = this.getScreenshotName(prodName, null);
        String folderPath = AmazonConstants.PATH_TO_SAVE_SS + dept + "\\";
        String pathToSave = folderPath + ssName;
        fileHelper.saveAmazonSS(browser, pathToSave, folderPath);
    }

    private boolean isExistingProduct(String prodName, Integer currentPrice) {
        PriceHistory existingEntity = priceHistoryRepo.findByProductNameAndCurrentPrice(prodName, currentPrice);
        return existingEntity != null;
    }

    private void drawBorderLowestAndHighestPriceElement(WebDriver browser, int lowestPrice, int highestPrice) {
        boolean isElementAvailable = !browser.findElements(By.id(PriceHistoryConstants.PRICE_HISTORY_REPLACE_DIV)).isEmpty();
        if (isElementAvailable) {
            WebElement element = browser.findElement(By.id(PriceHistoryConstants.PRICE_HISTORY_REPLACE_DIV));
            JavascriptExecutor jse = (JavascriptExecutor)browser;
            String lowestHighestHtml = AmazonConstants.PRICE_HISTORY_LOWEST_HIGHEST_PRICE_TABLE_HTML
                    .replace("$lowestPrice" , String.valueOf(lowestPrice))
                    .replace("$highestPrice" , String.valueOf(highestPrice));
            jse.executeScript("var ele=arguments[0]; ele.innerHTML = '" + lowestHighestHtml + "';", element);
            this.drawBorderById(browser, PriceHistoryConstants.PRICE_HISTORY_DIV);
        } else {
            log.info("PRICE_HISTORY_REPLACE_DIV is not available for the product " + lowestPrice + " " + highestPrice);
        }
    }

}
