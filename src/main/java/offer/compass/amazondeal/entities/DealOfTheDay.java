package offer.compass.amazondeal.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class DealOfTheDay {
    @Id
    private String url;
    private String dept;
    private Integer price;
    private LocalDateTime createdDate;
}
