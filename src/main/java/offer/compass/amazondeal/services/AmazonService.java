package offer.compass.amazondeal.services;

import offer.compass.amazondeal.entities.Department;

import java.util.List;

public interface AmazonService {

    List<Department> loadDepartments() throws InterruptedException;

    boolean getUrlsByDepartment() throws Exception;

    boolean getDealOfTheDayUrls() throws Exception;

    boolean getPrimeExclusiveUrls() throws InterruptedException;
}
