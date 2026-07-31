package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.BidIncrementTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidIncrementTierRepository extends JpaRepository<BidIncrementTier, Short> {
    List<BidIncrementTier> findByIsActiveTrue();

    /**
     * BR-B-003 · Verilen fiyatın düştüğü aktif kademeyi bulur.
     * ex_tiers_no_overlap kısıtı sayesinde tam olarak bir kademe eşleşir.
     */
    @Query("SELECT t FROM BidIncrementTier t WHERE t.isActive = true "
            + "AND t.minAmount <= :price AND (t.maxAmount IS NULL OR t.maxAmount > :price)")
    Optional<BidIncrementTier> findMatchingTier(@Param("price") BigDecimal price);
}
