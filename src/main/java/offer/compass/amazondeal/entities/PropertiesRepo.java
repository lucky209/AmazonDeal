package offer.compass.amazondeal.entities;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertiesRepo extends JpaRepository<Property,Integer> {
    Property findByPropName(String name);
}
