package rs.ac.uns.eventplanner.team7.ui.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.dto.invitation.InvitationAcceptanceDTO;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.InvitationService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.NotificationUtils;
import rs.ac.uns.eventplanner.team7.utils.TokenInterceptor;
import rs.ac.uns.eventplanner.team7.utils.WebSocketService;

public class HomeActivity extends AppCompatActivity {

    private final InvitationService invitationService = ClientUtils.injectService(InvitationService.class);
    private UserRole role;
    private final Set<Integer> topLevelDestinations = new HashSet<>();
    private NavController navController;
    private WebSocketService webSocketService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        role = AuthUtil.extractRole(this);
        if (role == UserRole.ADMIN) {
            setContentView(R.layout.activity_home_admin);
        } else if (role == UserRole.GUEST) {
            setContentView(R.layout.activity_home_base);
        } else if (role == UserRole.SPP) {
            setContentView(R.layout.activity_home_spp);
        } else {
            setContentView(R.layout.activity_home);
        }
        setupNavigation();
        checkStoragePermissions();
        if (role == UserRole.GUEST) return;
        setupNotifications();
        TokenInterceptor.register(AuthUtil.extractExpirationDate(this), this::handleExpiredToken);
        if (getIntent().getExtras() != null) handleIntentParams(getIntent().getExtras());
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
            handleLogout();
            return true;
        } else if (itemId == R.id.nav_register) {
            handleRegister();
            return true;
        } else if (itemId == R.id.nav_notifications) {
            handleNotificationsNavigation();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (role != UserRole.GUEST) {
                    webSocketService = new WebSocketService(this);
                }
            } else {
                Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketService != null) {
            webSocketService.disconnect();
            getSystemService(NotificationManager.class).cancelAll();
        }
        TokenInterceptor.unregister();
    }

    // Occurs when tapping the notification while the app is running
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) handleIntentParams(intent.getExtras());
    }

    private void checkStoragePermissions() {
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {});
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

    }

    private void setupNotifications() {
        NotificationUtils.createNotificationChannel(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    102
            );
            return;
        }
        if (webSocketService == null) {
            webSocketService = new WebSocketService(this);
            webSocketService.connect(AuthUtil.getAuthorizationValue(this));
        }
    }

    private void handleExpiredToken() {
        runOnUiThread(() -> new MaterialAlertDialogBuilder(HomeActivity.this)
                .setTitle(getString(R.string.session_expired))
                .setMessage(getString(R.string.session_expired_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.proceed), (d, w) -> handleLogout())
                .show());
    }

    private void handleLogout() {
        AuthUtil.clearPreferences(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleRegister() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = Objects.requireNonNull(navHost).getNavController();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (AuthUtil.isSuspended(this)) {
            toolbar.setVisibility(View.GONE);
            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.nav_home, true)
                    .build();
            navController.navigate(R.id.nav_suspended, null, navOptions);
            return;
        }
        setSupportActionBar(toolbar);

        topLevelDestinations.add(R.id.nav_home);
        AppBarConfiguration appBarConfig;
        if (role == UserRole.GUEST) {
            appBarConfig = new AppBarConfiguration.Builder(topLevelDestinations).build();
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfig);
            return;
        }
        setupBottomNavbar(findViewById(R.id.bottom_navigation_view));
        final var appBarConfigBuilder = new AppBarConfiguration.Builder(topLevelDestinations);

        if (role == UserRole.ADMIN) {
            DrawerLayout drawerLayout = findViewById(R.id.home_drawer_layout);
            appBarConfigBuilder.setOpenableLayout(drawerLayout);

            NavigationView navigationDrawerView = findViewById(R.id.navigation_view);
            NavigationUI.setupWithNavController(navigationDrawerView, navController);
            navController.addOnDestinationChangedListener(
                    (c, destination, bundle) -> {
                        if (!topLevelDestinations.contains(destination.getId()))
                            drawerLayout.closeDrawers();
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
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfig);
    }

    private void setupBottomNavbar(BottomNavigationView bottomNavigationView) {
        topLevelDestinations.addAll(Set.of(R.id.nav_contacts, R.id.nav_account));
        if (role == UserRole.EVENT_ORG) {
            bottomNavigationView.inflateMenu(R.menu.event_organizer_menu);
            topLevelDestinations.add(R.id.nav_event);
        } else if (role == UserRole.SPP) {
            bottomNavigationView.inflateMenu(R.menu.spp_menu);
            topLevelDestinations.addAll(Set.of(R.id.nav_product_list, R.id.nav_service));
        } else {
            bottomNavigationView.inflateMenu(R.menu.basic_menu);
        }
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        navController.addOnDestinationChangedListener((c, destination, b)
                -> bottomNavigationView.setVisibility(
                        topLevelDestinations.contains(destination.getId()) ? View.VISIBLE : View.GONE));

    }

    private void handleIntentParams(Bundle extras) {
        if ("notifications".equals(extras.getString("navigate_to"))) {
            handleNotificationsNavigation();
        }
        else if ("chats".equals(extras.getString("navigate_to"))) {
            handleChatNotificationNavigation(extras.getBundle("message"));
        }
        else handleInvitationAccepting();
    }

    private void handleNotificationsNavigation() {
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() != R.id.nav_notifications) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build();
            navController.navigate(R.id.nav_notifications, null, navOptions);
        }
    }

    private void handleChatNotificationNavigation(Bundle bundle) {
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() != R.id.nav_chats) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .build();
            navController.navigate(R.id.nav_chats, bundle, navOptions);
        }
    }

    private void handleInvitationAccepting() {
        String bearerToken = AuthUtil.getAuthorizationValue(this);
        InvitationAcceptanceDTO dto;
        try {
            Bundle params = Objects.requireNonNull(getIntent().getExtras());
            dto = new InvitationAcceptanceDTO(params);
        } catch (IllegalStateException | NullPointerException e) {
            return;
        }
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