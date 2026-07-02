package com.homes.backend.domain.property.repository;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.PropertyType;
import com.homes.backend.domain.property.entity.TradeType;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    @Query("SELECT DISTINCT p FROM Property p LEFT JOIN p.tags t " +
            "WHERE function('ST_Contains', :boundingBox, p.coordinate) = true " +
            "AND (:tradeType IS NULL OR p.tradeType = :tradeType) " +
            "AND (:propertyType IS NULL OR p.propertyType = :propertyType) " +
            "AND (:minDeposit IS NULL OR p.deposit >= :minDeposit) " +
            "AND (:maxDeposit IS NULL OR p.deposit <= :maxDeposit) " +
            "AND (:minMonthlyRent IS NULL OR p.monthlyRent >= :minMonthlyRent) " +
            "AND (:maxMonthlyRent IS NULL OR p.monthlyRent <= :maxMonthlyRent) " +
            "AND (:keyword IS NULL OR t LIKE :keyword) " +
            "ORDER BY p.id DESC")// 최신 등록순
    List<Property> findPropertiesByMapAndFilters(
            @Param("boundingBox") Polygon boundingBox,
            @Param("tradeType") TradeType tradeType,
            @Param("propertyType") PropertyType propertyType,
            @Param("minDeposit") Integer minDeposit,
            @Param("maxDeposit") Integer maxDeposit,
            @Param("minMonthlyRent") Integer minMonthlyRent,
            @Param("maxMonthlyRent") Integer maxMonthlyRent,
            @Param("keyword") String keyword
    );

    List<Property> findAllByUserId(Long userId);
    List<Property> findAllByOrderByIdDesc(); // 최신 등록순

    /**
     * 찜 개수 증가
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Property p SET p.favoriteCount = p.favoriteCount + 1 WHERE p.id = :propertyId")
    void increaseFavoriteCount(@Param("propertyId") Long propertyId);

    /**
     * 찜 개수 감소 (0 미만으로 떨어지지 않게 보호)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Property p SET p.favoriteCount = p.favoriteCount - 1 WHERE p.id = :propertyId AND p.favoriteCount > 0")
    void decreaseFavoriteCount(@Param("propertyId") Long propertyId);
}
