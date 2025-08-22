package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.event.EventStatistics;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class EventStatisticsFragment extends Fragment {
    private static final String EVENT = "event";
    private GetEventResponseDTO eventDTO;
    private EventStatistics eventStatistics;

    private final EventService eventService = ClientUtils.injectService(EventService.class);

    private TextView eventName;
    private TextView eventDate;
    private TextView currentParticipants;
    private TextView maxParticipants;
    private TextView attendanceRate;
    private TextView avgRating;

    public EventStatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventDTO = getArguments().getParcelable(EVENT, GetEventResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_statistics, container, false);

        eventName = view.findViewById(R.id.event_name);
        eventDate = view.findViewById(R.id.event_date);
        currentParticipants = view.findViewById(R.id.current_participants);
        maxParticipants = view.findViewById(R.id.max_participants);
        attendanceRate = view.findViewById(R.id.attendance_rate);
        avgRating = view.findViewById(R.id.avg_rating);

        Button exportStatisticsButton = view.findViewById(R.id.btn_export_statistics_pdf);
        exportStatisticsButton.setOnClickListener(v -> downloadStatisticsPdf());

        fillEventDetails();
        fetchStatistics();

        return view;
    }

    private void downloadStatisticsPdf() {
        eventService.getEventStatisticsPdf(AuthUtil.getAuthorizationValue(requireContext()), eventDTO.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                // Save file to Downloads
                                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                File file = new File(downloadsDir, "event_statistics.pdf");
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

    private void fetchStatistics() {
        eventService.getEventStatistics(AuthUtil.getAuthorizationValue(requireContext()), eventDTO.getId())
                .enqueue(new Callback<EventStatistics>() {
                    @Override
                    public void onResponse(@NonNull Call<EventStatistics> call, @NonNull Response<EventStatistics> response) {
                        if (response.isSuccessful()) {
                            eventStatistics = response.body();
                            fillStatistics();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EventStatistics> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fillEventDetails() {
        eventName.setText("The statistics for the event: " + eventDTO.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm");

        String formattedDate = eventDTO.getDate().format(formatter);
        eventDate.setText("Event held on: " + formattedDate);
    }

    private void fillStatistics() {
        currentParticipants.setText("Current Participants: " + eventStatistics.getCurrentParticipants());

        maxParticipants.setText("Max Participants: " + eventDTO.getMaxParticipants());

        // Attendance rate in %
        double rate = eventStatistics.getAttendanceRate() != null ? eventStatistics.getAttendanceRate() * 100 : 0.0;
        attendanceRate.setText(String.format("Attendance Rate: %.1f%%", rate));

        // Rating formatted nicely
        double rating = eventStatistics.getAvgRating() != null ? eventStatistics.getAvgRating() : 0.0;
        avgRating.setText(String.format("Average Rating: %.1f / 5", rating));
    }
}