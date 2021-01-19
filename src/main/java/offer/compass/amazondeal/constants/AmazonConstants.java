package offer.compass.amazondeal.constants;

public class AmazonConstants {

    public static final String TODAYS_DEAL_URL = "https://www.amazon.in/gp/goldbox?ref_=nav_cs_gb";
    public static final String LAST_BUTTON_CLASS_NAME = "a-last";
    public static final String TODAYS_DEAL_PRODUCT_LINK_ID = "dealImage";
    public static final String TODAYS_DEAL_REGULAR_PRICE_DIV_ID = "regularBuybox";
    public static final String TODAYS_DEAL_REVIEW_ID = "averageCustomerReviews_feature_div";
    public static final String TODAYS_DEAL_PRODUCT_XPATH = "//*[starts-with(@id,'100_dealView_')]";
    public static final String TODAYS_DEAL_PRICE_CSS_CLASS = ".gb-font-size-medium.inlineBlock.unitLineHeight.dealPriceText";
    public static final String DEPARTMENTS_CONTAINER_XPATH = "//*[@id=\"widgetFilters\"]/div[1]/div[2]";
    public static final String SEE_MORE_DEPT_XPATH = "//*[@id=\"widgetFilters\"]/div[1]/div[2]/a/span";
    public static final String DEPARTMENTS_CSS_CLASS = ".a-label.a-checkbox-label";
    public static final String FILTER_CSS_CLASS = ".a-row.a-spacing-small.filterItem";
    public static final String AVAILABILITY_FILTER = "Availability";
    public static final String ACTIVE_FILTER_CSS_CLASS = ".a-label.a-checkbox-label";
    public static final String PRICE_ID_V1 = "priceblock_dealprice";
    public static final String PRICE_ID_V2 = "priceblock_ourprice";

    public static final String REVIEW_CSS_CLASS = ".a-size-small.a-color-base";
    public static int TODAYS_DEAL_PRODUCTS_COUNT = 0;

    //deal of the day constants
    public static final String DOTD_MAIN_AS_SINGLE_PRODUCT_ID = "productTitle";
    public static final String DOTD_TOTAL_PAGE_DIV_ID = "shovlPagination";
    public static final String DOTD_MAIN_DIV_ID = "widgetContent";
    public static final String DOTD_MAIN_URL_ID = "dealTitle";
    public static final String DOTD_NEXT_BUTTON_DIV_ID = "nextShovlButton";

    public static final String PAGINATION_CSS_CLASS = ".a-pagination";
    public static final String PAGINATION_DOTS = "...";
    public static final String PAGINATION_PREV = "←Previous";
    public static final String PAGINATION_NEXT = "Next→";
    public static final String PAGINATION_NEXT_LINE = "\n";
    public static final String UTIL_NEXT = "next";
    public static final String UTIL_AMAZON_BRAND = "amazon-brand";

    public static final String UTIL_AVG_CUSTOMER_REVIEW = "Avg. Customer Review";
    public static final String VALIDATION_UTIL_STRING_ADD_TO_CART = "Add to Cart";
    public static final String PATH_TO_SAVE_SS = "D:\\OfferCompass\\images\\";

    public static final String PRICE_HISTORY_LOWEST_HIGHEST_PRICE_TABLE_HTML=
            "<div>" +
                "<span id="+ PriceHistoryConstants.PRICE_HISTORY_DIV +" style=\"width: 320px;\" class=\"a-size-base priceBlockDealPriceString\">" +
                    "<b>Lowest Price:</b>" +
                    "<span>" +
                        "₹$lowestPrice.00      " +
                    "</span>" +
                    "<b>Highest Price:</b> " +
                    "<span>" +
                        "₹$highestPrice.00" +
                    "</span>" +
                "</span>" +
                "<div style=\"color:blue;font-size:12.5px;\"> Lowest price and highest price are taken from https://pricehistory.in</div>" +
            "</div>";
}
