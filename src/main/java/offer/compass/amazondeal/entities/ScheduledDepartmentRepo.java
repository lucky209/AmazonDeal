package offer.compass.amazondeal.entities;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledDepartmentRepo extends JpaRepository<ScheduledDepartment, Integer> {

    List<ScheduledDepartment> findByDayNo(int day);

}
