package com.blog.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class WebsiteStatusConverter implements AttributeConverter<Website.WebsiteStatus, String> {

    @Override
    public String convertToDatabaseColumn(Website.WebsiteStatus status) {
        return status != null ? status.getValue() : null;
    }

    @Override
    public Website.WebsiteStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? Website.WebsiteStatus.fromValue(dbData) : null;
    }
}
