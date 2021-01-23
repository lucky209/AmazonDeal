package offer.compass.amazondeal.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

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
    private boolean isDotd;
    private String shortUrl;
    @CreationTimestamp
    private LocalDate createdDate;
    @UpdateTimestamp
    private LocalDate updatedDate;
}