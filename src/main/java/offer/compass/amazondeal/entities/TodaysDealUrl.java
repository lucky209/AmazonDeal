package offer.compass.amazondeal.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "todays_deal_url")
public class TodaysDealUrl {
    @Id
    private String url;
    private String dept;
    private Integer price;
    private Boolean prime;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
