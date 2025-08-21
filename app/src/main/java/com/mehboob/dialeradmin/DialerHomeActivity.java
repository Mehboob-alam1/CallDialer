package com.mehboob.dialeradmin;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mehboob.dialeradmin.fragment.CallHistoryFragment;
import com.mehboob.dialeradmin.fragment.ContactsFragment;
import com.mehboob.dialeradmin.fragment.DialPadFragment;

public class DialerHomeActivity extends AppCompatActivity implements MyApplication.OnModeChangeListener {
    private static final String TAG = "DialerHomeActivity";
    private static final long ANIMATION_DURATION = 300;

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabQuickDial;
    private Fragment currentFragment;
    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make status bar transparent and beautiful
        setupStatusBar();

        setContentView(R.layout.activity_dialer_home);

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupFab();
        setupWindowInsets();

        // Load initial fragment with animation
        if (savedInstanceState == null) {
            loadFragmentWithAnimation(new DialPadFragment(), false);
            bottomNavigationView.setSelectedItemId(R.id.nav_dialpad);
        }

        MyApplication.getInstance().setModeChangeListener(this);

        // Add entrance animations
        animateViewsEntrance();
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        fabQuickDial = findViewById(R.id.fabQuickDial);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (isAnimating) return false;

            int id = item.getItemId();
            Fragment fragment = createFragmentForId(id);

            if (fragment != null && !fragment.getClass().equals(getCurrentFragmentClass())) {
                loadFragmentWithAnimation(fragment, true);
                animateBottomNavSelection(item.getItemId());
                return true;
            }
            return false;
        });

        // Add initial scale animation
        bottomNavigationView.setScaleX(0f);
        bottomNavigationView.setScaleY(0f);
    }

    private void setupFab() {
        if (fabQuickDial != null) {
            fabQuickDial.setOnClickListener(v -> {
                // Add ripple animation
                animateFabPress();
                // Handle quick dial action here
            });

            // Initial state for animation
            fabQuickDial.setScaleX(0f);
            fabQuickDial.setScaleY(0f);
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),
                (v, insets) -> {
                    int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                    int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

                    // Adjust toolbar padding for status bar
                    toolbar.setPadding(toolbar.getPaddingLeft(), statusBarHeight,
                            toolbar.getPaddingRight(), toolbar.getPaddingBottom());

                    // Adjust bottom navigation margin for navigation bar
                    ViewGroup.MarginLayoutParams navParams =
                            (ViewGroup.MarginLayoutParams) bottomNavigationView.getLayoutParams();
                    navParams.bottomMargin = navigationBarHeight + 32; // 32dp base margin + nav bar height
                    bottomNavigationView.setLayoutParams(navParams);

                    // Adjust fragment container margin
                    ViewGroup.MarginLayoutParams containerParams =
                            (ViewGroup.MarginLayoutParams) findViewById(R.id.fragmentContainer).getLayoutParams();
                    containerParams.bottomMargin = navigationBarHeight + 120; // Space for bottom nav + padding
                    findViewById(R.id.fragmentContainer).setLayoutParams(containerParams);

                    return insets;
                });
    }

    private Fragment createFragmentForId(int id) {
        if (id == R.id.nav_dialpad) {
            return new DialPadFragment();
        } else if (id == R.id.nav_history) {
            return new CallHistoryFragment();
        } else if (id == R.id.nav_contacts) {
            return new ContactsFragment();
        }
        return null;
    }

    private Class<?> getCurrentFragmentClass() {
        return currentFragment != null ? currentFragment.getClass() : null;
    }

    private void loadFragmentWithAnimation(Fragment fragment, boolean animate) {
        if (isAnimating && animate) return;

        isAnimating = true;
        currentFragment = fragment;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (animate) {
            // Custom slide animations
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }

        transaction.replace(R.id.fragmentContainer, fragment)
                .commit();

        // Reset animation flag after animation completes
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isAnimating = false;
        }, animate ? ANIMATION_DURATION : 100);
    }

    private void animateViewsEntrance() {
        // Animate toolbar
        toolbar.setTranslationY(-200f);
        toolbar.setAlpha(0f);
        toolbar.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animate bottom navigation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bottomNavigationView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }, 150);

        // Animate FAB if visible
        if (fabQuickDial != null && fabQuickDial.getVisibility() == View.VISIBLE) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                fabQuickDial.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }, 300);
        }
    }

    private void animateBottomNavSelection(int selectedId) {
        // Find selected item view and animate it
        View selectedView = bottomNavigationView.findViewById(selectedId);
        if (selectedView != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(selectedView, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(selectedView, "scaleY", 1f, 1.1f, 1f);

            scaleX.setDuration(200);
            scaleY.setDuration(200);
            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

            scaleX.start();
            scaleY.start();
        }
    }

    private void animateFabPress() {
        if (fabQuickDial != null) {
            fabQuickDial.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        fabQuickDial.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }
    }

    @Override
    public void onModeChanged(boolean isAdminMode) {
        if (isAdminMode) {
            // Add exit animation before switching
            animateExit(() -> {
                Intent intent = new Intent(this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void animateExit(Runnable onComplete) {
        bottomNavigationView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(200);

        toolbar.animate()
                .translationY(-200f)
                .alpha(0f)
                .setDuration(200);

        findViewById(R.id.fragmentContainer).animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(onComplete)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().removeModeChangeListener();
    }

    @Override
    public void onBackPressed() {
        // Add custom back animation if needed
        if (currentFragment instanceof DialPadFragment) {
            super.onBackPressed();
        } else {
            // Navigate back to dialer
            bottomNavigationView.setSelectedItemId(R.id.nav_dialpad);
        }
    }
}