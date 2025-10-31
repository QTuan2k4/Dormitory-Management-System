package com.group7.DMS.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleConverter implements AttributeConverter<Users.Role, String> {

    @Override
    public String convertToDatabaseColumn(Users.Role attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case ADMIN -> "admin";
            case STAFF -> "staff";
            case STUDENT -> "student";
        };
    }

    @Override
    public Users.Role convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return switch (dbData.toLowerCase()) {
            case "admin" -> Users.Role.ADMIN;
            case "staff" -> Users.Role.STAFF;
            case "student" -> Users.Role.STUDENT;
            default -> null;
        };
    }
}
