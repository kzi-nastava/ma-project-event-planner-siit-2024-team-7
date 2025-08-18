package rs.ac.uns.eventplanner.team7.ui.fragments.reservation;

import static rs.ac.uns.eventplanner.team7.utils.ClientUtils.injectService;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.event.FutureReservableEventsDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reservation.ReservationDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.ReservationService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ReservationStepsAdapter;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class ServiceReservationFragment extends Fragment {

    private final ReservationService reservationService = injectService(ReservationService.class);
    private final UserService userService = injectService(UserService.class);
    private ViewPager2 stepPager;
    private ReservationStepsAdapter stepsAdapter;
    private MaterialTextView errorView, selectedEventOverview, selectedDateOverview,
            selectedTimeslotOverview, selectedTimesOverview;
    private MaterialButton backButton, nextButton;

    private ReservationViewModel viewModel;
    private String bearerToken;

    private static final String ARG_SERVICE = "service";
    private static final String ARG_ORGANIZER_EVENTS = "organizerEvents";

    public ServiceReservationFragment() {
        // Required empty public constructor
    }

    public static ServiceReservationFragment newInstance(
            GetServiceResponseDTO service,
            FutureReservableEventsDTO organizerEvents
    ) {
        ServiceReservationFragment fragment = new ServiceReservationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SERVICE, service);
        args.putParcelable(ARG_ORGANIZER_EVENTS, organizerEvents);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            var service = getArguments().getParcelable(ARG_SERVICE, GetServiceResponseDTO.class);
            var events = getArguments().getParcelable(ARG_ORGANIZER_EVENTS, FutureReservableEventsDTO.class);
            viewModel = new ViewModelProvider(this).get(ReservationViewModel.class);
            viewModel.init(Objects.requireNonNull(service), Objects.requireNonNull(events));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_reservation, container, false);
        stepsAdapter = new ReservationStepsAdapter(this);
        stepPager = view.findViewById(R.id.step_pager);
        stepPager.setUserInputEnabled(false);
        stepPager.setAdapter(stepsAdapter);
        selectedEventOverview = view.findViewById(R.id.selected_event_overview);
        selectedDateOverview = view.findViewById(R.id.selected_date_overview);
        selectedTimeslotOverview = view.findViewById(R.id.selected_timeslot_overview);
        selectedTimesOverview = view.findViewById(R.id.selected_times_overview);
        errorView = view.findViewById(R.id.error);
        nextButton = view.findViewById(R.id.next_step_button);
        backButton = view.findViewById(R.id.back_step_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialTextView title = view.findViewById(R.id.reservation_title);
        title.setText(getString(R.string.service_reservation, viewModel.getService().getName()));
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        viewModel.setOrganizerEmail(AuthUtil.extractEmail(requireContext()));
        getServiceProvider();

        viewModel.getCurrentStepValid()
                .observe(getViewLifecycleOwner(), isValid -> nextButton.setEnabled(isValid));

        stepPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                viewModel.setCurrentStep(pos);
            }
        });

        backButton.setOnClickListener(v -> {
            if (stepPager.getCurrentItem() > 0) {
                stepPager.setCurrentItem(stepPager.getCurrentItem() - 1, true);
                refreshButtons();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (stepPager.getCurrentItem() < stepsAdapter.getItemCount() - 1) {
                stepPager.setCurrentItem(stepPager.getCurrentItem() + 1, true);
                refreshButtons();
            } else {
                tryToReserve();
            }
        });

        viewModel.getResponseError().observe(getViewLifecycleOwner(), error ->
                errorView.setText(error));

        observeFormChanges();
    }

    private void observeFormChanges() {
        viewModel.getSelectedEvent().observe(getViewLifecycleOwner(), event ->
                selectedEventOverview.setText(getString(R.string.selected_event_overview, event.getName())));

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            Instant instant = Instant.ofEpochMilli(date);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            String pattern;
            if (localDateTime.getYear() == LocalDateTime.now().getYear()) {
                pattern = "EEEE, MMMM dd";
            } else {
                pattern = "EEEE, MMMM dd, yyyy";
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
            selectedDateOverview.setText(getString(R.string.selected_date_overview, localDateTime.format(formatter)));
        });

        viewModel.getSelectedAvailableTimeslot().observe(getViewLifecycleOwner(), timeSlot -> {
            String text = getString(R.string.selected_available_timeslot_overview, timeSlot);
            selectedTimeslotOverview.setText(timeSlot != null ? text : "");
        });

        viewModel.getSelectedTimeslot().observe(getViewLifecycleOwner(), timeSlot -> {
            String text = getString(R.string.selected_timeslot_overview, timeSlot);
            selectedTimesOverview.setText(timeSlot != null ? text : "");
        });

    }


    private void refreshButtons() {
        nextButton.setText(stepPager.getCurrentItem() < 3 ? R.string.next : R.string.confirm);
        backButton.setEnabled(stepPager.getCurrentItem() > 0);
    }

    private void getServiceProvider() {
        Integer serviceId = viewModel.getService().getId();
        userService.getProviderByItemId(bearerToken, serviceId).enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<GetProviderResponseDTO> call,
                    @NonNull Response<GetProviderResponseDTO> response
            ) {
                if (response.isSuccessful()) {
                    GetProviderResponseDTO provider = response.body();
                    if (provider == null) return;
                    viewModel.setProviderEmail(provider.getEmail());
                } else {
                    viewModel.setResponseError(response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetProviderResponseDTO> call, @NonNull Throwable t) {
                viewModel.setResponseError(t.getMessage());
            }
        });
    }

    private void tryToReserve() {
        if (!viewModel.isValid()) return;
        nextButton.setEnabled(false);
        reservationService.create(bearerToken, viewModel.toDto()).enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<ReservationDTO> call,
                    @NonNull Response<ReservationDTO> response
            ) {
                if (response.isSuccessful()) {
                    if (getView() != null) {
                        Bundle args = new Bundle();
                        args.putBoolean("reservation", true);
                        args.putParcelable("serviceDTO", viewModel.getService()); // causes exception if not put back into the bundle
                        Navigation.findNavController(getView()).navigate(R.id.navigate_back_from_service_reservation, args);
                    }
                    return;
                }
                nextButton.setEnabled(true);
                viewModel.setResponseError(response.errorBody());
            }

            @Override
            public void onFailure(@NonNull Call<ReservationDTO> call, @NonNull Throwable t) {
                nextButton.setEnabled(true);
                viewModel.setResponseError(t.getMessage());
            }
        });
    }

}