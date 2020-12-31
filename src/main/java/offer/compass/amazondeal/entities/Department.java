package offer.compass.amazondeal.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(schema = "amazonservice")
public class Department {
    @Id
    private Integer id;
    private String deptName;
    private String parentDept;
    private boolean enabled;
    private String createdDate;
}
