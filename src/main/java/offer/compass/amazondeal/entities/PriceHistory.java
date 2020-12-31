package offer.compass.amazondeal.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "price_history_test", schema = "amazonservice")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "amazonservice.price_history_id_seq")
    @SequenceGenerator(name = "amazonservice.price_history_id_seq", sequenceName = "amazonservice.price_history_id_seq", allocationSize = 1)
    private int id;
    private String site;
    private String url;
    private String productName;
    private String dropChances;
    private Integer lowestPrice;
    private Integer highestPrice;
    private Integer currentPrice;
    private boolean isGoodOffer;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}