package rs.ac.uns.eventplanner.team7.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public final class DateConverter {

    public static long toLong(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public static long toLong(LocalDate dateTime) {
        return dateTime.toEpochDay();
    }

    public static long toLong(LocalTime time) {
        return time.atDate(LocalDate.ofEpochDay(0)) // anchor to 1970-01-01
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    public static LocalTime toLocalTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
    }

    public static LocalDate toLocalDate(long epochDays) {
        return LocalDate.ofEpochDay(epochDays);
    }

}
