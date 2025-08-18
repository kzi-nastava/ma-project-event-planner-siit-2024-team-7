package rs.ac.uns.eventplanner.team7.ui.fragments.admin.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.UpdateFeedbackRequestDTO;
import rs.ac.uns.eventplanner.team7.data.model.enums.FeedbackStatus;
import rs.ac.uns.eventplanner.team7.data.services.FeedbackService;
import rs.ac.uns.eventplanner.team7.ui.adapters.FeedbackAdapter;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;


public class AllFeedbackFragment extends Fragment {

    private final FeedbackService feedbackService = ClientUtils.injectService(FeedbackService.class);

    private RecyclerView feedbackView;
    private FeedbackAdapter adapter;
    private MaterialTextView messageTextView;
    private FeedbackDecisionDialogFragment decisionDialog;

    private final Page<FeedbackDTO> page = Page.getDefault();
    private boolean isLoading = true;
    private String bearerToken;

    public AllFeedbackFragment() {
        // Required empty public constructor
    }

    public static AllFeedbackFragment newInstance() {
        return new AllFeedbackFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_feedback, container, false);
        feedbackView = view.findViewById(R.id.all_feedback_recycler_view);
        messageTextView = view.findViewById(R.id.message_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        adapter = new FeedbackAdapter(page.getContent(), this::onDecide);
        feedbackView.setAdapter(adapter);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.feedback_swipe_refresh);
        setContent(false);

        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            setContent(false);
        });

        setupContentScrollListener();
    }

    private void onDecide(FeedbackDTO feedbackDTO) {
        if (decisionDialog != null) {
            decisionDialog.dismiss();
        }
        decisionDialog = FeedbackDecisionDialogFragment.newInstance();
        decisionDialog.setCancelable(false);
        decisionDialog.show(getChildFragmentManager(), "FeedbackDecisionDialog");
        decisionDialog.setOnDecisionClickListener(decidedStatus ->
                onDecisionClicked(feedbackDTO.getId(), decidedStatus));
    }

    private void onDecisionClicked(int feedbackId, FeedbackStatus decidedStatus) {
        feedbackService.update(bearerToken, feedbackId, new UpdateFeedbackRequestDTO(decidedStatus))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<FeedbackDTO> call,
                            @NonNull Response<FeedbackDTO> response
                    ) {
                        if (!isAdded()) return;
                        View view = getView();
                        if (response.isSuccessful() && response.body() != null) {
                            if (view != null && view.getContext() != null) {
                                Snackbar.make(requireView(), R.string.decision_submitted, Snackbar.LENGTH_SHORT).show();
                            }
                            setContent(false);
                            return;
                        }
                        var error = ClientUtils.convertToErrorMessage(response.errorBody());
                        if (error != null && view != null && view.getContext() != null) {
                            Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FeedbackDTO> call, @NonNull Throwable t) {
                        messageTextView.setText(R.string.unable_to_contact_server);
                    }
                });
    }

    private void setupContentScrollListener() {
        feedbackView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                var layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisiblePosition = Objects.requireNonNull(layoutManager).findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == adapter.getLastItemIndex() && !page.isLast())
                    onNextPage();
            }
        });
    }

    private void onNextPage() {
        if (isLoading || page.isLast()) return;
        page.nextPage();
        setContent(true);
    }

    private void setContent(boolean isUpdate) {
        isLoading = true;
        messageTextView.setText(R.string.fetching_data);
        if (!isUpdate) page.resetToDefault();
        feedbackService.getAllPending(bearerToken, page.getPageNumber()).enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<Page<FeedbackDTO>> call,
                    @NonNull Response<Page<FeedbackDTO>> response
            ) {
                if (!isAdded()) return;
                if (!isUpdate) adapter.clear();
                if (!response.isSuccessful()) {
                    messageTextView.setText(R.string.unable_to_contact_server);
                    return;
                }
                var pagedResponse = response.body();
                if (pagedResponse == null || pagedResponse.isEmpty()) {
                    if (page.isFirst()) {
                        messageTextView.setText(R.string.no_feedback);
                    }
                    return;
                }
                page.update(pagedResponse);
                adapter.addAll(page.getContent());
                isLoading = false;
                messageTextView.setText("");
            }

            @Override
            public void onFailure(@NonNull Call<Page<FeedbackDTO>> call, @NonNull Throwable t) {
                messageTextView.setText(R.string.unable_to_contact_server);
                isLoading = false;
            }
        });
    }
}