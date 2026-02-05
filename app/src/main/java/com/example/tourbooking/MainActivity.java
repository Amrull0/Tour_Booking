package com.example.tourbooking;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Загружаем первый фрагмент
        loadFragment(new HomeFragment());

        // Проверяем роль пользователя и настраиваем меню
        setupNavigation();

        // Обработка кликов по навигации
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_bookings) {
                fragment = new BookingsFragment();
            } else if (item.getItemId() == R.id.nav_favorites) {
                fragment = new FavoritesFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.nav_admin_tours) {
                fragment = new AdminToursFragment();
            } else if (item.getItemId() == R.id.nav_admin_bookings) {
                fragment = new AdminBookingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void setupNavigation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Проверяем роль пользователя
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String role = task.getResult().getString("role");
                            boolean isAdmin = "admin".equals(role);

                            Log.d("MAIN_ACTIVITY", "Роль пользователя: " + role + ", isAdmin: " + isAdmin);

                            // Показываем/скрываем админские пункты меню
                            bottomNav.getMenu().findItem(R.id.nav_admin_tours).setVisible(isAdmin);
                            bottomNav.getMenu().findItem(R.id.nav_admin_bookings).setVisible(isAdmin);
                        } else {
                            // Если не удалось получить роль, скрываем админские пункты
                            bottomNav.getMenu().findItem(R.id.nav_admin_tours).setVisible(false);
                            bottomNav.getMenu().findItem(R.id.nav_admin_bookings).setVisible(false);
                        }
                    });
        } else {
            // Пользователь не авторизован
            bottomNav.getMenu().findItem(R.id.nav_admin_tours).setVisible(false);
            bottomNav.getMenu().findItem(R.id.nav_admin_bookings).setVisible(false);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении в приложение обновляем меню (на случай смены пользователя)
        setupNavigation();
    }
}