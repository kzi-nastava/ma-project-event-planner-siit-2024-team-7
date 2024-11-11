package rs.ac.uns.eventplanner.team7.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class DateConverter {

    public static long toLong(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public static long toLong(LocalDate dateTime) {
        return dateTime.toEpochDay();
    }

    public static LocalDateTime toLocalDateTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    public static LocalDate toLocalDate(long epochDays) {
        return LocalDate.ofEpochDay(epochDays);
    }

}
