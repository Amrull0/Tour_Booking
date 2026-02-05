package com.example.tourbooking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private TourAdapter tourAdapter;
    private List<Tour> favoriteTours;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.favorites_recycler);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyText = view.findViewById(R.id.empty_text);

        favoriteTours = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // ИСПРАВЛЕНИЕ ЗДЕСЬ: используй конструктор с одним параметром
        tourAdapter = new TourAdapter(getContext(), favoriteTours); // Только список

        tourAdapter.setOnItemClickListener(new TourAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Tour tour) {
                // Переход на детали тура
                Intent intent = new Intent(getActivity(), TourDetailActivity.class);
                intent.putExtra("tour", tour);
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(tourAdapter);

        loadFavorites();
    }

    private void loadFavorites() {
        String userId = mAuth.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Войдите в систему", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        // Загружаем избранные туры пользователя
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        favoriteTours.clear(); // Очищаем список

                        if (task.getResult().isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        // Счетчик для отслеживания загрузки всех туров
                        final int[] loadedCount = {0};
                        int totalFavorites = task.getResult().size();

                        // Если нет избранных - показываем пустой экран
                        if (totalFavorites == 0) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        // Для каждого избранного тура загружаем полные данные
                        for (QueryDocumentSnapshot favoriteDoc : task.getResult()) {
                            String tourId = favoriteDoc.getString("tourId");

                            if (tourId != null) {
                                loadTourDetails(tourId, totalFavorites, loadedCount);
                            } else {
                                // Если tourId null, считаем его загруженным
                                loadedCount[0]++;
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Ошибка загрузки избранного",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTourDetails(String tourId, int totalFavorites, int[] loadedCount) {
        db.collection("tours")
                .document(tourId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Tour tour = task.getResult().toObject(Tour.class);
                        if (tour != null) {
                            tour.setId(tourId);

                            // Проверяем, нет ли уже этого тура в списке
                            boolean alreadyExists = false;
                            for (Tour existingTour : favoriteTours) {
                                if (existingTour.getId().equals(tourId)) {
                                    alreadyExists = true;
                                    break;
                                }
                            }

                            if (!alreadyExists) {
                                favoriteTours.add(tour);
                            }
                        }
                    }

                    // Увеличиваем счетчик загруженных
                    loadedCount[0]++;

                    // Когда все туры загружены - обновляем адаптер
                    if (loadedCount[0] == totalFavorites) {
                        tourAdapter.notifyDataSetChanged();

                        if (favoriteTours.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            Log.d("FAVORITES", "Загружено туров: " + favoriteTours.size());
                        }
                    }
                });
    }

    private void loadTourDetails(String tourId) {
        db.collection("tours")
                .document(tourId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Tour tour = task.getResult().toObject(Tour.class);
                        if (tour != null) {
                            tour.setId(tourId);
                            favoriteTours.add(tour);
                            tourAdapter.notifyDataSetChanged();

                            if (!favoriteTours.isEmpty()) {
                                emptyText.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
}