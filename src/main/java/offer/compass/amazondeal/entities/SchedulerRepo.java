package offer.compass.amazondeal.entities;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulerRepo extends JpaRepository<SchedulerControl, Integer> {
    SchedulerControl findByScheduler(String name);
}
