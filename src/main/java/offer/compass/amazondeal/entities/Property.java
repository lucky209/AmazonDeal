package offer.compass.amazondeal.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Property {
    @Id
    private int id;
    private String propName;
    private String propValue;
    private boolean enabled;
}
