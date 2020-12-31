package offer.compass.amazondeal.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(schema = "offercompass")
public class Property {
    @Id
    private int id;
    private String propName;
    private String propValue;
    private boolean enabled;
}
