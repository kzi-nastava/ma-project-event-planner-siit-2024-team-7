package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.fragments.EventTypeListFragment;
import rs.ac.uns.eventplanner.team7.fragments.HomeFragment;
import rs.ac.uns.eventplanner.team7.fragments.SPPServicesBaseFragment;
import rs.ac.uns.eventplanner.team7.fragments.UserProfileFragment;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class HomeActivity extends AppCompatActivity {

    private boolean isGuest;
    private UserRole role;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        isGuest = getIntent().getBooleanExtra("isGuest", false);
        role = UserRole.valueOf(JwtUtil.getRole(this));

        // Initialize the DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupBottomNavbar();

        // Check if the user is an admin or not
        if (role == UserRole.ADMIN) {
            setupAdminNav();
        }

        loadFragment(new HomeFragment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (role != UserRole.ADMIN) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (role == UserRole.ADMIN) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        if (item.getItemId() == R.id.nav_account) {
            View profileMenuItemView = findViewById(R.id.nav_account);
            PopupMenu popupMenu = new PopupMenu(this, profileMenuItemView);
            if (isGuest) { //inflate it with guest menu
                popupMenu.getMenuInflater().inflate(R.menu.guest_profile_menu, popupMenu.getMenu());
            }
            else {
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
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_logout) { //logout action
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                if (item.getItemId() == R.id.nav_my_account) {
                    Fragment userProfileFragment = new UserProfileFragment();
                    loadFragment(userProfileFragment);
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
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    private void setupAdminNav() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.event_type) {
                loadFragment(new EventTypeListFragment());
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupBottomNavbar() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });
    }
}
