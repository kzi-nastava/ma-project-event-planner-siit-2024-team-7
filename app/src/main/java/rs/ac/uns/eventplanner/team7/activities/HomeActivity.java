package rs.ac.uns.eventplanner.team7.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.dto.invitation.InvitationAcceptanceDTO;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.InvitationService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;
import rs.ac.uns.eventplanner.team7.utils.NotificationUtils;
import rs.ac.uns.eventplanner.team7.utils.WebSocketService;

public class HomeActivity extends AppCompatActivity {

    private final InvitationService invitationService = ClientUtils.injectService(InvitationService.class);
    private UserRole role;
    private final Set<Integer> topLevelDestinations = new HashSet<>();
    private NavController navController;
    private AppBarConfiguration appBarConfig;
    private WebSocketService webSocketService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        role = UserRole.valueOf(JwtUtil.getRole(this));
        if (role == UserRole.ADMIN) {
            setContentView(R.layout.activity_home_admin);
        } else if (role == UserRole.GUEST) {
            setContentView(R.layout.activity_home_base);
        } else if (role == UserRole.SPP) {
            setContentView(R.layout.activity_home_spp);
        } else {
            setContentView(R.layout.activity_home);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupNavigation();

        if (getIntent().getExtras() != null) handleInvitationAccepting();

        webSocketService = new WebSocketService(this);
        webSocketService.connect(JwtUtil.getToken(this));

        NotificationUtils.createNotificationChannel(this);
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {});
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfig) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuLayout = role == UserRole.GUEST ? R.menu.guest_toolbar_menu : R.menu.toolbar_menu;
        getMenuInflater().inflate(menuLayout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_login_logout) {
            JwtUtil.setDefaultValues(this);
            webSocketService.disconnect();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (itemId == R.id.nav_register) {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketService.disconnect();
    }

    private void setupNavigation() {
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = Objects.requireNonNull(navHost).getNavController();

        topLevelDestinations.add(R.id.nav_home);
        if (role == UserRole.GUEST) {
            appBarConfig = new AppBarConfiguration.Builder(topLevelDestinations).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
            return;
        }
        setupBottomNavbar();
        topLevelDestinations.addAll(Set.of(R.id.nav_chats, R.id.nav_account));
        final var appBarConfigBuilder = new AppBarConfiguration.Builder(topLevelDestinations);

        if (role == UserRole.ADMIN) {
            DrawerLayout drawerLayout = findViewById(R.id.home_drawer_layout);
            appBarConfigBuilder.setOpenableLayout(drawerLayout);

            NavigationView navigationDrawerView = findViewById(R.id.navigation_view);
            NavigationUI.setupWithNavController(navigationDrawerView, navController);
            navController.addOnDestinationChangedListener((controller, navDestination, bundle) -> {
                if (!topLevelDestinations.contains(navDestination.getId())) {
                    drawerLayout.closeDrawers();
                }
            });
        } else if (role == UserRole.SPP) {
            DrawerLayout drawerLayout = findViewById(R.id.home_drawer_layout_spp);
            appBarConfigBuilder.setOpenableLayout(drawerLayout);

            NavigationView navigationDrawerView = findViewById(R.id.navigation_view_spp);
            NavigationUI.setupWithNavController(navigationDrawerView, navController);
            navController.addOnDestinationChangedListener((controller, navDestination, bundle) -> {
                if (!topLevelDestinations.contains(navDestination.getId())) {
                    drawerLayout.closeDrawers();
                }
            });
        }

        appBarConfig = appBarConfigBuilder.build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
    }

    private void setupBottomNavbar() {
        // Guest users don't have bottom navbar!
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        if (role == UserRole.EVENT_ORG) {
            bottomNavigationView.inflateMenu(R.menu.event_organizer_menu);
            topLevelDestinations.add(R.id.nav_event);
        } else if (role == UserRole.SPP) {
            bottomNavigationView.inflateMenu(R.menu.spp_menu);
            topLevelDestinations.addAll(Set.of(R.id.nav_product, R.id.nav_service));
        } else {
            bottomNavigationView.inflateMenu(R.menu.basic_menu);
        }
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
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