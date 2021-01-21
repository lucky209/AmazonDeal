package offer.compass.amazondeal.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface DealOfTheDayRepo extends JpaRepository<DealOfTheDay, String> {
    @Transactional
    @Modifying
    @Query(value = "delete from amazonservice.deal_of_the_day where dept !='Prime'", nativeQuery = true)
    void deleteAllNonPrimeRecords();

    @Query(value = "select count(url) from amazonservice.deal_of_the_day where dept='Prime'", nativeQuery = true)
    int countOfPrimeDeals();

    @Transactional
    @Modifying
    @Query(value = "delete from amazonservice.deal_of_the_day dod where dod.dept='Prime'", nativeQuery = true)
    void deleteAllPrimeRecords();

    @Query(value = "select * from amazonservice.deal_of_the_day dod where dod.dept='Prime'", nativeQuery = true)
    List<DealOfTheDay> findAllPrimeDealUrls();
}
