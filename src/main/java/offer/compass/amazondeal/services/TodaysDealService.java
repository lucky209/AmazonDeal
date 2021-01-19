package offer.compass.amazondeal.services;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.constants.AmazonConstants;
import offer.compass.amazondeal.constants.Constants;
import offer.compass.amazondeal.constants.PropertyConstants;
import offer.compass.amazondeal.entities.Department;
import offer.compass.amazondeal.entities.DepartmentRepo;
import offer.compass.amazondeal.entities.PropertiesRepo;
import offer.compass.amazondeal.helpers.BrowserHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TodaysDealService {

    @Autowired
    private BrowserHelper browserHelper;
    @Autowired
    private DepartmentRepo departmentRepo;
    @Autowired
    private PropertiesRepo propertiesRepo;

    public void clickNextButton(WebDriver browser) throws InterruptedException {
        List<WebElement> webElementList = browser.findElements(
                By.className(AmazonConstants.LAST_BUTTON_CLASS_NAME));
        if (webElementList.size() > 0) {
            webElementList = webElementList.stream().filter(
                    element -> element.getText().toLowerCase().contains(AmazonConstants.UTIL_NEXT)).collect(Collectors.toList());
            webElementList.get(0).click();
            Thread.sleep(3000);
        }
    }

    private HashMap<String, Integer> getSinglePageUrlsAndPrice(WebDriver browser) {
        List<WebElement> prodList = browserHelper.getWebElemnetsByXpath(
                browser, AmazonConstants.TODAYS_DEAL_PRODUCT_XPATH);
        prodList = this.validateProductList(prodList);
        HashMap<String, Integer> urlPriceMap = new HashMap<>();
        prodList.forEach(product -> {
            String url = product.findElement(By.id(AmazonConstants.TODAYS_DEAL_PRODUCT_LINK_ID))
                    .getAttribute(Constants.ATTRIBUTE_HREF);
            if (!url.toLowerCase().contains(AmazonConstants.UTIL_AMAZON_BRAND)) {
                String strPrice = product.findElement(By.cssSelector(
                        AmazonConstants.TODAYS_DEAL_PRICE_CSS_CLASS))
                        .getText().trim();
                Integer price = Integer.parseInt(strPrice.replace("₹","")
                        .replace(",",""));
                urlPriceMap.put(url, price);
            }
        });
        return urlPriceMap;
    }

    private List<WebElement> validateProductList(List<WebElement> prodList) {
        prodList = prodList.stream().filter(prod -> prod.getAttribute(
                Constants.VALIDATION_ATTRIBUTE_ID).length() < 16).collect(Collectors.toList());
        this.filterByReviewCount(prodList);
        return prodList.stream().filter(prod -> prod.getText().contains(
                AmazonConstants.VALIDATION_UTIL_STRING_ADD_TO_CART)).collect(Collectors.toList());
    }

    public HashMap<String, Integer> getAllPagesUrlsAndPrice(WebDriver browser)
            throws InterruptedException {
        HashMap<String, Integer> urlsPriceMap = new HashMap<>();
        int lastPage = this.getTotalPageCount(browser);
        int pageCount = 1;
        while (pageCount <= lastPage) {
            HashMap<String, Integer> pageUrlsPriceMap = this.getSinglePageUrlsAndPrice(browser);
            urlsPriceMap.putAll(pageUrlsPriceMap);
            this.clickNextButton(browser);
            pageCount++;
        }
        browser.quit();
        return urlsPriceMap;
    }

    public List<String> getAllPagesPrimeUrls(WebDriver browser)
            throws InterruptedException {
        List<String> primeUrls = new ArrayList<>();
        int lastPage = this.getTotalPageCount(browser);
        int pageCount = 1;
        while (pageCount <= lastPage) {
            List<String> primeSinglePageUrls = this.getSinglePagePrimeUrls(browser);
            primeUrls.addAll(primeSinglePageUrls);
            this.clickNextButton(browser);
            pageCount++;
        }
        return primeUrls;
    }

    private List<String> getSinglePagePrimeUrls(WebDriver browser) {
        List<WebElement> prodList = browser.findElements(By.xpath(AmazonConstants.TODAYS_DEAL_PRODUCT_XPATH));
        prodList = this.validatePrimeProductList(prodList);
        List<String> primeUrls = new ArrayList<>();
        prodList.forEach(product -> {
            if (!product.findElements(By.id(AmazonConstants.TODAYS_DEAL_PRODUCT_LINK_ID)).isEmpty()) {
                String url = product.findElement(By.id(AmazonConstants.TODAYS_DEAL_PRODUCT_LINK_ID))
                        .getAttribute(Constants.ATTRIBUTE_HREF);
                primeUrls.add(url);
            }
        });
        return primeUrls;
    }

    private List<WebElement> validatePrimeProductList(List<WebElement> prodList) {
        prodList = prodList.stream().filter(prod -> {
            if (prod.getAttribute(Constants.VALIDATION_ATTRIBUTE_ID) != null) {
                return prod.getAttribute(Constants.VALIDATION_ATTRIBUTE_ID).length() < 16;
            }
            return false;
        }).collect(Collectors.toList());
        return prodList.stream().filter(prod -> {
            if (prod.getText() != null) {
                return !prod.getText().contains(
                        AmazonConstants.VALIDATION_UTIL_STRING_ADD_TO_CART);
            }
            return false;
        }).collect(Collectors.toList());
    }

    List<Department> loadDepartments(WebDriver browser) {
        int deptId = 1;
        WebElement webElement = browser.findElement(By.xpath(AmazonConstants.DEPARTMENTS_CONTAINER_XPATH));
        this.clickSeeMoreDept(browser);
        List<WebElement> elements = webElement.findElements(By.cssSelector(AmazonConstants.DEPARTMENTS_CSS_CLASS));
        for (WebElement element : elements) {
            this.saveDepartment(element, deptId);
            deptId++;
        }
        log.info("::: Response is fetched, quitting the browser.");
        browser.quit();
        return departmentRepo.findAll();
    }

    private void saveDepartment(WebElement element, int deptId) {
        Department department = new Department();
        department.setId(deptId);
        department.setDeptName(element.getText().trim());
        department.setEnabled(true);
        department.setCreatedDate(LocalDate.now().toString());
        departmentRepo.save(department);
    }

    public synchronized boolean findAndClickSingleDepartment(WebDriver browser, String deptName) throws InterruptedException {
        this.clickSeeMoreDept(browser);
        log.info("::: Clicking the department " + deptName);
        List<WebElement> elements = browser.findElements(By.cssSelector(AmazonConstants.DEPARTMENTS_CSS_CLASS));
        Optional<WebElement> optionalWebElement = elements.stream().filter(webElement -> webElement.getText().
                trim().equals(deptName)).findFirst();
        if (optionalWebElement.isPresent()) {
            optionalWebElement.get().click();
            log.info("::: " + deptName + " clicked.");
            Thread.sleep(3000);
            return true;
        } else {
            log.info("::: " + deptName + " department not available to click.");
            browser.quit();
            return false;
        }
    }

    private void clickSeeMoreDept(WebDriver browser) {
        boolean isSeeMoreDeptFound = !browser.findElements(By.xpath(AmazonConstants.SEE_MORE_DEPT_XPATH))
                .isEmpty();
        if (isSeeMoreDeptFound)
            browser.findElement(By.xpath(AmazonConstants.SEE_MORE_DEPT_XPATH)).click();
    }

    public synchronized int getTotalPageCount(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.cssSelector(AmazonConstants.PAGINATION_CSS_CLASS));
        int lastPage = 1;
        for (WebElement element : elements) {
            String text = element.getText();
            if (!text.isEmpty()) {
                text = text.replace(AmazonConstants.PAGINATION_PREV, Constants.UTIL_EMPTY_QUOTE)
                        .replace(AmazonConstants.PAGINATION_NEXT, Constants.UTIL_EMPTY_QUOTE)
                        .replace(AmazonConstants.PAGINATION_DOTS, Constants.UTIL_EMPTY_QUOTE)
                        .replace(AmazonConstants.PAGINATION_NEXT_LINE, Constants.UTIL_SINGLE_SPACE).trim();
                String[] pageNumbers = text.split(Constants.UTIL_SINGLE_SPACE);
                for (String pageNumber : pageNumbers) {
                    if (!pageNumber.isEmpty())
                        if (Integer.parseInt(pageNumber) > lastPage)
                            lastPage = Integer.parseInt(pageNumber);
                }
                break;
            }
        }
        log.info("::: Total pages are " + lastPage);
        return lastPage;
    }

    void clickReviewFilter(WebDriver browser) throws Exception {
        try {
            List<WebElement> filters = browser.findElements(By.cssSelector(AmazonConstants.FILTER_CSS_CLASS));
            if (!filters.isEmpty()) {
                Optional<WebElement> reviewFilter = filters.stream().
                        filter(filter -> filter.getText().contains(AmazonConstants.UTIL_AVG_CUSTOMER_REVIEW)).findFirst();
                if (reviewFilter.isPresent()) {
                    List<WebElement> reviews = reviewFilter.get().findElements(By.tagName(Constants.TAG_ANCHOR));
                    if (propertiesRepo.findByPropName(PropertyConstants.FOUR_STAR_UP).isEnabled())
                        reviews.get(0).findElement(By.tagName(Constants.TAG_SPAN)).click();
                    else if (propertiesRepo.findByPropName(PropertyConstants.THREE_STAR_UP).isEnabled())
                        reviews.get(1).findElement(By.tagName(Constants.TAG_SPAN)).click();
                    Thread.sleep(2500);
                }
            }
        } catch (Exception ex) {
            throw new Exception("Cannot find star filter. Exception is " + ex.getMessage());
        }
    }

    void clickActiveProducts(WebDriver browser) throws Exception {
        try {
            List<WebElement> filters = browser.findElements(By.cssSelector(AmazonConstants.FILTER_CSS_CLASS));
            if (!filters.isEmpty()) {
                Optional<WebElement> availabilityFilter = filters.stream().filter(filter -> filter.getText().
                        contains(AmazonConstants.AVAILABILITY_FILTER)).findFirst();
                availabilityFilter.ifPresent(
                        webElement -> webElement.findElement(By.cssSelector(AmazonConstants.ACTIVE_FILTER_CSS_CLASS))
                                .click()
                );
                Thread.sleep(2500);
            }
        } catch (Exception ex) {
            throw new Exception("Cannot find active products checkbox.Exception is " + ex.getMessage());
        }
    }

    void clickDiscountFilter(WebDriver browser) throws Exception {
        try {
            if (propertiesRepo.findByPropName(PropertyConstants.DISCOUNT_PERCENTAGE).isEnabled()) {
                String discount = propertiesRepo.findByPropName(PropertyConstants.DISCOUNT_PERCENTAGE).getPropValue();
                switch (discount) {
                    case "25%":
                        browser.findElement(By.xpath("//*[@id=\"widgetFilters\"]/div[5]/span[2]/div/a")).click();
                        break;
                    case "50%":
                        browser.findElement(By.xpath("//*[@id=\"widgetFilters\"]/div[5]/span[3]/div/a")).click();
                        break;
                    case "70%":
                        browser.findElement(By.xpath("//*[@id=\"widgetFilters\"]/div[5]/span[4]/div/a")).click();
                        break;
                    default:
                        browser.findElement(By.xpath("//*[@id=\"widgetFilters\"]/div[5]/span[1]/div/a")).click();
                }
                Thread.sleep(2500);
            }
        } catch (Exception ex) {
            throw new Exception("Cannot find discount filter.Exception is " + ex.getMessage());
        }
    }

    private void filterByReviewCount(List<WebElement> prodList) {
        int reviewThreshold = Integer.parseInt(propertiesRepo.findByPropName(PropertyConstants.REVIEW_THRESHOLD).getPropValue());
        List<WebElement> removalList = new ArrayList<>();
        prodList.forEach(product -> {
            boolean isReviewAvailable = !product.findElements(By.cssSelector(AmazonConstants.REVIEW_CSS_CLASS)).isEmpty();
            if (isReviewAvailable) {
                int ratings = Integer.parseInt(product.findElement(By.cssSelector(AmazonConstants.REVIEW_CSS_CLASS)).getText());
                if (ratings < reviewThreshold) {
                    removalList.add(product);
                }
            } else {
                removalList.add(product);
            }
        });
        if (removalList.size() > 0) {
            prodList.removeAll(removalList);
        }
    }
}
