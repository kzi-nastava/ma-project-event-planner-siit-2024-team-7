package rs.ac.uns.eventplanner.team7.data.dto;

import androidx.annotation.NonNull;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMessageDTO extends ResponseMessageDTO {

    private Instant timestamp; // UTC time

    public ErrorMessageDTO(String message, Instant timestamp) {
        super(message);
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Error: %s, %s", message, timestamp);
    }
}