package offer.compass.amazondeal.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todays_deal_url")
@Data
public class TodaysDealUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "amazonservice.todays_deal_url_id_seq")
    @SequenceGenerator(name = "amazonservice.todays_deal_url_id_seq", sequenceName = "offercompass.todays_deal_url_id_seq", allocationSize = 1)
    private int id;
    private String url;
    private String dept;
    private Integer price;
    private Boolean prime;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
