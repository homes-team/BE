package com.homes.backend.domain.property.repository;

import com.homes.backend.domain.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findAllByUserId(Long userId);
}
