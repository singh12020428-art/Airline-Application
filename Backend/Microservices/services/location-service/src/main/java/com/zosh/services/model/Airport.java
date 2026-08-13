package com.zosh.services.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zosh.embeddable.Address;
import com.zosh.embeddable.GeoCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3, unique = true)
    private String iataCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "time_zone_id", length = 50)
    private String timeZone;

    @Embedded
    private Address address;

    @Embedded
    private GeoCode geoCode;

    @ManyToOne
    @JsonIgnore
    private City city;

    @JsonIgnore
    @Transient
    public String getDetailedName(){
        if(city!=null && city.getCountryCode()!=null){
            return name.toUpperCase() + "/" + city.getCountryCode();
        }
        return name.toUpperCase();
    }
}
