# Airline System Backend API Documentation

## airline-core-service

| Method | Path | Controller |
|---|---|---|
| GET | /api/aircrafts/{id} | AircraftController.java |
| PUT | /api/aircrafts/{id} | AircraftController.java |
| DELETE | /api/aircrafts/{id} | AircraftController.java |
| GET | /api/airlines/admin | AirlineController.java |
| GET | /api/airlines/{id} | AirlineController.java |
| GET | /api/airlines/dropdown | AirlineController.java |
| DELETE | /api/airlines/{id} | AirlineController.java |
| POST | /api/airlines/{id}/approve | AirlineController.java |
| POST | /api/airlines/{id}/suspend | AirlineController.java |
| POST | /api/airlines/{id}/ban | AirlineController.java |

## ancillary-service

| Method | Path | Controller |
|---|---|---|
| GET | /api/ancillaries/{id} | AncillaryController.java |
| PUT | /api/ancillaries/{id} | AncillaryController.java |
| DELETE | /api/ancillaries/{id} | AncillaryController.java |
| GET | /api/flight-cabin-ancillaries/{id} | FlightCabinAncillaryController.java |
| GET | /api/flight-cabin-ancillaries/flight/{flightId}/cabin/{cabinClassId} | FlightCabinAncillaryController.java |
| GET | /api/flight-cabin-ancillaries/flight/{flightId}/cabin/{cabinClassId}/type/{type} | FlightCabinAncillaryController.java |
| GET | /api/flight-cabin-ancillaries/flight/{flightId}/cabin/{cabinClassId}/type/{type}/all | FlightCabinAncillaryController.java |
| PUT | /api/flight-cabin-ancillaries/{id} | FlightCabinAncillaryController.java |
| DELETE | /api/flight-cabin-ancillaries/{id} | FlightCabinAncillaryController.java |
| POST | /api/flight-cabin-ancillaries/price/total | FlightCabinAncillaryController.java |
| POST | /api/flight-meals/price/total | FlightMealController.java |
| GET | /api/flight-meals/{id} | FlightMealController.java |
| GET | /api/flight-meals/flight/{flightId} | FlightMealController.java |
| PATCH | /api/flight-meals/{id}/availability | FlightMealController.java |
| DELETE | /api/flight-meals/{id} | FlightMealController.java |
| PUT | /api/insurance-coverages/{id} | InsuranceCoverageController.java |
| DELETE | /api/insurance-coverages/{id} | InsuranceCoverageController.java |
| GET | /api/insurance-coverages/{id} | InsuranceCoverageController.java |
| GET | /api/insurance-coverages/ancillary/{ancillaryId} | InsuranceCoverageController.java |
| GET | /api/insurance-coverages/ancillary/{ancillaryId}/active | InsuranceCoverageController.java |
| GET | /api/meals/{id} | MealController.java |
| PUT | /api/meals/{id} | MealController.java |
| PATCH | /api/meals/{id}/availability | MealController.java |
| DELETE | /api/meals/{id} | MealController.java |

## booking-service

| Method | Path | Controller |
|---|---|---|
| GET | /api/bookings/{id} | BookingController.java |
| GET | /api/bookings/airline | BookingController.java |
| GET | /api/bookings/user/history | BookingController.java |
| PATCH | /api/bookings/{id}/cancel | BookingController.java |
| DELETE | /api/bookings/{id} | BookingController.java |

## flight-ops-service

| Method | Path | Controller |
|---|---|---|
| GET | /api/flights/{id} | FlightController.java |
| GET | /api/flights/airline | FlightController.java |
| PUT | /api/flights/{id} | FlightController.java |
| PATCH | /api/flights/{id}/status | FlightController.java |
| DELETE | /api/flights/{id} | FlightController.java |
| GET | /api/flight-instances/{id} | FlightInstanceController.java |
| PUT | /api/flight-instances/{id} | FlightInstanceController.java |
| DELETE | /api/flight-instances/{id} | FlightInstanceController.java |
| GET | /api/flight-schedules/{id} | FlightScheduleController.java |
| PUT | /api/flight-schedules/{id} | FlightScheduleController.java |
| DELETE | /api/flight-schedules/{id} | FlightScheduleController.java |
| GET | /api/flights/search | FlightSearchController.java |

## location-service

| Method | Path | Controller |
|---|---|---|
| GET | /api/airports/{id} | AirportController.java |
| GET | /api/airports/city/{cityId} | AirportController.java |
| PUT | /api/airports/{id} | AirportController.java |
| DELETE | /api/airports/{id} | AirportController.java |
| GET | /api/cities/{id} | CityController.java |
| PUT | /api/cities/{id} | CityController.java |
| DELETE | /api/cities/{id} | CityController.java |
| GET | /api/cities/search | CityController.java |
| GET | /api/cities/country/{countryCode} | CityController.java |
| GET | /api/cities/exists/{cityCode} | CityController.java |

## payment-service

| Method | Path | Controller |
|---|---|---|
| POST | /api/payments/initiate | PaymentController.java |
| POST | /api/payments/verify | PaymentController.java |
| POST | /api/payments/batch/bookings | PaymentController.java |

## pricing-service

| Method | Path | Controller |
|---|---|---|
| GET | /api/baggage-policies/{id} | BaggagePolicyController.java |
| GET | /api/baggage-policies/fare/{fareId} | BaggagePolicyController.java |
| GET | /api/baggage-policies/airline/{airlineId} | BaggagePolicyController.java |
| PUT | /api/baggage-policies/{id} | BaggagePolicyController.java |
| DELETE | /api/baggage-policies/{id} | BaggagePolicyController.java |
| GET | /api/fares/{id} | FareController.java |
| GET | /api/fares/flight/{flightId}/cabin-class/{cabinClassId} | FareController.java |
| GET | /api/fares/lowest/flight/{flightId}/cabin-class/{cabinClassId} | FareController.java |
| PUT | /api/fares/{id} | FareController.java |
| DELETE | /api/fares/{id} | FareController.java |
| POST | /api/fares/batch-by-ids | FareController.java |
| POST | /api/fares/search | FareController.java |
| GET | /api/fare-rules/{id} | FareRuleController.java |
| GET | /api/fare-rules/fare/{fareId} | FareRuleController.java |
| GET | /api/fare-rules/airline/{airlineId} | FareRuleController.java |
| PUT | /api/fare-rules/{id} | FareRuleController.java |
| DELETE | /api/fare-rules/{id} | FareRuleController.java |

## seat-service

| Method | Path | Controller |
|---|---|---|
| GET | /api/cabin-classes/{id} | CabinClassController.java |
| GET | /api/cabin-classes/aircraft/{id}/name/{cabinClass} | CabinClassController.java |
| GET | /api/cabin-classes/aircraft/{aircraftId} | CabinClassController.java |
| PUT | /api/cabin-classes/{id} | CabinClassController.java |
| DELETE | /api/cabin-classes/{id} | CabinClassController.java |
| GET | /api/flight-instance-cabins/{id} | FlightInstanceCabinController.java |
| GET | /api/flight-instance-cabins/flight-instance/{flightInstanceId} | FlightInstanceCabinController.java |
| GET | /api/flight-instance-cabins/flight-instance/{flightInstanceId}/cabin-class/{cabinClassId} | FlightInstanceCabinController.java |
| PUT | /api/flight-instance-cabins/{id} | FlightInstanceCabinController.java |
| DELETE | /api/flight-instance-cabins/{id} | FlightInstanceCabinController.java |
| POST | /api/seat-instances/calculate/seat/price | SeatInstanceController.java |
| GET | /api/seat-maps/{id} | SeatMapController.java |
| GET | /api/seat-maps/cabin-class/{cabinClassId} | SeatMapController.java |
| PUT | /api/seat-maps/{id} | SeatMapController.java |
| DELETE | /api/seat-maps/{id} | SeatMapController.java |

## user-service

| Method | Path | Controller |
|---|---|---|
| POST | /auth/signup | AuthController.java |
| POST | /auth/login | AuthController.java |
| GET | /api/users/profile | UserController.java |
| GET | /api/users/{userId} | UserController.java |
| GET | /api/users | UserController.java |

