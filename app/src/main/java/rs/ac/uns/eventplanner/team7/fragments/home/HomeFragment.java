package rs.ac.uns.eventplanner.team7.fragments.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.HomePagerAdapter;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initTabs(view);
        return view;
    }

    private void initTabs(View view) {
        TabLayout tabLayout = view.findViewById(R.id.home_page_tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.home_page_view_pager);

        HomePagerAdapter adapter = new HomePagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Link the TabLayout and ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_top_events);
                    break;
                case 1:
                    tab.setText(R.string.tab_all_events);
                    break;
                case 2:
                    tab.setText(R.string.tab_top_services_products);
                    break;
                case 3:
                    tab.setText(R.string.tab_all_services_products);
                    break;
            }
        }).attach();

    }
}