package com.homes.backend.domain.property.repository;

import com.homes.backend.domain.property.entity.Property;

import java.util.List;

public interface PropertyRepositoryCustom {
    List<Property> findHybridRecommendations(Double minPrice, Double maxPrice, String preferredRegion, List<Long> recentViewedIds, int limit);
}
