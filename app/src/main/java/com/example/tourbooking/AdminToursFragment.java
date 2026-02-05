package com.example.tourbooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class AdminToursFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button addTourButton;
    private TourAdapter tourAdapter;
    private List<Tour> tourList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_tours, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.admin_tours_recycler);
        addTourButton = view.findViewById(R.id.add_tour_button);

        tourList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Создаем адаптер в АДМИНСКОМ РЕЖИМЕ
        tourAdapter = new TourAdapter(getContext(), tourList, true);

        // Устанавливаем слушатели для админских действий
        tourAdapter.setOnAdminActionListener(new TourAdapter.OnAdminActionListener() {
            @Override
            public void onEditTour(Tour tour) {
                // Открываем экран редактирования
                Intent intent = new Intent(getActivity(), AddEditTourActivity.class);
                intent.putExtra("tourId", tour.getId());
                intent.putExtra("tour", tour);
                startActivity(intent);
            }

            @Override
            public void onDeleteTour(Tour tour) {
                // Удаляем тур
                deleteTour(tour);
            }
        });

        // Также можно открывать детали при клике
        tourAdapter.setOnItemClickListener(tour -> {
            Intent intent = new Intent(getActivity(), TourDetailActivity.class);
            intent.putExtra("tour", tour);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(tourAdapter);

        loadAllTours();

        addTourButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEditTourActivity.class);
            startActivity(intent);
        });
    }

    private void loadAllTours() {
        db.collection("tours")
                .get() // Все туры, включая неактивные
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tourList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // ПРАВИЛЬНОЕ ЧТЕНИЕ
                            Tour tour = new Tour();
                            tour.setId(document.getId());
                            tour.setTitle(document.getString("title"));
                            tour.setDescription(document.getString("description"));
                            tour.setImageUrl(document.getString("imageUrl"));

                            Object priceObj = document.get("price");
                            if (priceObj instanceof Long) {
                                tour.setPrice(((Long) priceObj).doubleValue());
                            } else if (priceObj instanceof Double) {
                                tour.setPrice((Double) priceObj);
                            }

                            Boolean active = document.getBoolean("active");
                            tour.setActive(active != null ? active : true);

                            tourList.add(tour);
                        }
                        tourAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Ошибка загрузки туров",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteTour(Tour tour) {
        if (tour.getId() == null) return;

        // Удаляем из Firebase
        db.collection("tours")
                .document(tour.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Удаляем из локального списка
                    int position = tourList.indexOf(tour);
                    if (position != -1) {
                        tourList.remove(position);
                        tourAdapter.notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Тур удален", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка удаления: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список при возвращении на фрагмент
        loadAllTours();
    }
}