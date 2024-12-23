package rs.ac.uns.eventplanner.team7.fragments;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.activities.LoginActivity;
import rs.ac.uns.eventplanner.team7.adapters.CalendarAdapter;
import rs.ac.uns.eventplanner.team7.adapters.CarouselAdapter;
import rs.ac.uns.eventplanner.team7.dto.BusynessDTO;
import rs.ac.uns.eventplanner.team7.dto.ErrorMessageDTO;
import rs.ac.uns.eventplanner.team7.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.dto.user.GetOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateOrganizerRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateProviderRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Location;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.CurrentDayDecorator;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class UserProfileFragment extends Fragment {

    private UserRole role;
    private UserService userService;
    private MaterialCalendarView calendarView;
    private List<BusynessDTO> futureBusyness;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        userService = ClientUtils.retrofit.create(UserService.class);

        calendarView = view.findViewById(R.id.calendarView);

        setupCalendar(view);
        setupRoleText(view);
        setupInputIcons(view);
        fillFields(view);

        MaterialButton changePass = view.findViewById(R.id.change_password);
        changePass.setOnClickListener(v -> showChangePasswordDialog());
        MaterialButton deactivate = view.findViewById(R.id.deactivate_account);
        deactivate.setOnClickListener(v -> showConfirmDeactivationDialog());

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String roleString = JwtUtil.getRole(context);
        this.role = UserRole.valueOf(roleString);
    }

    private void setupCalendar(View view) {
        calendarView.setSelectedDate(LocalDate.now());
        Integer userId = JwtUtil.extractId(requireContext());
        Call<List<BusynessDTO>> call = userService.getBusyness(JwtUtil.getAuthorizationValue(requireContext()), userId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<BusynessDTO>> call, @NonNull Response<List<BusynessDTO>> response) {
                if (response.isSuccessful()) {
                    List<BusynessDTO> dtos = response.body();
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    futureBusyness = dtos;
                    for (var dto : dtos) { // add dots to the calendar
                        LocalDate date = LocalDate.parse(dto.getDate(), formatter);
                        calendarView.addDecorator(new CurrentDayDecorator(date, R.color.red_delete));
                    }
                }

                calendarView.setOnDateChangedListener((widget, date, selected) -> {
                    LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
                    List<BusynessDTO> filteredEvents = filterEvents(selectedDate);

                    RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); // for vertical layout
                    if (recyclerView.getAdapter() == null) {
                        CalendarAdapter adapter = new CalendarAdapter(requireContext(), filteredEvents);
                        recyclerView.setAdapter(adapter);
                    } else {
                        CalendarAdapter adapter = (CalendarAdapter) recyclerView.getAdapter();
                        adapter.updateEvents(filteredEvents);
                    }

                    setFutureActivitiesVisibility(view, filteredEvents);
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<BusynessDTO>> call, @NonNull Throwable t) {
            }
        });
    }

    private void setFutureActivitiesVisibility(View view, List<BusynessDTO> filteredEvents) {
        TextView futureActivities = view.findViewById(R.id.future_activities);
        if (filteredEvents.isEmpty()) {
            futureActivities.setVisibility(View.GONE);
        } else {
            futureActivities.setVisibility(View.VISIBLE);
        }
    }

    private List<BusynessDTO> filterEvents(LocalDate selectedDate) {
        List<BusynessDTO> filteredEvents = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (BusynessDTO dto : futureBusyness) {
            LocalDate eventDate = LocalDate.parse(dto.getDate(), formatter);
            if (eventDate.equals(selectedDate)) {
                filteredEvents.add(dto);
            }
        }
        return filteredEvents;
    }

    private void setupRoleText(View view) {
        MaterialTextView roleTextView = view.findViewById(R.id.user_role);
        int visibility;

        switch (role) {
            case EVENT_ORG:
                roleTextView.setText(R.string.you_eo);
                visibility = View.VISIBLE;
                view.findViewById(R.id.eo_update_inputs).setVisibility(visibility);
                break;
            case SPP:
                roleTextView.setText(R.string.you_spp);
                visibility = View.VISIBLE;
                view.findViewById(R.id.spp_update_inputs).setVisibility(visibility);
                break;
            case AUTH:
                roleTextView.setText(R.string.you_au);
                break;
            case ADMIN:
                roleTextView.setText(R.string.you_admin);
                break;
        }
    }

    private void setupInputIcons(View view) {
        List<Pair<TextInputLayout, TextInputEditText>> fields = getFields(view);
        int editIconRes = R.drawable.ic_edit;
        int checkIconRes = R.drawable.ic_check;

        for (Pair<TextInputLayout, TextInputEditText> field : fields) {
            TextInputLayout layout = field.first;
            TextInputEditText input = field.second;

            layout.setTag(editIconRes);
            layout.setEndIconOnClickListener(v -> toggleFieldEditMode(layout, input, editIconRes, checkIconRes));
        }
    }

    private List<Pair<TextInputLayout, TextInputEditText>> getFields(View view) {
        List<Pair<TextInputLayout, TextInputEditText>> fields = new ArrayList<>();

        fields.add(new Pair<>(view.findViewById(R.id.change_phone_layout), view.findViewById(R.id.change_phone)));
        fields.add(new Pair<>(view.findViewById(R.id.change_country_layout), view.findViewById(R.id.change_country)));
        fields.add(new Pair<>(view.findViewById(R.id.change_city_layout), view.findViewById(R.id.change_city)));
        fields.add(new Pair<>(view.findViewById(R.id.change_street_layout), view.findViewById(R.id.change_street)));
        fields.add(new Pair<>(view.findViewById(R.id.change_house_number_layout), view.findViewById(R.id.change_house_number)));
        fields.add(new Pair<>(view.findViewById(R.id.change_profile_pic_layout), view.findViewById(R.id.change_profile_pic)));

        if (role == UserRole.EVENT_ORG) {
            fields.add(new Pair<>(view.findViewById(R.id.change_first_name_layout), view.findViewById(R.id.change_first_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_last_name_layout), view.findViewById(R.id.change_last_name)));
        } else if (role == UserRole.SPP) {
            fields.add(new Pair<>(view.findViewById(R.id.change_org_name_layout), view.findViewById(R.id.change_org_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_org_desc_layout), view.findViewById(R.id.change_org_desc)));
        }

        return fields;
    }

    private void toggleFieldEditMode(TextInputLayout layout, TextInputEditText input, int editIconRes, int checkIconRes) {
        if ((int) layout.getTag() == editIconRes) {
            layout.setEndIconDrawable(checkIconRes);
            input.setEnabled(true);
            layout.setTag(checkIconRes);
        } else {
            layout.setEndIconDrawable(editIconRes);
            input.setEnabled(false);
            layout.setTag(editIconRes);

            String fieldValue = Objects.requireNonNull(input.getText()).toString().trim();
            updateField(layout.getId(), fieldValue);
        }

    }

    private void updateField(int fieldId, String value) {
        Integer userId = JwtUtil.extractId(requireContext());
        String authHeader = JwtUtil.getAuthorizationValue(requireContext());
        TextInputLayout field = requireView().findViewById(fieldId);
        if (value.isEmpty()) {
            field.setError("Value can't be empty!");
        } else {
            field.setError(null);
        }

        if (role == UserRole.EVENT_ORG) {
            UpdateOrganizerRequestDTO dto = new UpdateOrganizerRequestDTO();
            Location location = getCurrentLocationData();

            if (fieldId == R.id.change_country_layout) {
                location.setCountry(value);
            } else if (fieldId == R.id.change_city_layout) {
                location.setCity(value);
            } else if (fieldId == R.id.change_street_layout) {
                location.setStreet(value);
            } else if (fieldId == R.id.change_house_number_layout) {
                location.setHouseNumber(value);
            }
            dto.setLocation(location);

            if (fieldId == R.id.change_phone_layout) {
                dto.setPhone(value);
            } else if (fieldId == R.id.change_first_name_layout) {
                dto.setFirstName(value);
            } else if (fieldId == R.id.change_last_name_layout) {
                dto.setLastName(value);
            } else if (fieldId == R.id.change_profile_pic_layout) {
                dto.setPhotoURL(value);
            }
            userService.updateOrganizer(authHeader, userId, dto).enqueue((Callback<UpdateOrganizerResponseDTO>) createUpdateFieldCallback());
        } else if (role == UserRole.SPP) {
            UpdateProviderRequestDTO dto = new UpdateProviderRequestDTO();
            Location location = getCurrentLocationData(); // Retrieve the full location data

            if (fieldId == R.id.change_country_layout) {
                location.setCountry(value);
            } else if (fieldId == R.id.change_city_layout) {
                location.setCity(value);
            } else if (fieldId == R.id.change_street_layout) {
                location.setStreet(value);
            } else if (fieldId == R.id.change_house_number_layout) {
                location.setHouseNumber(value);
            }
            dto.setLocation(location);

            if (fieldId == R.id.change_phone_layout) {
                dto.setPhone(value);
            } else if (fieldId == R.id.change_org_desc_layout) {
                dto.setOrgDesc(value);
            } else if (fieldId == R.id.change_profile_pic_layout) {
                dto.setPhotoURL(value);
            }
            userService.updateProvider(authHeader, userId, dto).enqueue((Callback<UpdateProviderResponseDTO>) createUpdateFieldCallback());
        }
    }


    private Location getCurrentLocationData() {
        Location location = new Location();

        TextInputEditText city = requireView().findViewById(R.id.change_city);
        TextInputEditText country = requireView().findViewById(R.id.change_country);
        TextInputEditText street = requireView().findViewById(R.id.change_street);
        TextInputEditText houseNumber = requireView().findViewById(R.id.change_house_number);
        location.setCountry(Objects.requireNonNull(country.getText()).toString());
        location.setCity(Objects.requireNonNull(city.getText()).toString());
        location.setStreet(Objects.requireNonNull(street.getText()).toString());
        location.setHouseNumber(Objects.requireNonNull(houseNumber.getText()).toString());

        return location;
    }

    private Callback<?> createUpdateFieldCallback() {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    Log.d("UserProfileFragment", "Field updated successfully");
                } else {
                    Log.d("UserProfileFragment", "Failed to update field: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                Log.d("UserProfileFragment", "Error updating field: " + t.getMessage());
            }
        };
    }

    private void setupAllCarousels(View view, GetOrganizerResponseDTO orgDto, GetProviderResponseDTO proDto) {
        Set<BasicItemDTO> favServices;
        Set<BasicItemDTO> favProducts;

        if (role == UserRole.EVENT_ORG) {
            setupCarousel(view, R.id.favouriteEventsCarousel, orgDto.getFavoriteEvents());
            favServices = extractItems(orgDto.getFavoriteItems(), "services");
            favProducts = extractItems(orgDto.getFavoriteItems(), "products");
            setupCarousel(view, R.id.favouriteServicesCarousel, favServices);
            setupCarousel(view, R.id.favouriteProductsCarousel, favProducts);
            view.findViewById(R.id.my_events).setVisibility(View.VISIBLE);
            setupCarousel(view, R.id.myEventsCarousel, orgDto.getCreatedEvents());
        }
        else if (role == UserRole.SPP) {
            setupCarousel(view, R.id.favouriteEventsCarousel, proDto.getFavoriteEvents());
            favServices = extractItems(proDto.getFavoriteItems(), "services");
            favProducts = extractItems(proDto.getFavoriteItems(), "products");
            setupCarousel(view, R.id.favouriteServicesCarousel, favServices);
            setupCarousel(view, R.id.favouriteProductsCarousel, favProducts);
            view.findViewById(R.id.my_services).setVisibility(View.VISIBLE);
            view.findViewById(R.id.my_products).setVisibility(View.VISIBLE);
            setupCarousel(view, R.id.myServicesCarousel, extractItems(proDto.getItems(), "services"));
            setupCarousel(view, R.id.myProductsCarousel, extractItems(proDto.getItems(), "products"));
        }
    }

    private Set<BasicItemDTO> extractItems(Set<BasicItemDTO> items, String itemType) {
        Set<BasicItemDTO> favItems = new HashSet<>();
        for (var item : items) {
            if (item.getType().equals(itemType)) {
                favItems.add(item);
            }
        }
        return favItems;
    }

    private void setupCarousel(View view, int carouselId, Set<?> items) {
        RecyclerView carousel = view.findViewById(carouselId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        carousel.setLayoutManager(layoutManager);

        if (!items.isEmpty()) {
            if (items.iterator().next() instanceof BasicEventDTO) {
                Set<BasicEventDTO> events = (Set<BasicEventDTO>) items;
                carousel.setAdapter(new CarouselAdapter(requireContext(), new ArrayList<>(events), "events"));
            } else if (items.iterator().next() instanceof BasicItemDTO) {
                Set<BasicItemDTO> itemList = (Set<BasicItemDTO>) items;
                carousel.setAdapter(new CarouselAdapter(requireContext(), new ArrayList<>(itemList), "items"));
            }
        }
    }

    private void fillFields(View view) {
        Integer userId = JwtUtil.extractId(requireContext());
        String authHeader = JwtUtil.getAuthorizationValue(requireContext());

        if (role == UserRole.EVENT_ORG) {
            userService.getOrganizer(authHeader, userId).enqueue((Callback<GetOrganizerResponseDTO>) createFillCallback(view, true));
        } else if (role == UserRole.SPP) {
            userService.getProvider(authHeader, userId).enqueue((Callback<GetProviderResponseDTO>) createFillCallback(view, false));
        }
    }

    private Callback<?> createFillCallback(View view, boolean isOrganizer) {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    if (isOrganizer) {
                        GetOrganizerResponseDTO dto = (GetOrganizerResponseDTO) response.body();
                        fillFieldsFromDto(view, dto, null);
                    } else {
                        GetProviderResponseDTO dto = (GetProviderResponseDTO) response.body();
                        fillFieldsFromDto(view, null, dto);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                Log.d("UserProfileFragment", Objects.requireNonNull(t.getMessage()));
            }
        };
    }

    private void fillFieldsFromDto(View view, GetOrganizerResponseDTO orgDto, GetProviderResponseDTO proDto) {
        if (orgDto != null) {
            fillCommonFields(view, orgDto.getEmail(), orgDto.getPhone(), orgDto.getLocation().getCountry(),
                    orgDto.getLocation().getCity(), orgDto.getLocation().getStreet(), orgDto.getLocation().getHouseNumber(),
                    orgDto.getPhotoURL());

            fillField(view, R.id.change_first_name, orgDto.getFirstName());
            fillField(view, R.id.change_last_name, orgDto.getLastName());

            setupAllCarousels(view, orgDto, proDto);
        } else if (proDto != null) {
            fillCommonFields(view, proDto.getEmail(), proDto.getPhone(), proDto.getLocation().getCountry(),
                    proDto.getLocation().getCity(), proDto.getLocation().getStreet(), proDto.getLocation().getHouseNumber(),
                    proDto.getPhotoURL());

            fillField(view, R.id.change_org_name, proDto.getOrgName());
            fillField(view, R.id.change_org_desc, proDto.getOrgDesc());

            setupAllCarousels(view, null, proDto);
        }
    }

    private void fillCommonFields(View view, String email, String phone, String country, String city, String street, String houseNumber, String photoURL) {
        fillField(view, R.id.email_user_profile, email);
        fillField(view, R.id.change_phone, phone);
        fillField(view, R.id.change_country, country);
        fillField(view, R.id.change_city, city);
        fillField(view, R.id.change_street, street);
        fillField(view, R.id.change_house_number, houseNumber);
        fillField(view, R.id.change_profile_pic, photoURL);

        ShapeableImageView profilePic = view.findViewById(R.id.profile_picture);
        if (photoURL != null && !photoURL.isEmpty()) {
            Picasso.get()
                    .load(photoURL)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(profilePic);
        }
    }

    private void fillField(View view, int fieldId, String data) {
        TextInputEditText field = view.findViewById(fieldId);
        field.setText(data);
    }

    private void showChangePasswordDialog() {
        ChangePasswordFragment fragment = ChangePasswordFragment.newInstance(role);
        fragment.show(getParentFragmentManager(), "ChangePasswordFragment");
    }

    private void showConfirmDeactivationDialog() {
        ConfirmDeactivationFragment fragment = ConfirmDeactivationFragment.newInstance(role);
        fragment.show(getParentFragmentManager(), "ConfirmDeactivationFragment");
    }
}
