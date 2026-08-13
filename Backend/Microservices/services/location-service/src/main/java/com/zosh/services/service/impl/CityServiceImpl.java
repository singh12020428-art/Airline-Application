package com.zosh.services.service.impl;

import com.zosh.payload.request.CityRequest;
import com.zosh.payload.response.CityResponse;
import com.zosh.services.mapper.CityMapper;
import com.zosh.services.model.City;
import com.zosh.services.repository.CityRepository;
import com.zosh.services.service.CityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    // ---------- Core CRUD ----------

    @Override
    public CityResponse createCity(CityRequest request) throws Exception {

        if (cityRepository.existsByCityCode(request.getCityCode())) {
            throw new Exception("City with code " + request.getCityCode() + " already exists");
        }

        City city = CityMapper.toEntity(request);
        City result = cityRepository.save(city);

        log.info("City created: {} ({})", result.getName(), result.getCityCode());
        return CityMapper.toResponse(result);
    }


    @Override
    public CityResponse getCityById(Long id) throws Exception {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new Exception("City not found with id: " + id));
        return CityMapper.toResponse(city);
    }


    @Override
    public CityResponse updateCity(Long id, CityRequest request) throws Exception {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new Exception("City not found with id: " + id));

        if (cityRepository.existsByCityCodeAndIdNot(request.getCityCode(), id)) {
            throw new Exception("City with code " + request.getCityCode() + " already exists");
        }

        City updatedCity = cityRepository.save(CityMapper.updateEntity(city, request));

        log.info("City updated: {} ({})", updatedCity.getName(), updatedCity.getCityCode());
        return CityMapper.toResponse(updatedCity);
    }

    @Override
    public void deleteCity(Long id) throws Exception {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new Exception("City not found with id: " + id));
        cityRepository.delete(city);
        log.info("City deleted: {} ({})", city.getName(), city.getCityCode());
    }

    @Override
    public Page<CityResponse> getAllCities(Pageable pageable) {
        return cityRepository.findAll(pageable).map(CityMapper::toResponse);
    }

    // ---------- Search & Query ----------

    @Override
    public Page<CityResponse> searchCities(String keyword, Pageable pageable) {
        return cityRepository.searchByKeyword(keyword, pageable)
                .map(CityMapper::toResponse);
    }

    @Override
    public Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable) {
        return cityRepository.findByCountryCodeIgnoreCase(countryCode, pageable)
                .map(CityMapper::toResponse);
    }



    // ---------- Validation ----------

    @Override
    public boolean cityExists(String cityCode) {

        return cityRepository.existsByCityCode(cityCode);
    }
}
