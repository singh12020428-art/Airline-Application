package com.zosh.services.repository;

import com.zosh.services.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface CityRepository extends JpaRepository<City, Long> {

    boolean existsByCityCode(String cityCode);

    boolean existsByCityCodeAndIdNot(String cityCode, Long id);

    Page<City> findByCountryCodeIgnoreCase(String countryCode, Pageable pageable);

    @Query("""
           SELECT c FROM City c
           WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.cityCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.countryCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.countryName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.regionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    Page<City> searchByKeyword(String keyword, Pageable pageable);
}
