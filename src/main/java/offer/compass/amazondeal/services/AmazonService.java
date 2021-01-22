package offer.compass.amazondeal.services;

public interface AmazonService {

    boolean getUrlsByDepartment() throws Exception;

    boolean getDealOfTheDayUrls() throws Exception;

    boolean getPrimeExclusiveUrls() throws InterruptedException;
}
