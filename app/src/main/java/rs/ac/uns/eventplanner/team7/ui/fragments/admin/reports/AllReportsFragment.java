package rs.ac.uns.eventplanner.team7.ui.fragments.admin.reports;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.UpdateReportRequestDTO;
import rs.ac.uns.eventplanner.team7.data.model.enums.ReportDecision;
import rs.ac.uns.eventplanner.team7.data.services.ReportService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ReportsAdapter;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;


public class AllReportsFragment extends Fragment {

    private final ReportService reportService = ClientUtils.injectService(ReportService.class);

    private RecyclerView reportsView;
    private ReportsAdapter adapter;
    private MaterialTextView messageTextView, titleTextView;
    private MaterialButton switchViewButton;
    private ReportDecisionDialogFragment decisionDialog;

    private final Page<ReportDTO> page = Page.getDefault();
    private boolean isLoading, showAll = true;
    private String bearerToken;

    public AllReportsFragment() {
        // Required empty public constructor
    }

    public static AllReportsFragment newInstance() {
        return new AllReportsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_reports, container, false);
        reportsView = view.findViewById(R.id.all_reports_recycler_view);
        titleTextView = view.findViewById(R.id.all_reports_title);
        messageTextView = view.findViewById(R.id.message_view);
        switchViewButton = view.findViewById(R.id.switch_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        adapter = new ReportsAdapter(page.getContent(), this::onDecide);
        reportsView.setAdapter(adapter);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.reports_swipe_refresh);
        setContent(false);

        switchViewButton.setOnClickListener(v -> {
            showAll = !showAll;
            setContent(false);
        });

        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            setContent(false);
        });

        setupContentScrollListener();
    }

    private void onDecide(ReportDTO report) {
        if (decisionDialog != null) {
            decisionDialog.dismiss();
        }
        decisionDialog = ReportDecisionDialogFragment.newInstance(report.getReportedFeedback() != null);
        decisionDialog.setCancelable(false);
        decisionDialog.show(getChildFragmentManager(), "ReportDecisionDialog");
        decisionDialog.setOnDecideClickListener(decision ->
                onDecisionClicked(report.getId(), decision)
        );
    }

    private void onDecisionClicked(int reportId, ReportDecision decision) {
        reportService.update(bearerToken, reportId, new UpdateReportRequestDTO(decision))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<List<ReportDTO>> call,
                            @NonNull Response<List<ReportDTO>> response
                    ) {
                        if (!isAdded()) return;
                        View view = getView();
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReportDTO> body = response.body();
                            if (view != null && view.getContext() != null) {
                                String message = requireContext().getString(R.string.report_resolved, body.size());
                                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
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
                    public void onFailure(@NonNull Call<List<ReportDTO>> call, @NonNull Throwable t) {
                        messageTextView.setText(R.string.unable_to_contact_server);
                    }
        });
    }

    private void setupContentScrollListener() {
        reportsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        Call<Page<ReportDTO>> serviceCall = showAll
                ? reportService.getAllReports(bearerToken, page.getPageNumber())
                : reportService.getAllUndecidedReports(bearerToken, page.getPageNumber());
        serviceCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<Page<ReportDTO>> call,
                    @NonNull Response<Page<ReportDTO>> response
            ) {
                if (!isAdded()) return;
                if (!isUpdate) adapter.clear();
                if (!response.isSuccessful()) {
                    messageTextView.setText(R.string.unable_to_contact_server);
                    return;
                }
                var pagedResponse = response.body();
                titleTextView.setText(showAll ? R.string.all_reports : R.string.all_undecided_reports);
                switchViewButton.setTooltipText(getString(showAll ? R.string.view_undecided : R.string.view_all));
                if (pagedResponse == null || pagedResponse.isEmpty()) {
                    if (page.isFirst()) {
                        messageTextView.setText(showAll ? R.string.no_reports : R.string.no_undecided_reports);
                    }
                    return;
                }
                page.update(pagedResponse);
                adapter.addAll(page.getContent());
                isLoading = false;
                messageTextView.setText("");
            }

            @Override
            public void onFailure(@NonNull Call<Page<ReportDTO>> call, @NonNull Throwable t) {
                messageTextView.setText(R.string.unable_to_contact_server);
                isLoading = false;
            }
        });
    }


}