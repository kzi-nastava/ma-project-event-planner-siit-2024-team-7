package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.dto.invitation.InvitationAcceptanceDTO;
import rs.ac.uns.eventplanner.team7.fragments.EventTypeListFragment;
import rs.ac.uns.eventplanner.team7.fragments.HomeFragment;
import rs.ac.uns.eventplanner.team7.fragments.SPPServicesBaseFragment;
import rs.ac.uns.eventplanner.team7.fragments.UserProfileFragment;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.InvitationService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class HomeActivity extends AppCompatActivity {

    private final InvitationService invitationService = ClientUtils.injectService(InvitationService.class);
    private Toolbar toolbar;
    private UserRole role;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        role = UserRole.valueOf(JwtUtil.getRole(this));

        // Initialize the DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.home_drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (role == UserRole.ADMIN) {
            setupAdminNav();
        }
        if (role != UserRole.GUEST) {
            setupBottomNavbar();
        }
        if (getIntent().getExtras() != null) handleInvitationAccepting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem chatItem = menu.findItem(R.id.nav_chats);
        MenuItem notificationsItem = menu.findItem(R.id.nav_notifications);
        if (role == UserRole.ADMIN) {
            chatItem.setVisible(false);
        } else if (role == UserRole.GUEST) {
            chatItem.setVisible(false);
            notificationsItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_chats) {
            // TODO
        } else if (item.getItemId() == R.id.nav_notifications) {
            // TODO
        } else if (item.getItemId() == R.id.nav_account) {
            View profileMenuItemView = findViewById(R.id.nav_account);
            PopupMenu popupMenu = new PopupMenu(this, profileMenuItemView);
            if (role == UserRole.GUEST) { //inflate it with guest menu
                popupMenu.getMenuInflater().inflate(R.menu.guest_profile_menu, popupMenu.getMenu());
            } else {
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
            }
            setAccountClickListener(popupMenu);
            popupMenu.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setAccountClickListener(PopupMenu popupMenu) {
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_logout || itemId == R.id.nav_sign_in) {
                if (itemId == R.id.nav_logout) {
                    JwtUtil.setDefaultValues(this);
                }
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            if (itemId == R.id.nav_my_account) {
                loadFragment(new UserProfileFragment());
                toolbar.setTitle(R.string.profile);
                return true;
            }
            if (itemId == R.id.nav_sign_up) {
                startActivity(new Intent(this, RegistrationActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupAdminNav() {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.openDrawer,  R.string.closeDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
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
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.setOnItemSelectedListener(item -> {
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
                .replace(R.id.home_main_fragment_container, fragment)
                .commit();
    }

    private void handleInvitationAccepting() {
        String bearerToken = JwtUtil.getAuthorizationValue(this);
        Bundle params = Objects.requireNonNull(getIntent().getExtras());
        InvitationAcceptanceDTO dto = new InvitationAcceptanceDTO(params);
        invitationService.acceptInvitation(bearerToken, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseMessageDTO> call, @NonNull Response<ResponseMessageDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this,
                            Objects.requireNonNull(response.body()).getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                switch (response.code()) {
                    case 400:
                        Toast.makeText(HomeActivity.this,
                                Objects.requireNonNull(response.body()).getMessage(),
                                Toast.LENGTH_LONG).show();
                        break;
                    case 401:
                        Toast.makeText(HomeActivity.this, R.string.invitation_not_found,
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(HomeActivity.this, R.string.error_accepting_invitation,
                                Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseMessageDTO> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, R.string.error_accepting_invitation,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

}