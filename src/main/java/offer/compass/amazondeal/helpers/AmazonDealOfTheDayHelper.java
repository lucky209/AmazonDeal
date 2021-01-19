package offer.compass.amazondeal.helpers;

import com.google.common.base.CharMatcher;
import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.DOTDConstants;
import offer.compass.amazondeal.entities.DealOfTheDay;
import offer.compass.amazondeal.entities.ScheduledDepartment;
import offer.compass.amazondeal.services.ProdService;
import offer.compass.amazondeal.services.TodaysDealService;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
@Slf4j
public class AmazonDealOfTheDayHelper {

    @Autowired
    private TodaysDealService todaysDealService;

    @Autowired
    private AmazonDealHelper amazonDealHelper;

    @Autowired
    private ProdService prodService;

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

    public List<DealOfTheDay> fetchSingleTabProductUrls(WebDriver browser, boolean isPrime) throws Exception {
        List<DealOfTheDay> singleTabEntities = new ArrayList<>();
        //check if the page is unavailable
        boolean pageUnavailable = this.checkPageUnavailable(browser);
        if (pageUnavailable) {
            log.info("Page is currently unavailable");
            return singleTabEntities;
        }
        //check single page product
        boolean isSingleProduct = !browser.findElements(By.id(AmazonConstants.DOTD_MAIN_AS_SINGLE_PRODUCT_ID)).isEmpty();
        if (isSingleProduct) {
            Integer price = this.getSingleProductPrice(browser);
            singleTabEntities.add(this.createEntity(browser.getCurrentUrl(), price, isPrime));
            return singleTabEntities;
        }
        //click see all results & get total pages
        this.clickSeeAllResults(browser);
        int totalPages = this.getTotalPagesOfMainUrl(browser);
        String type = this.getType(browser);
        //if other types
        for (int i=0;i<totalPages;i++) {
            List<DealOfTheDay> currentPageEntities = this.fetchCurrentPageEntities(browser, type, isPrime);
            singleTabEntities.addAll(currentPageEntities);
            this.clickMainUrlNextPage(browser);
        }
        return singleTabEntities;
    }

    private void clickMainUrlNextPage(WebDriver browser) throws InterruptedException {
        boolean isNextButtonAvail = !browser.findElements(By.id("pagnNextString")).isEmpty();
        if (!isNextButtonAvail) {
            todaysDealService.clickNextButton(browser);
        } else {
            browser.findElement(By.id("pagnNextString")).click();
        }
    }

    private List<DealOfTheDay> fetchCurrentPageEntities(WebDriver browser, String type, boolean isPrime) {
        List<DealOfTheDay> entities = new ArrayList<>();
        switch (type) {
            case "type1":
                entities = this.getType1Entities(browser, isPrime);
                break;
            case "type2":
                entities = this.getType2Entities(browser, isPrime);
                break;
            case "type3":
                entities = this.getType3Entities(browser, isPrime);
                break;
            case "type4":
                entities = this.getType4Entities(browser, isPrime);
                break;
            case "type5":
                entities = this.getType5Entities(browser, isPrime);
                break;
        }
        return entities;
    }

    private List<DealOfTheDay> getType5Entities(WebDriver browser, boolean isPrime) {
        List<DealOfTheDay> entities = new ArrayList<>();
        List<WebElement> commonElements = browser.findElement(By.cssSelector(DOTDConstants.TYPE_5_MAIN_PRODUCT_CSS_CLASS))
                .findElements(By.cssSelector(DOTDConstants.TYPE_5_SINGLE_PRODUCT_COMMON_CSS_CLASS));
        for (WebElement commonElement : commonElements) {
            if (!this.isGoodReviewProduct(commonElement, "type5"))
                continue;
            boolean urlEleAvail = !commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_URL_CSS_CLASS))
                    .findElements(By.tagName(Constants.TAG_ANCHOR)).isEmpty();
            if (urlEleAvail) {
                String url = commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_5_SINGLE_PRODUCT_URL_CSS_CLASS))
                        .findElement(By.tagName(Constants.TAG_ANCHOR)).getAttribute(Constants.ATTRIBUTE_HREF);
                boolean priceEleAvail = !commonElement.findElements(By.className(DOTDConstants.TYPE_5_SINGLE_PRODUCT_PRICE_CLASS)).isEmpty();
                if (priceEleAvail) {
                    String price = commonElement.findElement(By.className(DOTDConstants.TYPE_5_SINGLE_PRODUCT_PRICE_CLASS))
                            .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim();
                    DealOfTheDay entity = this.createEntity(url, Integer.parseInt(price), isPrime);
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    private List<DealOfTheDay> getType4Entities(WebDriver browser, boolean isPrime) {
        List<DealOfTheDay> entities = new ArrayList<>();
        List<WebElement> commonElements = browser.findElement(By.cssSelector(DOTDConstants.TYPE_4_MAIN_PRODUCT_CSS_CLASS))
                .findElements(By.cssSelector(DOTDConstants.TYPE_4_SINGLE_PRODUCT_COMMON_CSS_CLASS));
        for (WebElement commonElement : commonElements) {
            if (!this.isGoodReviewProduct(commonElement, "type4"))
                continue;
            boolean urlEleAvail = !commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_4_SINGLE_PRODUCT_URL_CSS_CLASS))
                    .findElements(By.tagName(Constants.TAG_ANCHOR)).isEmpty();
            if (urlEleAvail) {
                String url = commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_4_SINGLE_PRODUCT_URL_CSS_CLASS))
                        .findElement(By.tagName(Constants.TAG_ANCHOR)).getAttribute(Constants.ATTRIBUTE_HREF);
                boolean priceEleAvail = !commonElement.findElements(By.className(DOTDConstants.TYPE_4_SINGLE_PRODUCT_PRICE_CLASS)).isEmpty();
                if (priceEleAvail) {
                    String price = commonElement.findElement(By.className(DOTDConstants.TYPE_4_SINGLE_PRODUCT_PRICE_CLASS))
                            .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim();
                    DealOfTheDay entity = this.createEntity(url, Integer.parseInt(price), isPrime);
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    private List<DealOfTheDay> getType3Entities(WebDriver browser, boolean isPrime) {
        List<DealOfTheDay> entities = new ArrayList<>();
        List<WebElement> commonElements = browser.findElement(By.id(DOTDConstants.TYPE_3_MAIN_PRODUCT_ID))
                .findElements(By.cssSelector(DOTDConstants.TYPE_3_SINGLE_PRODUCT_COMMON_CSS_CLASS));
        for (WebElement commonElement : commonElements) {
            if (!this.isGoodReviewProduct(commonElement, "type3"))
                continue;
            boolean urlEleAvail = !commonElement.findElements(By.tagName(Constants.TAG_ANCHOR)).isEmpty();
            if (urlEleAvail) {
                String url = commonElement.findElement(By.tagName(Constants.TAG_ANCHOR)).getAttribute(Constants.ATTRIBUTE_HREF);
                boolean priceEleAvail = !commonElement.findElements(By.className(DOTDConstants.TYPE_3_SINGLE_PRODUCT_PRICE_CLASS)).isEmpty();
                if (priceEleAvail) {
                    String price = commonElement.findElement(By.className(DOTDConstants.TYPE_3_SINGLE_PRODUCT_PRICE_CLASS))
                            .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE)
                            .replace(Constants.UTIL_DOT, Constants.UTIL_EMPTY_QUOTE).trim();
                    DealOfTheDay entity = this.createEntity(url, Integer.parseInt(price), isPrime);
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    private List<DealOfTheDay> getType2Entities(WebDriver browser, boolean isPrime) {
        List<DealOfTheDay> entities = new ArrayList<>();
        List<WebElement> commonElements = browser.findElement(By.id(DOTDConstants.TYPE_2_MAIN_PRODUCT_ID))
                .findElements(By.xpath(DOTDConstants.TYPE_2_SINGLE_PRODUCT_COMMON_XPATH));
        for (WebElement commonElement : commonElements) {
            if (!this.isGoodReviewProduct(commonElement, "type2"))
                continue;
            boolean urlEleAvail = !commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_URL_CSS_CLASS))
                    .findElements(By.tagName(Constants.TAG_ANCHOR)).isEmpty();
            if (urlEleAvail) {
                String url = commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_URL_CSS_CLASS))
                        .findElement(By.tagName(Constants.TAG_ANCHOR)).getAttribute(Constants.ATTRIBUTE_HREF);
                boolean priceEleAvail = !commonElement.findElements(By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_PRICE_CSS_CLASS)).isEmpty();
                if (priceEleAvail) {
                    String price = commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_2_SINGLE_PRODUCT_PRICE_CSS_CLASS))
                            .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim();
                    DealOfTheDay entity = this.createEntity(url, Integer.parseInt(price), isPrime);
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    private List<DealOfTheDay> getType1Entities(WebDriver browser, boolean isPrime) {
        List<DealOfTheDay> entities = new ArrayList<>();
        List<WebElement> commonElements = browser.findElement(By.id(DOTDConstants.TYPE_1_MAIN_PRODUCT_ID))
                .findElements(By.cssSelector(DOTDConstants.TYPE_1_SINGLE_PRODUCT_COMMON_CSS_CLASS));
        for (WebElement commonElement : commonElements) {
            if (!this.isGoodReviewProduct(commonElement, "type1"))
                continue;
            boolean urlEleAvail = !commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_1_SINGLE_PRODUCT_URL_CSS_CLASS))
                    .findElements(By.tagName(Constants.TAG_ANCHOR)).isEmpty();
            if (urlEleAvail) {
                String url = commonElement.findElement(By.cssSelector(DOTDConstants.TYPE_1_SINGLE_PRODUCT_URL_CSS_CLASS))
                        .findElement(By.tagName(Constants.TAG_ANCHOR)).getAttribute(Constants.ATTRIBUTE_HREF);
                boolean priceEleAvail = !commonElement.findElements(By.className(DOTDConstants.TYPE_1_SINGLE_PRODUCT_PRICE_CLASS)).isEmpty();
                if (priceEleAvail) {
                    String price = commonElement.findElement(By.className(DOTDConstants.TYPE_1_SINGLE_PRODUCT_PRICE_CLASS))
                            .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim();
                    DealOfTheDay entity = this.createEntity(url, Integer.parseInt(price), isPrime);
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    public int getTotalPagesOfMainUrl(WebDriver browser) {
        boolean lastPage = !browser.findElements(By.className("pagnDisabled")).isEmpty();
        if (lastPage) {
            String lastPageStr = browser.findElement(By.className("pagnDisabled")).getText().trim();
            log.info("total pages " + lastPageStr);
            return Integer.parseInt(lastPageStr);
        } else {
            return todaysDealService.getTotalPageCount(browser);
        }
    }

    private void clickSeeAllResults(WebDriver browser) {
        boolean isSeeAllResultsAvail = !browser.findElements(By.cssSelector(".a-size-medium.a-color-link.a-text-bold")).isEmpty();
        if (isSeeAllResultsAvail) {
            browser.findElement(By.cssSelector(".a-size-medium.a-color-link.a-text-bold")).click();
        }
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

    private boolean checkPageUnavailable(WebDriver browser) {
        boolean currentlyUnavailable = !browser.findElements(By.className("a-spacing-base")).isEmpty();
        if (currentlyUnavailable) {
            currentlyUnavailable = browser.findElement(By.className("a-spacing-base")).getText().contains("This deal is currently unavailable");
        }
        return currentlyUnavailable;
    }

    private String getType(WebDriver browser) {
        boolean isType1Avail = !browser.findElements(By.id(DOTDConstants.TYPE_1_MAIN_PRODUCT_ID)).isEmpty();
        if (isType1Avail) {
            return "type1";
        }
        boolean isType2Avail = !browser.findElements(By.id(DOTDConstants.TYPE_2_MAIN_PRODUCT_ID)).isEmpty();
        if (isType2Avail) {
            return "type2";
        }
        boolean isType3Avail = !browser.findElements(By.id(DOTDConstants.TYPE_3_MAIN_PRODUCT_ID)).isEmpty();
        if (isType3Avail) {
            return "type3";
        }
        boolean isType4Avail = !browser.findElements(By.cssSelector(DOTDConstants.TYPE_4_MAIN_PRODUCT_CSS_CLASS)).isEmpty();
        if (isType4Avail) {
            return "type4";
        }
        boolean isType5Avail = !browser.findElements(By.cssSelector(DOTDConstants.TYPE_5_MAIN_PRODUCT_CSS_CLASS)).isEmpty();
        if (isType5Avail) {
            return "type5";
        }
        boolean isType6Avail = !browser.findElements(By.id(DOTDConstants.TYPE_6_MAIN_PRODUCT_ID)).isEmpty();
        if (isType6Avail) {
            return "type6";
        }
        log.info("unknown type for the url " + browser.getCurrentUrl());
        return "unknown";
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

    private boolean isGoodReviewProduct(WebElement element, String type) {
        boolean isGoodReviewProduct = false;
        switch (type) {
            case "type3":
                isGoodReviewProduct = this.type3ReviewsAndStarRating(element);
                break;
            case "type1":
                isGoodReviewProduct = this.type1ReviewsAndStarRating(element);
                break;
            case "type2":
                isGoodReviewProduct = this.type2ReviewsAndStarRating(element);
                break;
            case "type4":
                isGoodReviewProduct = this.type4ReviewsAndStarRating(element);
                break;
            default:
                log.info("Cannot found review type for url...");
        }
        return isGoodReviewProduct;
    }

    private boolean type2ReviewsAndStarRating(WebElement element) {
        boolean eleAvail = !element.findElements(By.xpath(DOTDConstants.TYPE_2_REVIEW_STAR_XPATH)).isEmpty();
        if (eleAvail) {
            String star = element.findElement(By.xpath(DOTDConstants.TYPE_2_REVIEW_STAR_XPATH))
                    .getAttribute("class");
            star = CharMatcher.inRange('0', '9').retainFrom(star);
            if (star.equals("4") || star.equals("45") || star.equals("5")) {
                List<WebElement> list = element.findElements(By.cssSelector(DOTDConstants.TYPE_2_REVIEW_RATING_CSS_CLASS));
                Boolean rating = isRatingAvailable(list);
                if (rating != null) return rating;
            }
        }
        return false;
    }

    private boolean type4ReviewsAndStarRating(WebElement element) {
        boolean eleAvail = !element.findElements(By.xpath(DOTDConstants.TYPE_4_REVIEW_STAR_XPATH)).isEmpty();
        if (eleAvail) {
            String star = element.findElement(By.xpath(DOTDConstants.TYPE_4_REVIEW_STAR_XPATH))
                    .getAttribute("class");
            star = CharMatcher.inRange('0', '9').retainFrom(star);
            if (star.equals("4") || star.equals("45") || star.equals("5")) {
                List<WebElement> list = element.findElements(By.className(DOTDConstants.TYPE_4_REVIEW_RATING_CLASS));
                Boolean rating = isRatingAvailable(list);
                if (rating != null) return rating;
            }
        }
        return false;
    }

    private Boolean isRatingAvailable(List<WebElement> list) {
        boolean eleAvail;
        eleAvail = !list.isEmpty();
        if (eleAvail) {
            for (WebElement ele : list) {
                eleAvail = !ele.findElements(By.tagName(Constants.TAG_ANCHOR)).isEmpty();
                if (eleAvail) {
                    String url = ele.findElement(By.tagName(Constants.TAG_ANCHOR)).getAttribute(Constants.ATTRIBUTE_HREF);
                    if (url.contains("customerReviews")) {
                        int rating = Integer.parseInt(ele.findElement(By.tagName(Constants.TAG_ANCHOR)).getText().trim());
                        return rating > 49;
                    }
                }
            }
        }
        return null;
    }

    private boolean type1ReviewsAndStarRating(WebElement element) {
        boolean eleRatingAvail = !element.findElements(By.cssSelector(DOTDConstants.TYPE_1_REVIEW_RATING_CSS_CLASS)).isEmpty();
        boolean eleStarAvail = !element.findElements(By.xpath(DOTDConstants.TYPE_1_REVIEW_STAR_XPATH)).isEmpty();
        if (eleRatingAvail && eleStarAvail) {
            String star = element.findElement(By.xpath(DOTDConstants.TYPE_1_REVIEW_STAR_XPATH))
                    .getAttribute("class");
            star = CharMatcher.inRange('0', '9').retainFrom(star);
            if (star.equals("4") || star.equals("45") || star.equals("5")) {
                int ratingStr = Integer.parseInt(element.findElement(By.cssSelector(DOTDConstants.TYPE_1_REVIEW_RATING_CSS_CLASS))
                        .getText().replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim());
                return ratingStr > 49;
            }
        }
        return false;
    }

    private boolean type3ReviewsAndStarRating(WebElement element) {
        boolean eleAvail = !element.findElements(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS)).isEmpty();
        if (eleAvail) {
            eleAvail = !element.findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS))
                    .findElements(By.tagName(Constants.TAG_I)).isEmpty();
            if (eleAvail) {
                String star = element.findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS))
                        .findElement(By.tagName(Constants.TAG_I)).getAttribute("class");
                star = CharMatcher.inRange('0', '9').retainFrom(star);
                if (star.equals("4") || star.equals("45") || star.equals("5")) {
                    eleAvail = !element.findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS))
                            .findElements(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_RATING_CSS_CLASS)).isEmpty();
                    if (eleAvail) {
                        int ratingStr = Integer.parseInt(element.findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_CSS_CLASS))
                                .findElement(By.cssSelector(DOTDConstants.TYPE_3_REVIEW_RATING_CSS_CLASS)).getText()
                                .replace(Constants.UTIL_COMMA, Constants.UTIL_EMPTY_QUOTE).trim());
                        return ratingStr > 49;
                    }
                }
            }
        }
        return false;
    }

    public List<String> primeExclusiveDealsByDepts(WebDriver browser) throws Exception {
        List<String> mainUrls = new ArrayList<>();
        List<ScheduledDepartment> todayDepts = amazonDealHelper.getScheduledDepartments();
        IntStream.range(1,todayDepts.size()).forEach(count -> ((JavascriptExecutor) browser)
                .executeScript(Constants.NEW_TAB_SCRIPT));
        List<String> tabs = new ArrayList<>(browser.getWindowHandles());
        for (String tab : tabs) {
            browser.switchTo().window(tab);
            browser.get(AmazonConstants.TODAYS_DEAL_URL);
            Thread.sleep(2000);
            prodService.clickFilters(browser);
        }
        for (int i=0;i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));
            boolean isClicked = todaysDealService.findAndClickSingleDepartment(browser, todayDepts.get(i).getDept());
            if (isClicked) {
                int totalPages = todaysDealService.getTotalPageCount(browser);
                for (int j=0;j<totalPages;j++) {

                }
            }
        }
        for (int i=0;i<tabs.size();i++) {
            browser.switchTo().window(tabs.get(i));

        }
        return mainUrls;
    }
}
