package offer.compass.amazondeal.services;

import offer.compass.amazondeal.entities.Department;

import java.io.IOException;
import java.util.List;

public interface AmazonService {

    List<Department> loadDepartments() throws InterruptedException;

    boolean getUrlsByDepartment() throws Exception;

    boolean getPriceHistoryByUrls() throws InterruptedException, IOException;
}
