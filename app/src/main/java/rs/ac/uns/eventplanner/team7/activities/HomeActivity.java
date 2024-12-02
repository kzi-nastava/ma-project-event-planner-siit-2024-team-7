package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.fragments.HomeFragment;
import rs.ac.uns.eventplanner.team7.fragments.SPPServicesBaseFragment;
import rs.ac.uns.eventplanner.team7.fragments.UserProfileFragment;

public class HomeActivity extends AppCompatActivity {

    private boolean isGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        isGuest = getIntent().getBooleanExtra("isGuest", false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navbarSetup();
        loadFragment(new HomeFragment());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_account) {
            View profileMenuItemView = findViewById(R.id.nav_account);
            PopupMenu popupMenu = new PopupMenu(this, profileMenuItemView);
            if (isGuest) { //inflate it with guest menu
                popupMenu.getMenuInflater().inflate(R.menu.guest_profile_menu, popupMenu.getMenu());
            }
            else {
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
            }

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

            popupMenu.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void navbarSetup() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                Toolbar toolbar = findViewById(R.id.toolbar);
                // add the cases here
                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    toolbar.setTitle(item.getTitle());
                }
                else if (item.getItemId() == R.id.nav_service) {
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

}