package com.zosh.services;

import com.zosh.domain.AncillaryMetaData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter
public class AncillaryMetadataConverter implements AttributeConverter<AncillaryMetaData, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AncillaryMetaData ancillaryMetadata) {
        try {
            return ancillaryMetadata == null ? null : objectMapper.writeValueAsString(ancillaryMetadata);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting AncillaryMetadata to JSON", e);
        }
    }

    @Override
    public AncillaryMetaData convertToEntityAttribute(String s) {
        try {
            return s == null ? null : objectMapper.readValue(s, AncillaryMetaData.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to AncillaryMetadata", e);
        }
    }
}
