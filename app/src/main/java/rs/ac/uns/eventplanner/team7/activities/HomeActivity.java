package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.fragments.EventTypeListFragment;
import rs.ac.uns.eventplanner.team7.fragments.HomeFragment;
import rs.ac.uns.eventplanner.team7.fragments.SPPServicesBaseFragment;
import rs.ac.uns.eventplanner.team7.fragments.UserProfileFragment;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class HomeActivity extends AppCompatActivity {

    private boolean isGuest;
    private Toolbar toolbar;
    private UserRole role;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        isGuest = getIntent().getBooleanExtra("isGuest", false);
        role = UserRole.valueOf(JwtUtil.getRole(this));

        // Initialize the DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.home_drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (role == UserRole.ADMIN) {
            setupAdminNav();
        }
        setupBottomNavbar();
        loadFragment(new HomeFragment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        if (role == UserRole.ADMIN) {
            MenuItem chatItem = menu.findItem(R.id.nav_chats);
            if (chatItem != null) {
                menu.removeItem(R.id.nav_chats);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_account) {
            View profileMenuItemView = findViewById(R.id.nav_account);
            PopupMenu popupMenu = new PopupMenu(this, profileMenuItemView);
            if (isGuest) { //inflate it with guest menu
                popupMenu.getMenuInflater().inflate(R.menu.guest_profile_menu, popupMenu.getMenu());
            } else {
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
            }
            setAccountClickListener(popupMenu);
            popupMenu.show();
        } else if (item.getItemId() == R.id.nav_logout) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setAccountClickListener(PopupMenu popupMenu) {
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_my_account) {
                loadFragment(new UserProfileFragment());
                toolbar.setTitle(R.string.profile);
                return true;
            }
            if (item.getItemId() == R.id.nav_sign_in) {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_sign_up) {
                Intent intent = new Intent(HomeActivity.this, RegistrationActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupAdminNav() {
        MaterialTextView headerText = navigationView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
        headerText.setText(R.string.admin_options);
        navigationView.inflateMenu(R.menu.admin_nav_drawer_menu);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.event_type) {
                loadFragment(new EventTypeListFragment());
                toolbar.setTitle(item.getTitle());
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupBottomNavbar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                toolbar.setTitle(item.getTitle());
            } else if (item.getItemId() == R.id.nav_service) {
                selectedFragment = new SPPServicesBaseFragment();
                toolbar.setTitle(item.getTitle());
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

}