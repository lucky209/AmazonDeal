package offer.compass.amazondeal.entities;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepo extends JpaRepository<Department, Integer> {
    List<Department> findByEnabled(boolean enabled);
}
