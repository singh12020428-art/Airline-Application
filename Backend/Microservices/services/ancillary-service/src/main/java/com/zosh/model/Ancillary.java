package com.zosh.model;

import com.zosh.domain.AncillaryMetaData;
import com.zosh.enums.AncillaryType;
import com.zosh.services.AncillaryMetadataConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Ancillary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AncillaryType type;

    private String subType;

    private String rfisc;

    @Column(nullable = false)
    private String name;

    private String description;

    @Convert(converter = AncillaryMetadataConverter.class)
    private AncillaryMetaData metadata;

    @OneToMany(mappedBy = "ancillary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InsuranceCoverage> coverages = new ArrayList<>();

    private Integer displayOrder;

    private String iconUrl;

    private Long airlineId;
}
