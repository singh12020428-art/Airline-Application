package com.zosh.services;

import com.zosh.payload.request.MealRequest;
import com.zosh.payload.response.MealResponse;
import java.util.List;

public interface MealService {
    MealResponse createMeal(Long userId, MealRequest request) throws Exception;
    MealResponse getMealById(Long id) throws Exception;
    MealResponse updateMeal(Long userId, Long id, MealRequest request) throws Exception;
    List<MealResponse> getByAirlineId(Long userId, String roles);
    void deleteMeal(Long id) throws Exception;
    MealResponse updateAvailability(Long id, Boolean availability) throws Exception;
}
