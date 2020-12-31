package offer.compass.amazondeal.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface TodaysDealUrlRepo extends JpaRepository<TodaysDealUrl, Integer> {
    @Transactional
    @Modifying
    @Query(value = "delete from amazonservice.todays_deal_url;" +
            "ALTER SEQUENCE amazonservice.todays_deal_url_id_seq RESTART WITH 1;", nativeQuery = true)
    void deleteAllRecords();

    TodaysDealUrl findByUrl(String url);
}
