package offer.compass.amazondeal.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface PriceHistoryRepo extends JpaRepository<PriceHistory, Integer> {
    @Transactional
    @Modifying
    @Query(value = "delete from amazonservice.price_history;" +
            "ALTER SEQUENCE amazonservice.price_history_id_seq RESTART WITH 1;", nativeQuery = true)
    void deleteAllRecords();

    @Query(value = "select * from amazonservice.price_history where is_good_offer=true", nativeQuery = true)
    List<PriceHistory> getGoodOfferProducts();
}
