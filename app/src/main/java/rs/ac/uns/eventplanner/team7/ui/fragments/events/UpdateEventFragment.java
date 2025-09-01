package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.event.CreateEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.UpdateEventRequestDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.data.model.Activity;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.enums.EventVisibility;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class UpdateEventFragment extends Fragment {

    private final EventService eventService = ClientUtils.injectService(EventService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);

    private GetEventResponseDTO eventDTO;
    private UpdateEventRequestDTO updateDTO = new UpdateEventRequestDTO();

    private TextInputEditText eventNameInput, eventDescInput, maxParticipantsInput;
    private TextInputEditText countryInput, cityInput, streetInput, houseNumberInput;
    private MaterialAutoCompleteTextView visibilityDropdown, eventTypeDropdown;
    private TextInputEditText dateInput;
    private TextInputEditText timeInput;
    private LinearLayout activitiesContainer;
    private MaterialButton addActivityButton;
    private MaterialButton updateButton;
    private MaterialButton deleteButton;
    private List<ActivityFragment> activityFragments = new ArrayList<>();

    private String bearerToken;

    public UpdateEventFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventDTO = getArguments().getParcelable("eventDTO", GetEventResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_event, container, false);

        eventNameInput = view.findViewById(R.id.event_name_input);
        eventDescInput = view.findViewById(R.id.event_description_input);
        maxParticipantsInput = view.findViewById(R.id.max_participants_input);
        countryInput = view.findViewById(R.id.event_country_input);
        cityInput = view.findViewById(R.id.event_city_input);
        streetInput = view.findViewById(R.id.event_street_input);
        houseNumberInput = view.findViewById(R.id.event_house_number_input);

        visibilityDropdown = view.findViewById(R.id.event_visibility_dropdown);
        eventTypeDropdown = view.findViewById(R.id.event_event_type_dropdown);

        dateInput = view.findViewById(R.id.event_date_input);
        timeInput = view.findViewById(R.id.event_time_input);

        activitiesContainer = view.findViewById(R.id.activities_container);
        addActivityButton = view.findViewById(R.id.add_activity_button);
        updateButton = view.findViewById(R.id.update_submit_button);
        deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        MaterialButton updateBudgetButton = view.findViewById(R.id.update_budget_btn);
        updateBudgetButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("eventBudget", eventDTO.getBudget());
            View view1 = getView();
            if (view1 == null) return;
            Navigation.findNavController(view1).navigate(R.id.navigate_to_budget_management, bundle);
        });

        checkFieldEnabling(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());

        initVisibilityDropdown();
        fetchEventTypes();
        listenForMaxParticipantsChanges();
        prefillFields();
        prefillActivities();

        addActivityButton.setOnClickListener(v -> addNewActivityFragment());

        updateButton.setOnClickListener(v -> tryToSubmit());
    }

    private void checkFieldEnabling(View view) {
        if (eventDTO.getDate().isBefore(LocalDateTime.now())) {
            view.findViewById(R.id.cant_be_updated).setVisibility(View.VISIBLE);
            eventDescInput.setEnabled(false);
            maxParticipantsInput.setEnabled(false);
            countryInput.setEnabled(false);
            cityInput.setEnabled(false);
            streetInput.setEnabled(false);
            houseNumberInput.setEnabled(false);
            visibilityDropdown.setEnabled(false);
            eventTypeDropdown.setEnabled(false);
            dateInput.setEnabled(false);
            timeInput.setEnabled(false);
            updateButton.setEnabled(false);
            addActivityButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void prefillFields() {
        if (eventDTO == null) return;

        eventNameInput.setText(eventDTO.getName());
        eventDescInput.setText(eventDTO.getDescription());
        maxParticipantsInput.setText(String.valueOf(eventDTO.getMaxParticipants()));

        countryInput.setText(eventDTO.getLocation().getCountry());
        cityInput.setText(eventDTO.getLocation().getCity());
        streetInput.setText(eventDTO.getLocation().getStreet());
        houseNumberInput.setText(eventDTO.getLocation().getHouseNumber());

        LocalDateTime eventDateTime = eventDTO.getDate();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());
        dateInput.setText(eventDateTime.toLocalDate().format(dateFormatter));

        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                eventDateTime.getHour(), eventDateTime.getMinute());
        timeInput.setText(formattedTime);

        visibilityDropdown.setText(eventDTO.getVisibility().toString(), false);
        eventTypeDropdown.setText(eventDTO.getEventType().getName(), false);
    }

    private void prefillActivities() {
        if (eventDTO.getActivities() == null) return;
        for (Activity act : eventDTO.getActivities()) {
            ActivityFragment fragment = new ActivityFragment();
            fragment.setOnDeleteListener(this::removeActivityFragment);

            Bundle args = new Bundle();
            args.putInt("index", activityFragments.size() + 1);
            args.putParcelable("activity", act);
            fragment.setArguments(args);

            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.activities_container, fragment, "activity_" + activityFragments.size())
                    .commit();

            activityFragments.add(fragment);
        }
    }

    private void initVisibilityDropdown() {
        visibilityDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                EventVisibility.values()));
    }

    private void fetchEventTypes() {
        eventTypeService.findAllActive(bearerToken).enqueue(
                new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<EventType>> call,
                                   @NonNull Response<List<EventType>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<EventType> types = response.body();

                    // Extract names for the dropdown
                    List<String> typeNames = new ArrayList<>();
                    for (EventType type : types) {
                        typeNames.add(type.getName());
                    }

                    // Set adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            typeNames
                    );
                    eventTypeDropdown.setAdapter(adapter);

                    eventTypeDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        String selected = adapter.getItem(position);
                    });

                } else {
                    Toast.makeText(requireContext(), "Failed to load event types" + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EventType>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForMaxParticipantsChanges() {
        maxParticipantsInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable text) {
                try {
                    int count = Integer.parseUnsignedInt(text.toString());
                    updateButton.setEnabled(count > 0);
                } catch (NumberFormatException e) {
                    updateButton.setEnabled(false);
                }
            }
        });
    }

    private void tryToSubmit() {

        updateDTO.setDescription(validateRequiredField(eventDescInput, R.string.event_desc_is_required));
        updateDTO.setMaxParticipants(Integer.parseUnsignedInt(maxParticipantsInput.getText().toString()));

        updateDTO.setLocation(eventDTO.getLocation());
        updateDTO.getLocation().setCountry(validateRequiredField(countryInput, R.string.country_is_required));
        updateDTO.getLocation().setCity(validateRequiredField(cityInput, R.string.city_is_required));
        updateDTO.getLocation().setStreet(validateRequiredField(streetInput, R.string.street_is_required));
        updateDTO.getLocation().setHouseNumber(validateRequiredField(houseNumberInput, R.string.house_number_is_required));

        if (validateRequiredField(dateInput, R.string.event_date_is_required) != null &&
                validateRequiredField(timeInput, R.string.event_time_is_required) != null) {
            updateDTO.setDate(getSelectedDateTime());
        }

        updateDTO.setEventTypeName(validateRequiredField(eventTypeDropdown, requireView().findViewById(R.id.event_event_type_layout), R.string.event_event_type_is_required));
        if (validateRequiredField(visibilityDropdown,  requireView().findViewById(R.id.event_visibility_layout), R.string.event_visibility_is_required) != null) {
            updateDTO.setVisibility(EventVisibility.valueOf(visibilityDropdown.getText().toString()));
        }
        if (!collectActivities()) return;

        updateEvent();
    }

    private boolean collectActivities() {
        boolean valid = true;
        updateDTO.setActivities(new ArrayList<>());
        for (ActivityFragment frag : activityFragments) {
            Activity act = frag.extractActivity();
            if (act != null) updateDTO.getActivities().add(act);
            else valid = false;
        }
        if (!valid) Toast.makeText(requireContext(), "Please fill all activity fields correctly", Toast.LENGTH_SHORT).show();
        return valid;
    }

    private void updateEvent() {
        Call<CreateEventResponseDTO> call = eventService.updateEvent(
                bearerToken, eventDTO.getId(), updateDTO);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CreateEventResponseDTO> call, @NonNull Response<CreateEventResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(requireContext(), "Update failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateEventResponseDTO> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewActivityFragment() {
        ActivityFragment fragment = new ActivityFragment();
        fragment.setOnDeleteListener(this::removeActivityFragment);

        Bundle args = new Bundle();
        args.putInt("index", activityFragments.size() + 1);
        fragment.setArguments(args);

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.activities_container, fragment, "activity_" + activityFragments.size())
                .commit();

        activityFragments.add(fragment);
    }

    private void removeActivityFragment(ActivityFragment fragment) {
        getChildFragmentManager().beginTransaction().remove(fragment).commit();
        activityFragments.remove(fragment);
        renumberActivities();
    }

    private void renumberActivities() {
        for (int i = 0; i < activityFragments.size(); i++) {
            activityFragments.get(i).updateIndex(i + 1);
        }
    }

    private String validateRequiredField(TextInputEditText input, int errorRes) {
        String text = Objects.requireNonNull(input.getText()).toString().trim();
        if (text.isEmpty()) {
            input.setError(getString(errorRes));
            return null;
        }
        return text;
    }

    private void deleteEvent() {
        eventService.deleteEvent(bearerToken, eventDTO.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Event deletion successful", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String validateRequiredField(
            @NonNull MaterialAutoCompleteTextView input,
            @NonNull TextInputLayout layout,
            @StringRes int errorMessage) {

        String text = Objects.requireNonNull(input.getText()).toString().trim();

        if (text.isEmpty()) {
            layout.setError(getString(errorMessage));
            return null;
        } else {
            layout.setError(null); // clear previous error
            return text;
        }
    }

    private LocalDateTime getSelectedDateTime() {
        String dateText = Objects.requireNonNull(dateInput.getText()).toString(); // e.g., "Aug 18, 2025"
        String timeText = Objects.requireNonNull(timeInput.getText()).toString(); // e.g., "14:30"

        if (dateText.isEmpty() || timeText.isEmpty()) return null;
        // Convert to LocalDateTime
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault());
        LocalDate date = LocalDate.parse(dateText, dateFormatter);

        String[] parts = timeText.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        return date.atTime(hour, minute);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (d, which) ->
                        deleteEvent())
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (deleteButton != null) {
                deleteButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
        });
        dialog.show();
    }
}
