package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.FavouriteEventRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetUserDetailsResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ActivityAdapter;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.GeocodingHelper;

public class EventDetailsFragment extends Fragment {

    private static final String event = "eventDTO";
    private GetEventResponseDTO eventDto;
    private GetUserDetailsResponseDTO organizerDTO;

    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final EventService eventService = ClientUtils.injectService(EventService.class);
    private String bearerToken;
    private UserRole role;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventDto = getArguments().getParcelable(event, GetEventResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        role = AuthUtil.extractRole(requireContext());

        MaterialButton exportEventButton = view.findViewById(R.id.btn_export_event_pdf);
        exportEventButton.setOnClickListener(v -> downloadEventPdf());

        MaterialButton exportGuestListButton = view.findViewById(R.id.btn_export_guest_list_pdf);
        exportGuestListButton.setOnClickListener(v -> downloadGuestListPdf());

        MaterialButton viewOrganizerButton = view.findViewById(R.id.btn_view_organizer);
        if (eventDto.isOwn()) {
            viewOrganizerButton.setVisibility(View.GONE);
        }
        viewOrganizerButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putParcelable("organizerDTO", organizerDTO);
            Navigation.findNavController(v).navigate(R.id.navigate_from_events_to_organizer_details, args);
        });

        MaterialButton chatButton = view.findViewById(R.id.btn_chat_w_organizer);
        if (role == UserRole.GUEST || eventDto.isOwn()) {
            chatButton.setVisibility(View.GONE);
        }
        chatButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("contactDTO", organizerDTO);
            Navigation.findNavController(view).navigate(R.id.nav_chats, bundle);

        });
        userService.getOrganizerByEventId(bearerToken, eventDto.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetUserDetailsResponseDTO> call,
                                   @NonNull Response<GetUserDetailsResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    organizerDTO = response.body();
                    chatButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetUserDetailsResponseDTO> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        initMap(view);
        populateEventDetails(view);
        initActivities(view);
        return view;
    }

    private void initButtons(View view) {
        ImageView markFavButton = view.findViewById(R.id.mark_favorite_star);
        Log.d("EventDetails", "isFav = " + eventDto.isFav()); // Debug

        if (role == UserRole.GUEST) {
            markFavButton.setVisibility(View.GONE);
        }
        if (eventDto.isFav()) {
            markFavButton.setImageResource(R.drawable.ic_star_filled);
        } else {
            markFavButton.setOnClickListener(v -> markFav(markFavButton));
        }

        FloatingActionButton eventStatisticsButton = view.findViewById(R.id.btn_event_statistics);
        int visibility = role == UserRole.ADMIN || (role == UserRole.EVENT_ORG && eventDto.isOwn()) ? View.VISIBLE : View.GONE;
        eventStatisticsButton.setVisibility(visibility);
        eventStatisticsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", eventDto);
            Navigation.findNavController(requireView()).navigate(R.id.navigate_to_event_statistics, bundle);
        });
    }

    private void markFav(ImageView btn) {
        userService.markEventAsFavourite(AuthUtil.getAuthorizationValue(requireContext()), AuthUtil.extractId(requireContext()), new FavouriteEventRequestDTO(eventDto.getId()))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseMessageDTO> call, @NonNull Response<ResponseMessageDTO> response) {
                        if (response.isSuccessful()) {
                            btn.setOnClickListener(v -> {});
                            btn.setImageResource(R.drawable.ic_star_filled);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseMessageDTO> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateEventDetails(View view) {
        if (eventDto == null) return;

        MaterialTextView nameTv = view.findViewById(R.id.event_name);
        MaterialTextView dateTimeTv = view.findViewById(R.id.event_date_time);
        MaterialTextView locationTv = view.findViewById(R.id.event_location);
        MaterialTextView descriptionTv = view.findViewById(R.id.event_description);
        MaterialTextView participantsTv = view.findViewById(R.id.event_participants);
        MaterialTextView eventTypeTv = view.findViewById(R.id.event_type);

        nameTv.setText(eventDto.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        dateTimeTv.setText(eventDto.getDate().format(formatter));
        locationTv.setText(eventDto.getFullAddress());
        descriptionTv.setText(eventDto.getDescription());
        eventTypeTv.setText(eventDto.getEventType().getName());
        participantsTv.setText(String.format("Participants: %s / %s",eventDto.getCurrentParticipants(), eventDto.getMaxParticipants()));

        initButtons(view);

        // If you have a cover image, you can load it with Glide/Picasso
        // ImageView eventImage = view.findViewById(R.id.event_image);
        // Glide.with(this).load(eventDto.getCoverImage()).into(eventImage);
    }

    private void initActivities(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.activities_list);
        recyclerView.setAdapter(new ActivityAdapter(eventDto.getActivities()));
    }

    private void initMap(View view) {
        MapView map = view.findViewById(R.id.event_map);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        map.setMultiTouchControls(true);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            GeocodingHelper.LatLng coords = GeocodingHelper.getCoordinates(
                    eventDto.getLocation().getCountry(),
                    eventDto.getLocation().getCity(),
                    eventDto.getLocation().getStreet(),
                    eventDto.getLocation().getHouseNumber()
            );

            handler.post(() -> {
                if (!isAdded()) return;
                if (coords != null) {
                    GeoPoint point = new GeoPoint(coords.lat, coords.lon);
                    map.getController().setZoom(15.0);
                    map.getController().setCenter(point);

                    Marker marker = new Marker(map);
                    marker.setPosition(point);
                    marker.setTitle("Event location");
                    map.getOverlays().add(marker);
                }
            });
        });
    }

    private void downloadEventPdf() {
        eventService.getEventDetailsPdf(AuthUtil.getAuthorizationValue(requireContext()), eventDto.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                // Save file to Downloads
                                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                File file = new File(downloadsDir, "event_details.pdf");
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(response.body().bytes());
                                fos.close();

                                Toast.makeText(requireContext(), "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to download PDF", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void downloadGuestListPdf() {
        eventService.getEventGuestListPdf(AuthUtil.getAuthorizationValue(requireContext()), eventDto.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                // Save file to Downloads
                                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                File file = new File(downloadsDir, "event_guest_list.pdf");
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(response.body().bytes());
                                fos.close();

                                Toast.makeText(requireContext(), "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to download PDF", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}