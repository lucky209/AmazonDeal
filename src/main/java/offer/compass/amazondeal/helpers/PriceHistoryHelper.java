package offer.compass.amazondeal.helpers;

import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.PriceHistoryConstants;
import offer.compass.amazondeal.entities.PriceHistory;
import offer.compass.amazondeal.entities.PriceHistoryRepo;
import offer.compass.amazondeal.entities.TodaysDealUrl;
import offer.compass.amazondeal.entities.TodaysDealUrlRepo;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
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
        Thread.sleep(1000);
        browser.findElement(By.id(PriceHistoryConstants.PRICE_HISTORY_TRACK_SEARCH_ID)).click();
    }

    public void savePriceHistoryDetails(WebDriver browser, String url) throws IOException {
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
        //if good offer take SS
        if (isGoodOffer) {
            browser.get(url);
            //1.draw border on regular price box
            this.drawRedBorderById(browser, AmazonConstants.TODAYS_DEAL_REGULAR_PRICE_DIV_ID);
            //2.draw border on reviews box
            this.drawRedBorderById(browser, AmazonConstants.TODAYS_DEAL_REVIEW_ID);
            //3.Now take SS
            this.takeAmazonProductScreenShot(browser, todaysDealUrl.getDept(), prodName);
            //4.save in DB
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

    private void drawRedBorderById(WebDriver browser, String id){
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

    private void showPriceAlertBox(WebDriver browser, int lowestPrice, int highestPrice) {
        String alertHtmlMessage = "Lowest Price : " + lowestPrice + "\n" +
                "Highest Price : " + highestPrice;
        JavascriptExecutor jse = (JavascriptExecutor) browser;
        jse.executeScript("alert('jghjg');");
    }

    private void takeScreenShotOfPHGraph(Integer currentPrice, String prodName, WebDriver browser) throws IOException {
        String ssName = this.getScreenshotName(prodName, currentPrice);
        String pathToSave = PriceHistoryConstants.PATH_TO_SAVE_SS + ssName;
        fileHelper.savePriceHistorySS(browser, pathToSave);
    }
}
