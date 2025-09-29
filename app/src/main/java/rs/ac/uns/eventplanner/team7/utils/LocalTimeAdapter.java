package rs.ac.uns.eventplanner.team7.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.format(FORMATTER));
    }

    @Override
    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String str = json.getAsString();
        try {
            return LocalTime.parse(str, FORMATTER);
        } catch (DateTimeParseException e) {
            // fallback for "HH:mm" (in case some old data)
            return LocalTime.parse(str, DateTimeFormatter.ofPattern("HH:mm"));
        }
    }
}