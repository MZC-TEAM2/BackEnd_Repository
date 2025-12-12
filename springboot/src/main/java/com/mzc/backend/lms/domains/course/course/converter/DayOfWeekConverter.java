package com.mzc.backend.lms.domains.course.course.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;

@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            return null;
        }
        return dayOfWeek.getValue(); // MONDAY=1, TUESDAY=2, ..., FRIDAY=5
    }
    
    @Override
    public DayOfWeek convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return DayOfWeek.of(dbData); // 1=MONDAY, 2=TUESDAY, ..., 5=FRIDAY
    }
}
