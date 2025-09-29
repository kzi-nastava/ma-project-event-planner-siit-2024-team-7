package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.CreateBudgetRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.EventBudgetResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.CreateEventRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.CreateEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.invitation.InvitationSendingDTO;
import rs.ac.uns.eventplanner.team7.data.model.Activity;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.EventVisibility;
import rs.ac.uns.eventplanner.team7.data.services.BudgetService;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.data.services.InvitationService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;


public class CreateEventFragment extends Fragment {

    private final EventService eventService = ClientUtils.injectService(EventService.class);
    private final InvitationService invitationService = ClientUtils.injectService(InvitationService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);
    private final BudgetService budgetService = ClientUtils.injectService(BudgetService.class);

    private final CreateEventRequestDTO createDTO = new CreateEventRequestDTO();

    private final ArrayList<String> invitedPeopleEmails = new ArrayList<>();

    private LinearLayout activitiesContainer;
    private MaterialButton addActivityButton;
    private final List<ActivityFragment> activityFragments = new ArrayList<>();

    private TextInputEditText eventNameInput;
    private TextInputEditText eventDescInput;
    private TextInputEditText maxParticipantsInput;
    private TextInputEditText countryInput;
    private TextInputEditText cityInput;
    private TextInputEditText streetInput;
    private TextInputEditText houseNumberInput;
    MaterialAutoCompleteTextView visibilityDropdown;
    private MaterialTextView currentCount, maxCount;
    private LinearLayout invitePeopleLayout;
    private MaterialButton inviteButton, submitButton;
    private TextInputEditText dateInput;
    private TextInputEditText timeInput;
    private MaterialAutoCompleteTextView eventTypeDropdown;


    private String bearerToken, organizerEmail;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    public static CreateEventFragment newInstance() {
        return new CreateEventFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        eventNameInput = view.findViewById(R.id.event_name_input);
        eventDescInput = view.findViewById(R.id.event_description_input);
        countryInput = view.findViewById(R.id.event_country_input);
        cityInput = view.findViewById(R.id.event_city_input);
        streetInput = view.findViewById(R.id.event_street_input);
        houseNumberInput = view.findViewById(R.id.event_house_number_input);
        maxParticipantsInput = view.findViewById(R.id.max_participants_input);
        invitePeopleLayout = view.findViewById(R.id.invite_people_layout);
        currentCount = view.findViewById(R.id.invited_people_count);
        maxCount = view.findViewById(R.id.max_count);
        inviteButton = view.findViewById(R.id.invite_people_btn);
        visibilityDropdown = view.findViewById(R.id.event_visibility_dropdown);
        dateInput = view.findViewById(R.id.event_date_input);
        timeInput = view.findViewById(R.id.event_time_input);
        eventTypeDropdown = view.findViewById(R.id.event_event_type_dropdown);
        submitButton = view.findViewById(R.id.submit_button);

        activitiesContainer = view.findViewById(R.id.activities_container);
        addActivityButton = view.findViewById(R.id.add_activity_button);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        organizerEmail = AuthUtil.extractEmail(requireContext());
        initDatePicker();
        initTimePicker();
        initEventTypeDropdown();
        initVisibilityDropdown();

        initInviteButtonListener();

        listenForMaxParticipantsChanges();

        submitButton.setOnClickListener(this::tryToSubmit);
        addActivityButton.setOnClickListener(v -> addNewActivityFragment());

    }

    private void initVisibilityDropdown() {
        visibilityDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                EventVisibility.values()
        ));

        visibilityDropdown.setOnItemClickListener((parent, v, position, id) -> {
            EventVisibility selected = (EventVisibility) parent.getItemAtPosition(position);
            invitePeopleLayout.setVisibility(selected == EventVisibility.PRIVATE ? View.VISIBLE : View.GONE);
        });
    }

    private void initDatePicker() {
        dateInput.setOnClickListener(v -> {
            LocalDateTime minDate = LocalDateTime.now().plusDays(1);
            long minDateEpoch = DateConverter.toLong(minDate);
            long maxDateEpoch = DateConverter.toLong(minDate.plusYears(1));
            var calendarConstraints = new CalendarConstraints.Builder()
                    .setStart(minDateEpoch)
                    .setEnd(maxDateEpoch)
                    .setFirstDayOfWeek(Calendar.MONDAY)
                    .setValidator(new CalendarConstraints.DateValidator() {
                        @Override
                        public boolean isValid(long date) {
                            return minDateEpoch <= date && date <= maxDateEpoch;
                        }

                        @Override
                        public int describeContents() {return 0;}

                        @Override
                        public void writeToParcel(@NonNull Parcel dest, int flags) {}
                    })
                    .build();

            var datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.select_date)
                    .setSelection(minDateEpoch)
                    .setCalendarConstraints(calendarConstraints)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    Instant instant = Instant.ofEpochMilli(selection);
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    dateInput.setText(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
            });
            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        });
    }

    private void initTimePicker() {
        timeInput.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTitleText(R.string.select_time)
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build();

            timePicker.show(getChildFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(tp -> {
                String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                        timePicker.getHour(), timePicker.getMinute());
                timeInput.setText(formattedTime);
            });
        });
    }

    private void initEventTypeDropdown() {
        fetchEventTypes();
    }

    private void fetchEventTypes() {
        Call<List<EventType>> call = eventTypeService.findAllActive(bearerToken);
        call.enqueue(new Callback<>() {
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
                String message = t.getMessage();
                if (message != null) Log.d("ERROR", message);
            }
        });
    }

    private void listenForMaxParticipantsChanges() {
        maxParticipantsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable text) {
                String countText = "?";
                if (text != null) {
                    try {
                        int count = Integer.parseUnsignedInt(text.toString());
                        countText = String.valueOf(count);
                        inviteButton.setEnabled(count > 0);
                    } catch (NumberFormatException e) {
                        inviteButton.setEnabled(false);
                    }
                }
                maxCount.setText(countText);
            }
        });
    }

    private void initInviteButtonListener() {
        inviteButton.setOnClickListener(v -> {
            View currentFocus = requireActivity().getCurrentFocus();
            if (currentFocus != null) currentFocus.clearFocus();
            int maxPeople = Integer.parseInt(Objects.requireNonNull(maxParticipantsInput.getText()).toString());

            var dialog = InvitePeopleDialogFragment.newInstance(invitedPeopleEmails, maxPeople);
            dialog.setCancelable(false);
            dialog.show(getChildFragmentManager(), "InvitePeopleDialog");
            dialog.setOnConfirmClickListener(newEmails -> {
                invitedPeopleEmails.clear();
                invitedPeopleEmails.addAll(newEmails);
                currentCount.setText(String.format(Locale.getDefault(), "%d", invitedPeopleEmails.size()));
            });
        });
    }

    private LocalDateTime getSelectedDateTime() {
        String dateText = Objects.requireNonNull(dateInput.getText()).toString(); // e.g., "2025-08-31"
        String timeText = Objects.requireNonNull(timeInput.getText()).toString(); // e.g., "14:30"

        if (dateText.isEmpty() || timeText.isEmpty()) return null;
        // Convert to LocalDateTime
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
        LocalDate date = LocalDate.parse(dateText, dateFormatter);
        return date.atTime(LocalTime.parse(timeText));
    }

    private void tryToSubmit(View v) {
        final Location address = createDTO.getLocation();
        address.setCountry(validateRequiredField(countryInput, R.string.country_is_required));
        address.setCity(validateRequiredField(cityInput, R.string.city_is_required));
        address.setStreet(validateRequiredField(streetInput, R.string.street_is_required));
        address.setHouseNumber(validateRequiredField(houseNumberInput, R.string.house_number_is_required));
        createDTO.setName(validateRequiredField(eventNameInput, R.string.event_name_is_required));
        createDTO.setDescription(validateRequiredField(eventDescInput, R.string.event_desc_is_required));
        if (validateRequiredField(maxParticipantsInput, R.string.event_max_participants_is_required) != null) {
             createDTO.setMaxParticipants(Integer.parseUnsignedInt(maxCount.getText().toString()));
        }
//        validateRequiredField(dateInput, R.string.event_date_is_required);
//        validateRequiredField(timeInput, R.string.event_time_is_required);
        if (validateRequiredField(dateInput, R.string.event_date_is_required) != null &&
                validateRequiredField(timeInput, R.string.event_time_is_required) != null) {
            createDTO.setDate(getSelectedDateTime());
        }

        createDTO.setEventTypeName(validateRequiredField(eventTypeDropdown, requireView().findViewById(R.id.event_event_type_layout), R.string.event_event_type_is_required));
        if (validateRequiredField(visibilityDropdown,  requireView().findViewById(R.id.event_visibility_layout), R.string.event_visibility_is_required) != null) {
            createDTO.setVisibility(EventVisibility.valueOf(visibilityDropdown.getText().toString()));
        }
        if (!collectActivities()) return;
        Gson gson = new Gson();
        String json = gson.toJson(createDTO);
        Log.d("dto", json);

        if (!createDTO.areValidFields()) return;

        if (createDTO.getVisibility() == EventVisibility.PRIVATE
                && invitedPeopleEmails.size() < createDTO.getMaxParticipants()) {
            showWarningDialog();
        } else {
            createEvent();
        }
    }

    private void showWarningDialog() {
        boolean noOneInvited = invitedPeopleEmails.isEmpty();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(noOneInvited ? R.string.no_one_invited : R.string.free_slots_remain)
                .setMessage(noOneInvited ? R.string.no_one_invited_message : R.string.free_slots_remain_message)
                .setNegativeButton(R.string.no, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.yes, (dialog, w) -> {
                    dialog.dismiss();
                    createEvent();
                })
                .show();
    }

    private void createEvent() {
        submitButton.setEnabled(false);
        addActivityButton.setEnabled(false);
        inviteButton.setEnabled(false);
        eventService.createEvent(bearerToken, createDTO).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CreateEventResponseDTO> call, @NonNull Response<CreateEventResponseDTO> response) {
                if (isAdded()) {
                    submitButton.setEnabled(true);
                    addActivityButton.setEnabled(true);
                    inviteButton.setEnabled(true);
                }
                CreateEventResponseDTO createdEvent = response.body();
                if ((!response.isSuccessful() || createdEvent == null)) return;

                if (createdEvent.getVisibility() == EventVisibility.PRIVATE) {
                    sendInvitations(createdEvent.getId());
                }
                createBudget(createdEvent.getId());
            }

            @Override
            public void onFailure(@NonNull Call<CreateEventResponseDTO> call, @NonNull Throwable t) {
                String message = t.getMessage();
                if (message != null) Log.d("ERROR", message);
                if (isAdded()) {
                    submitButton.setEnabled(true);
                    addActivityButton.setEnabled(true);
                    inviteButton.setEnabled(true);
                }
            }
        });
    }

    private void sendInvitations(Integer eventId) {
        InvitationSendingDTO dto = new InvitationSendingDTO(organizerEmail, invitedPeopleEmails, eventId);
        invitationService.sendInvitations(bearerToken, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<ResponseMessageDTO> call,
                    @NonNull Response<ResponseMessageDTO> response
            ) {
                if (!response.isSuccessful()) {
                    Log.d("ERROR", "Error occurred while sending invitations");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseMessageDTO> call, @NonNull Throwable t) {
                String message = t.getMessage();
                if (message != null) Log.d("ERROR", message);
            }
        });
    }

    private void createBudget(Integer eventId) {
        CreateBudgetRequestDTO dto = new CreateBudgetRequestDTO(eventId, new HashMap<>());

        budgetService.createBudget(bearerToken, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<EventBudgetResponseDTO> call,
                    @NonNull Response<EventBudgetResponseDTO> response
            ) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), R.string.budget_creation_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(requireContext(), R.string.event_created_successfully, Toast.LENGTH_SHORT).show();
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.create_budget_title)
                        .setMessage(R.string.create_budget_message)
                        .setPositiveButton(R.string.yes, (d, w) -> {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("eventBudget", response.body());
                            Navigation.findNavController(requireView()).navigate(R.id.navigate_to_budget_creation, bundle);
                        })
                        .setNegativeButton(R.string.no, (d, w) ->
                                Navigation.findNavController(requireView()).navigateUp()
                        )
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<EventBudgetResponseDTO> call, @NonNull Throwable t) {
                String message = t.getMessage();
                if (message != null) Log.d("ERROR", message);
            }
        });
    }

    private void addNewActivityFragment() {

        int index = activityFragments.size() + 1;

        ActivityFragment fragment = new ActivityFragment();

        fragment.setOnDeleteListener(this::removeActivityFragment);


        // Optional: generate a unique tag for each fragment
        String tag = "activity_" + activityFragments.size();

        Bundle args = new Bundle();
        args.putInt("index", index);
        fragment.setArguments(args);

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.activities_container, fragment, tag)
                .commit();

        activityFragments.add(fragment);
    }

    private boolean collectActivities() {
        boolean canContinue = true; // boolean needed so it goes thru every activity
        createDTO.setActivities(new ArrayList<>());

        for (ActivityFragment fragment : activityFragments) {
            Activity dto = fragment.extractActivity();
            if (dto != null) {
                createDTO.getActivities().add(dto);
            } else {
                Toast.makeText(requireContext(), R.string.invalid_activity, Toast.LENGTH_SHORT).show();
                canContinue = false;
            }
        }
        return canContinue;
    }

    private void removeActivityFragment(ActivityFragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commit();

        activityFragments.remove(fragment);

        // 🔹 Optional: Renumber titles after deletion
        renumberActivities();
    }

    private void renumberActivities() {
        for (int i = 0; i < activityFragments.size(); i++) {
            ActivityFragment f = activityFragments.get(i);
            f.updateIndex(i + 1); // call the public method
        }
    }


    private String validateRequiredField(TextInputEditText input, @StringRes int errorMessage) {
        String text = Objects.requireNonNull(input.getText()).toString().trim();

        if (text.isEmpty()) {
            input.setError(getString(errorMessage));
            return null;
        }
        return text;
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

}