package offer.compass.amazondeal.helpers;

import com.google.common.base.CharMatcher;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.DOTDConstants;
import offer.compass.amazondeal.entities.DealOfTheDay;
import offer.compass.amazondeal.services.TodaysDealService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DOTDHelper {

    @Autowired
    private TodaysDealService todaysDealService;

    public List<String> fetchDOTDUrls(WebDriver browser, int totalPages) throws Exception {
        List<String> mainUrls = new ArrayList<>();
        boolean isMainDivAvail = !browser.findElements(By.id(AmazonConstants.DOTD_MAIN_DIV_ID)).isEmpty();
        if (isMainDivAvail) {
            for (int i=1;i<=totalPages;i++) {
                List<WebElement> mainUrlElements = browser.findElement(By.id(AmazonConstants.DOTD_MAIN_DIV_ID))
                        .findElements(By.id(AmazonConstants.DOTD_MAIN_URL_ID));
                for (WebElement mainUrlEle : mainUrlElements) {
                    mainUrls.add(mainUrlEle.getAttribute(Constants.ATTRIBUTE_HREF));
                }
                if (i < totalPages)
                    this.clickDOTDNextPage(browser);
            }
            return mainUrls;
        }
        throw new Exception("Deal of the day, main div not available.");
    }

    private void clickDOTDNextPage(WebDriver browser) throws Exception {
        boolean isNextButtonAvailable = !browser.findElements(
                By.id(AmazonConstants.DOTD_NEXT_BUTTON_DIV_ID)).isEmpty();
        if (isNextButtonAvailable) {
            browser.findElement(By.id(AmazonConstants.DOTD_NEXT_BUTTON_DIV_ID)).click();
            Thread.sleep(500);
        } else {
            throw new Exception("Deal of the day - Next button is not available");
        }
    }

    public int getTotalPagesOfDOTDUrl(WebDriver browser) throws Exception {
        boolean isPageEleAvail = !browser.findElements(
                By.id(AmazonConstants.DOTD_TOTAL_PAGE_DIV_ID)).isEmpty();
        if (isPageEleAvail) {
            String paginationText = browser.findElement(By.id(AmazonConstants.DOTD_TOTAL_PAGE_DIV_ID)).getText();
            paginationText = paginationText.substring(paginationText.lastIndexOf("of") + 3).trim();
            return Integer.parseInt(paginationText);
        }
        throw new Exception("Deal of the day div not available to get urls");
    }

    public List<DealOfTheDay> fetchDOTDEntitiesByMainUrl(WebDriver browser, boolean isPrime) throws Exception {
        List<DealOfTheDay> totalEntities = new ArrayList<>();
        //click see all results
        this.clickSeeAllResults(browser);
        //click rating star
        boolean isRatingStarClicked = this.clickRatingStar(browser);
        //get total Pages
        int totalPages = this.getTotalPagesOfMainUrl(browser);
        for (int i=0;i<totalPages;i++) {
            //find type
            DOTDTypeEnum dotdTypeEnum = this.fetchTypeOfMainUrl(browser);
            log.info("Found {}", dotdTypeEnum.toString());
            List<DealOfTheDay> entities = new ArrayList<>();
            switch (dotdTypeEnum) {
                case TYPE_1:
                    entities = this.fetchType1Entities(browser, isPrime);
                    break;
                case TYPE_2:
                    if (isRatingStarClicked)
                        entities = this.fetchType2Entities(browser, isPrime);//done mainResults becomes type4 after click star
                    else
                        throw new Exception("Cannot click rating star for type2");
                    break;
                case TYPE_3:
                    entities = this.fetchType3Entities(browser, isPrime, isRatingStarClicked);//tested oct-dls-asin-stream-container
                    break;
                case TYPE_4:
                        entities = this.fetchType4Entities(browser, isPrime, isRatingStarClicked);//tested s-main-slot s-result-list s-search-results sg-row
                    break;
                case TYPE_5:
                    entities = this.fetchType5Entities(browser, isPrime);
                    break;
                case SINGLE_PRODUCT:
                    entities = this.singleProductEntity(browser, isPrime);
                    break;
                case UNAVAILABLE:
                    log.info("Page is currently Unavailable");
                    break;
                default:
                    throw new Exception("Cannot find the type for the main url " + browser.getCurrentUrl());
            }
            log.info("{} entities found in the page {}", entities.size(), i+1);
            totalEntities.addAll(entities);
            this.clickMainUrlNextPage(browser);
            log.info("clicked next page");
            this.clickSeeAllResults(browser);
        }
        return totalEntities;
    }

    private void clickMainUrlNextPage(WebDriver browser) throws InterruptedException {
        boolean isNextButtonAvail = !browser.findElements(By.id(AmazonConstants.PAGINATION_NEXT_BUTTON_ID)).isEmpty();
        if (!isNextButtonAvail) {
            todaysDealService.clickNextButton(browser);
        } else {
            browser.findElement(By.id(AmazonConstants.PAGINATION_NEXT_BUTTON_ID)).click();
            Thread.sleep(3000);
        }
    }

    private synchronized void clickSeeAllResults(WebDriver browser) throws InterruptedException {
        boolean isSeeAllResultsAvail = !browser.findElements(By.cssSelector(DOTDConstants.SEE_ALL_RESULTS_CSS_CLASS)).isEmpty();
        if (isSeeAllResultsAvail) {
            browser.findElement(By.cssSelector(DOTDConstants.SEE_ALL_RESULTS_CSS_CLASS)).click();
            Thread.sleep(3000);
        }
    }

    private synchronized int getTotalPagesOfMainUrl(WebDriver browser) {
        boolean lastPage = !browser.findElements(By.className(
                AmazonConstants.PAGINATION_DISABLED_CLASS)).isEmpty();
        if (lastPage) {
            String lastPageStr = browser.findElement(By.className(
                    AmazonConstants.PAGINATION_DISABLED_CLASS)).getText().trim();
            log.info("total pages " + lastPageStr);
            return Integer.parseInt(lastPageStr);
        } else {
            return todaysDealService.getTotalPageCount(browser);
        }
    }

    private List<DealOfTheDay> singleProductEntity(WebDriver browser, boolean isPrime) {
        List<DealOfTheDay> entities = new ArrayList<>();
        Integer price = this.getSingleProductPrice(browser);
        DealOfTheDay dealOfTheDay = this.createEntity(browser.getCurrentUrl(), price, isPrime);
        entities.add(dealOfTheDay);
        return entities;
    }

    private List<DealOfTheDay> fetchType5Entities(WebDriver browser, boolean isPrime) throws Exception {
        throw new Exception("Type 5 not developed yet. Current url is " + browser.getCurrentUrl());
    }

    private List<DealOfTheDay> fetchType4Entities(WebDriver browser, boolean isPrime, boolean isRatingStarClicked) throws Exception {
        //filter by rating star
        WebElement mainElement = browser.findElement(By.cssSelector(
                DOTDConstants.TYPE_4_MAIN_PRODUCT_CSS_CLASS));
        List<WebElement> singleProductElements = mainElement.findElements(
                By.cssSelector(DOTDConstants.TYPE_4_SINGLE_PRODUCT_COMMON_CSS_CLASS));
        //filter by rating star
        if (!isRatingStarClicked) {
            this.filterByRatingStar(singleProductElements,
                    DOTDTypeEnum.TYPE_4);
        }
        //filter by rating count
        this.filterByRatingCount(singleProductElements, DOTDTypeEnum.TYPE_4);
        //get price and url and create entities
        return this.createEntities(singleProductElements, DOTDTypeEnum.TYPE_4, isPrime);
    }

    private List<DealOfTheDay> fetchType3Entities(WebDriver browser, boolean isPrime, boolean isRatingStarClicked) throws Exception {
        WebElement mainElement = browser.findElement(By.id(
                DOTDConstants.TYPE_3_MAIN_PRODUCT_ID));
        List<WebElement> singleProductElements = mainElement.findElements(
                By.cssSelector(DOTDConstants.TYPE_3_SINGLE_PRODUCT_COMMON_CSS_CLASS));
        //filter by rating star
        if (!isRatingStarClicked) {
            this.filterByRatingStar(singleProductElements,
                    DOTDTypeEnum.TYPE_3);
        }
        //filter by rating count
        this.filterByRatingCount(singleProductElements, DOTDTypeEnum.TYPE_3);
        //get price and url and create entities
        return this.createEntities(singleProductElements, DOTDTypeEnum.TYPE_3, isPrime);
    }

    private List<DealOfTheDay> fetchType2Entities(WebDriver browser, boolean isPrime) throws Exception {
        WebElement mainElement = browser.findElement(By.id(
                DOTDConstants.TYPE_2_MAIN_PRODUCT_ID));
        List<WebElement> singleProductElements = mainElement.findElements(
                By.xpath(DOTDConstants.TYPE_2_SINGLE_PRODUCT_COMMON_XPATH));
        //filter by rating count
        this.filterByRatingCount(singleProductElements, DOTDTypeEnum.TYPE_2);
        //get price and url and create entities
        return this.createEntities(singleProductElements, DOTDTypeEnum.TYPE_2, isPrime);
    }

    private List<DealOfTheDay> fetchType1Entities(WebDriver browser, boolean isPrime) throws Exception {
        WebElement mainElement = browser.findElement(By.id(
                DOTDConstants.TYPE_1_MAIN_PRODUCT_ID));
        List<WebElement> singleProductElements = mainElement.findElements(
                By.cssSelector(DOTDConstants.TYPE_1_SINGLE_PRODUCT_COMMON_CSS_CLASS));
        //filter by rating star
        boolean isRatingStarClicked = this.clickRatingStar(browser);
        if (!isRatingStarClicked) {
            this.filterByRatingStar(singleProductElements, DOTDTypeEnum.TYPE_1);
        }
        //filter by rating count
        this.filterByRatingCount(singleProductElements, DOTDTypeEnum.TYPE_1);
        //get price and url and create entities
        return this.createEntities(singleProductElements, DOTDTypeEnum.TYPE_1, isPrime);
    }

    private List<DealOfTheDay> createEntities(List<WebElement> productElements, DOTDTypeEnum type, boolean isPrime) throws Exception {
        List<DealOfTheDay> entities = new ArrayList<>();
        switch (type) {
            case TYPE_2:
                for (WebElement element : productElements) {
                    boolean isAvail = this.isPriceAndUrlElementAvailableForType2(element);
                    if (isAvail) {
                        Integer price = Integer.parseInt(element.findElement(
                                By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_PRICE_CSS_CLASS))
                                .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE)
                                .replace(Constants.UTIL_RUPEE, Constants.UTIL_EMPTY_QUOTE).trim());
                        String url = element.findElement(By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_URL_CSS_CLASS))
                                .findElement(By.tagName(Constants.TAG_ANCHOR)).getAttribute(Constants.ATTRIBUTE_HREF);
                        DealOfTheDay entity = this.createEntity(url, price, isPrime);
                        entities.add(entity);
                    }
                }
                break;
            case TYPE_3:
                for (WebElement element : productElements) {
                    boolean isAvail = this.isPriceAndUrlElementAvailableForType3(element);
                    if (isAvail) {
                        Integer price = Integer.parseInt(element.findElement(
                                By.className(DOTDConstants.TYPE_3_SINGLE_PRODUCT_PRICE_CLASS))
                                .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE)
                                .replace(Constants.UTIL_DOT, Constants.UTIL_EMPTY_QUOTE).trim());
                        String url = element.findElement(By.cssSelector(DOTDConstants.TYPE_3_SINGLE_PRODUCT_URL_CSS_CLASS))
                                .getAttribute(Constants.ATTRIBUTE_HREF);
                        DealOfTheDay entity = this.createEntity(url, price, isPrime);
                        entities.add(entity);
                    }
                }
                break;
            case TYPE_4:
                for (WebElement element : productElements) {
                    boolean isAvail = this.isPriceAndUrlElementAvailableForType4(element);
                    if (isAvail) {
                        Integer price = Integer.parseInt(element.findElement(
                                By.className(DOTDConstants.TYPE_4_SINGLE_PRODUCT_PRICE_CLASS))
                                .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE));
                        String url = element.findElement(By.cssSelector(DOTDConstants.TYPE_4_SINGLE_PRODUCT_URL_CSS_CLASS))
                                .getAttribute(Constants.ATTRIBUTE_HREF);
                        DealOfTheDay entity = this.createEntity(url, price, isPrime);
                        entities.add(entity);
                    }
                }
                break;
            default:
                throw new Exception("Not developed yet-create entities");
        }
        return entities;
    }

    private boolean isPriceAndUrlElementAvailableForType2(WebElement element) {
        boolean isPriceEleAvail = !element.findElements(
                By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_PRICE_CSS_CLASS)).isEmpty();
        boolean isUrlEleAvail = !element.findElements(
                By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_URL_CSS_CLASS)).isEmpty();
        return isPriceEleAvail && isUrlEleAvail;
    }

    private boolean isPriceAndUrlElementAvailableForType3(WebElement element) {
        boolean isPriceEleAvail = !element.findElements(
                By.className(DOTDConstants.TYPE_3_SINGLE_PRODUCT_PRICE_CLASS)).isEmpty();
        boolean isUrlEleAvail = !element.findElements(
                By.cssSelector(DOTDConstants.TYPE_3_SINGLE_PRODUCT_URL_CSS_CLASS)).isEmpty();
        return isPriceEleAvail && isUrlEleAvail;
    }

    private boolean isPriceAndUrlElementAvailableForType4(WebElement productElement) {
        boolean isPriceEleAvail = !productElement.findElements(
                By.className(DOTDConstants.TYPE_4_SINGLE_PRODUCT_PRICE_CLASS)).isEmpty();
        boolean isUrlEleAvail = !productElement.findElements(
                By.cssSelector(DOTDConstants.TYPE_4_SINGLE_PRODUCT_URL_CSS_CLASS)).isEmpty();
        return isPriceEleAvail && isUrlEleAvail;
    }

    private void filterByRatingCount(List<WebElement> productElements, DOTDTypeEnum type) throws Exception {
        switch (type) {
            case TYPE_2:
                this.filterByRatingCountForType2(productElements);
                break;
            case TYPE_3:
                this.filterByRatingCountForType3(productElements);
                break;
            case TYPE_4:
                this.filterByRatingCountForType4(productElements);
                break;
            default:
                throw new Exception("Not developed for this type-filter by rating count");
        }
    }

    private void filterByRatingCountForType2(List<WebElement> productElements) {
        List<WebElement> unwantedElements = new ArrayList<>();
        for (WebElement element : productElements) {
            boolean isEleAvail = !element.findElements(By.cssSelector(DOTDConstants.TYPE_2_REVIEW_CSS_CLASS)).isEmpty();
            if (isEleAvail) {
                String hrefAttr = element.findElement(By.cssSelector(DOTDConstants.TYPE_2_REVIEW_CSS_CLASS))
                        .getAttribute(Constants.ATTRIBUTE_HREF);
                if (hrefAttr.contains(DOTDConstants.PRODUCT_PROMOTIONS_UTIL_STRING)) {
                    unwantedElements.add(element);
                    continue;
                }
                int ratings = Integer.parseInt(element.findElement(By.cssSelector(DOTDConstants.TYPE_2_REVIEW_CSS_CLASS))
                        .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim());
                if (ratings < 50) {
                    unwantedElements.add(element);
                }
            } else {
                unwantedElements.add(element);
            }
        }
        productElements.removeAll(unwantedElements);
    }

    private void filterByRatingCountForType3(List<WebElement> productElements) {
        List<WebElement> unwantedElements = new ArrayList<>();
        for (WebElement element : productElements) {
            boolean isEleAvail = !element.findElements(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS)).isEmpty();
            if (isEleAvail) {
                int ratings = Integer.parseInt(element.findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS))
                        .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim());
                if (ratings < 50) {
                    unwantedElements.add(element);
                }
            } else {
                unwantedElements.add(element);
            }
        }
        productElements.removeAll(unwantedElements);
    }

    private void filterByRatingCountForType4(List<WebElement> productElements) {
        List<WebElement> unwantedElements = new ArrayList<>();
        for (WebElement element : productElements) {
            boolean isAvail = !element.findElements(By.tagName(Constants.TAG_ANCHOR)).isEmpty();
            if (isAvail) {
                List<WebElement> anchorElements = element.findElements(By.tagName(Constants.TAG_ANCHOR));
                int anchorCount = 1;
                for (WebElement anchorElement : anchorElements) {
                    if (anchorElement.getAttribute(Constants.ATTRIBUTE_HREF).contains(
                            DOTDConstants.CUSTOMER_REVIEWS_UTIL_STRING)) {
                        int ratingCount = Integer.parseInt(anchorElement.getText()
                                .replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim());
                        if (ratingCount < 50) {
                            unwantedElements.add(element);
                        }
                        break;
                    } else {
                        if (anchorElements.size() == anchorCount) {
                            //last element with href doesnt have reviews
                            unwantedElements.add(element);
                        }
                    }
                    anchorCount++;
                }
            } else {
                unwantedElements.add(element);
            }
        }
        productElements.removeAll(unwantedElements);
    }

    private void filterByRatingStar(List<WebElement> productElements, DOTDTypeEnum type) throws Exception {
        switch (type) {
            case TYPE_3:
                this.filterByRatingStarByType3(productElements);
                break;
            case TYPE_4:
                this.filterByRatingStarByType4(productElements);
                break;
            default:
                throw new Exception("Not developed for this type - filter by rating star");
        }
    }

    private void filterByRatingStarByType4(List<WebElement> productElements) {
        List<WebElement> unwantedElements = new ArrayList<>();
        for (WebElement element : productElements) {
            boolean isReviewEleAvail = !element.findElements(
                    By.xpath(DOTDConstants.TYPE_4_REVIEW_STAR_XPATH)).isEmpty();
            if (isReviewEleAvail) {
                String star = element.findElement(By.xpath(
                        DOTDConstants.TYPE_4_REVIEW_STAR_XPATH)).getAttribute(Constants.ATTRIBUTE_CLASS);
                star = CharMatcher.inRange('0', '9').retainFrom(star);
                if (!(star.equals("4") || star.equals("45") || star.equals("5"))) {
                    unwantedElements.add(element);
                }
            } else {
                unwantedElements.add(element);
            }
        }
        productElements.removeAll(unwantedElements);
    }

    private void filterByRatingStarByType3(List<WebElement> productElements) {
        List<WebElement> unwantedElements = new ArrayList<>();
        for (WebElement element : productElements) {
            boolean isReviewEleAvail = !element.findElements(
                    By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS)).isEmpty();
            if (isReviewEleAvail) {
                isReviewEleAvail = !element
                        .findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS))
                        .findElements(By.tagName(Constants.TAG_I)).isEmpty();
                if (isReviewEleAvail) {
                    String star = element
                            .findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS))
                            .findElement(By.tagName(Constants.TAG_I)).getAttribute(Constants.ATTRIBUTE_CLASS);
                    star = CharMatcher.inRange('0', '9').retainFrom(star);
                    if (!(star.equals("4") || star.equals("45") || star.equals("5"))) {
                        unwantedElements.add(element);
                    }
                } else {
                    unwantedElements.add(element);
                }
            } else {
                unwantedElements.add(element);
            }
        }
        productElements.removeAll(unwantedElements);
    }

    private synchronized boolean clickRatingStar(WebDriver browser) throws InterruptedException {
        boolean isEleAvail = !browser.findElements(By.id(DOTDConstants.RATING_STAR_ID)).isEmpty();
        boolean isEleAvailByIconXpath = !browser.findElements(By.xpath(DOTDConstants.RATING_STAR_XPATH)).isEmpty();
        if (isEleAvail) {
            List<WebElement> liList = browser.findElement(By.id(DOTDConstants.RATING_STAR_ID))
                    .findElements(By.tagName(Constants.TAG_LI));
            if (liList.size() > 0) {
                liList.get(0).click();
                Thread.sleep(3000);
                return true;
            }
        } else if (isEleAvailByIconXpath) {
            browser.findElement(By.xpath(DOTDConstants.RATING_STAR_XPATH)).click();
            Thread.sleep(3000);
            return true;
        }
        return false;
    }

    private Integer getSingleProductPrice(WebDriver browser) {
        String priceStr;
        boolean isPriceV1Available = !browser.findElements(By.id(AmazonConstants.PRICE_ID_V1)).isEmpty();
        if (isPriceV1Available) {
            priceStr = browser.findElement(By.id(AmazonConstants.PRICE_ID_V1)).getText();
        } else {
            priceStr = browser.findElement(By.id(AmazonConstants.PRICE_ID_V2)).getText();
        }
        priceStr = priceStr.replace(Constants.UTIL_RUPEE, Constants.UTIL_EMPTY_QUOTE)
                .replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE);
        priceStr = priceStr.substring(0, priceStr.indexOf(Constants.UTIL_DOT)).trim();
        return Integer.parseInt(priceStr);
    }

    private DealOfTheDay createEntity(String url, Integer price, boolean isPrime) {
        DealOfTheDay dealOfTheDay = new DealOfTheDay();
        dealOfTheDay.setUrl(url);
        dealOfTheDay.setPrice(price);
        String dept = isPrime ? "Prime":"Deal of the day";
        dealOfTheDay.setDept(dept);
        dealOfTheDay.setCreatedDate(LocalDateTime.now());
        return dealOfTheDay;
    }

    private synchronized DOTDTypeEnum fetchTypeOfMainUrl(WebDriver browser) {
        if (!browser.findElements(By.id(DOTDConstants.TYPE_1_MAIN_PRODUCT_ID)).isEmpty()) {
            return DOTDTypeEnum.TYPE_1;
        } else if (!browser.findElements(By.id(DOTDConstants.TYPE_2_MAIN_PRODUCT_ID)).isEmpty()) {
            return DOTDTypeEnum.TYPE_2;
        } else if (!browser.findElements(By.id(DOTDConstants.TYPE_3_MAIN_PRODUCT_ID)).isEmpty()) {
            return DOTDTypeEnum.TYPE_3;
        } else if (!browser.findElements(By.cssSelector(DOTDConstants.TYPE_4_MAIN_PRODUCT_CSS_CLASS)).isEmpty()) {
            return DOTDTypeEnum.TYPE_4;
        } else if (!browser.findElements(By.cssSelector(DOTDConstants.TYPE_5_MAIN_PRODUCT_CSS_CLASS)).isEmpty()) {
            return DOTDTypeEnum.TYPE_5;
        } else if (!browser.findElements(By.id(AmazonConstants.DOTD_MAIN_AS_SINGLE_PRODUCT_ID)).isEmpty()) {
            return DOTDTypeEnum.SINGLE_PRODUCT;
        } else if (browser.getPageSource().contains("This deal is currently unavailable")) {
            return DOTDTypeEnum.UNAVAILABLE;
        }
        return DOTDTypeEnum.UNKNOWN;
    }
}
