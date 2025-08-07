package rs.ac.uns.eventplanner.team7.ui.fragments.reservation;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import okhttp3.ResponseBody;
import rs.ac.uns.eventplanner.team7.data.dto.ErrorMessageDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.FutureReservableEventsDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reservation.CreateReservationRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.data.model.TimeSlot;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;

@Getter
public class ReservationViewModel extends ViewModel {

    private final String TAG = "ResVM";
    
    private GetServiceResponseDTO service;

    private FutureReservableEventsDTO organizerEvents;

    private final MutableLiveData<String> organizerEmail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> organizerEmailValid = new MutableLiveData<>(false);

    private final MutableLiveData<String> providerEmail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> providerEmailValid = new MutableLiveData<>(false);

    private final MutableLiveData<GetEventResponseDTO> selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> eventValid = new MutableLiveData<>(false);

    private Set<DayOfWeek> availableWeekdays = Set.of();
    private long minDate;
    @Setter
    private long maxDate;

    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dateValid = new MutableLiveData<>(false);

    private final MutableLiveData<TimeSlot> selectedAvailableTimeslot = new MutableLiveData<>();
    private final MutableLiveData<Boolean> availableTimeslotValid = new MutableLiveData<>(false);

    private final MutableLiveData<TimeSlot> selectedTimeslot = new MutableLiveData<>();
    private final MutableLiveData<Boolean> selectedTimeslotValid = new MutableLiveData<>(false);
    private final MutableLiveData<Pair<Boolean, Boolean>> selectedTimeslotValidSpecific =
            new MutableLiveData<>(Pair.create(false, false));

    private final MutableLiveData<String> responseError = new MutableLiveData<>();

    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(0);

    public void init(GetServiceResponseDTO service, FutureReservableEventsDTO organizerEvents) {
        if (this.service == null && this.organizerEvents == null) {
            this.service = service;
            this.organizerEvents = organizerEvents;
            availableWeekdays = service.getWorkDaysDTOs()
                    .stream()
                    .map(WorkDayDTO::getDay)
                    .collect(Collectors.toSet());
            LocalDateTime today = LocalDateTime.now();
            minDate = DateConverter.toLong(today.plusDays(service.getReservationDeadlineInDays()));
        }
    }

    public LiveData<Boolean> getCurrentStepValid() {
        return Transformations.switchMap(currentStep, idx -> {
            switch (idx) {
                case 0: return eventValid;
                case 1: return dateValid;
                case 2: return availableTimeslotValid;
                case 3: return selectedTimeslotValid;
                default: return new MutableLiveData<>(false);
            }
        });
    }

    public void setCurrentStep(int idx) {
        currentStep.setValue(idx);
    }

    public CreateReservationRequestDTO toDto() {
        // Don't call before isValid, or NullPointerException will be thrown
        return new CreateReservationRequestDTO(
                Objects.requireNonNull(organizerEmail.getValue()),
                Objects.requireNonNull(providerEmail.getValue()),
                Objects.requireNonNull(selectedEvent.getValue()).getId(),
                Objects.requireNonNull(service.getId()),
                Objects.requireNonNull(selectedTimeslot.getValue())
        );
    }

    public boolean isValid() {
        try {
            return Objects.requireNonNull(organizerEmailValid.getValue())
                    && Objects.requireNonNull(providerEmailValid.getValue())
                    && Objects.requireNonNull(eventValid.getValue())
                    && service != null && service.getId() != null
                    && Objects.requireNonNull(selectedTimeslotValid.getValue());
        } catch (NullPointerException e) {
            // This can only occur if calling isValid before the final step
            return false;
        }
    }

    public boolean isServiceAvailable(long utcDate) {
        boolean isWithinLimits = minDate < utcDate && utcDate < maxDate;
        Instant instant = Instant.ofEpochMilli(utcDate);
        ZonedDateTime zoned = instant.atZone(ZoneOffset.UTC);
        return availableWeekdays.contains(zoned.getDayOfWeek()) && isWithinLimits;
    }

    private void validateSelectedTimes(TimeSlot available, TimeSlot selected) {
        boolean isWithinAvailable = selected.isWithin(available);

        long duration = selected.getSlotDuration().toMinutes();
        boolean validDur = duration >= service.getMinDurationInMinutes()
                && duration <= service.getMaxDurationInMinutes();

        selectedTimeslotValidSpecific.setValue(Pair.create(isWithinAvailable, validDur));
        selectedTimeslotValid.setValue(isWithinAvailable && validDur);
    }

    public void setOrganizerEmail(String email) {
        organizerEmail.setValue(email);
        organizerEmailValid.setValue(email != null);
    }

    public void setProviderEmail(String email) {
        providerEmail.setValue(email);
        providerEmailValid.setValue(email != null);
    }

    public void setSelectedEvent(GetEventResponseDTO selectedEvent) {
        Log.d(TAG, "setSelectedEvent: eventName: " + selectedEvent);
        this.selectedEvent.setValue(selectedEvent);
        eventValid.setValue(selectedEvent != null);
    }

    public void setSelectedDate(long date) {
        Log.d(TAG, "setSelectedDate: date: " + date);
        selectedDate.setValue(date);
        dateValid.setValue(isServiceAvailable(date));
        setSelectedAvailableTimeslot(null);
    }

    public void setSelectedAvailableTimeslot(TimeSlot slot) {
        Log.d(TAG, "setSelectedAvailableTimeslot: slot: " + slot);
        selectedAvailableTimeslot.setValue(slot);
        availableTimeslotValid.setValue(slot != null);
        selectedTimeslot.setValue(null);
        selectedTimeslotValidSpecific.setValue(Pair.create(false, false));
        selectedTimeslotValid.setValue(false);
    }

    public void setStartTime(int hour, int minute) {
        Log.d(TAG, String.format("setStartTime: hour: %d, minute: %s", hour, minute));
        TimeSlot available = selectedAvailableTimeslot.getValue();
        if (available == null) return;
        LocalDateTime start = LocalDateTime.of(available.getDate(), LocalTime.of(hour, minute));
        LocalDateTime end = start.plusMinutes(service.getMinDurationInMinutes());
        TimeSlot selected = new TimeSlot(start, end);
        selectedTimeslot.setValue(selected);
        validateSelectedTimes(available, selected);
    }

    public void setEndTime(int hour, int minute) {
        Log.d(TAG, String.format("setEndTime: hour: %d, minute: %s", hour, minute));
        TimeSlot available = selectedAvailableTimeslot.getValue();
        if (available == null) return;
        LocalDateTime end = LocalDateTime.of(available.getDate(), LocalTime.of(hour, minute));
        LocalDateTime start;
        if (selectedTimeslot.getValue() != null) {
            start = selectedTimeslot.getValue().getStartTime();
        } else {
            start = end.minusMinutes(service.getMinDurationInMinutes());
        }
        TimeSlot selected = new TimeSlot(start, end);
        selectedTimeslot.setValue(selected);
        validateSelectedTimes(available, selected);
    }

    public void setResponseError(ResponseBody response) {
        ErrorMessageDTO errorMessage = ClientUtils.convertToErrorMessage(response);
        if (errorMessage != null) {
            responseError.setValue(errorMessage.getMessage());
            Log.d(TAG, "setResponseError: error message: " + errorMessage);
        }
    }

    public void setResponseError(String error) {
        responseError.setValue(error);
    }
}