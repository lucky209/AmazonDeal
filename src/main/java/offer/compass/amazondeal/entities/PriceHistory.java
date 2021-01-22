package offer.compass.amazondeal.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "price_history")
public class PriceHistory {
    private String site;
    @Id
    private String url;
    private String productName;
    private String dropChances;
    private Integer lowestPrice;
    private Integer highestPrice;
    private Integer currentPrice;
    private boolean isGoodOffer;
    private String shortUrl;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}