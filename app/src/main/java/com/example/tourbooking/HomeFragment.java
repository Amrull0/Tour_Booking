package com.example.tourbooking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private TourAdapter tourAdapter;
    private List<Tour> tourList;
    private FirebaseFirestore db;
    private Spinner filterSortSpinner;
    private String selectedSort = "По умолчанию";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("DEBUG", "HomeFragment запущен");

        // Инициализация
        recyclerView = view.findViewById(R.id.tours_recycler);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyText = view.findViewById(R.id.empty_text);

        // ТОЛЬКО сортировка, без фильтра по странам
        filterSortSpinner = view.findViewById(R.id.filter_sort_spinner);

        tourList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // ВЫЗОВ МЕТОДА setupRecyclerView() ← ДОБАВЬ ЭТУ СТРОЧКУ
        setupRecyclerView();

        // Настройка сортировки
        setupSortSpinner();

        // Загружаем туры
        loadTours();
    }

    private void setupRecyclerView() {
        tourAdapter = new TourAdapter(getContext(), tourList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(tourAdapter);

        Log.d("DEBUG", "RecyclerView создан. Адаптер установлен.");


        tourAdapter.setOnItemClickListener(new TourAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Tour tour) {
                Log.d("DEBUG", "Клик по туру из адаптера: " + tour.getTitle());

                Intent intent = new Intent(getActivity(), TourDetailActivity.class);
                intent.putExtra("tour", tour);
                startActivity(intent);
            }
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSortSpinner.setAdapter(sortAdapter);

        filterSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newSort = parent.getItemAtPosition(position).toString();
                if (!selectedSort.equals(newSort)) {
                    selectedSort = newSort;
                    applyLocalSorting(); // Сортируем локально без запроса к БД
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ничего не делаем
            }
        });
    }

    private void applyLocalSorting() {
        if (tourList.isEmpty()) return;

        Log.d("FILTER", "Применяем локальную сортировку: " + selectedSort);

        switch (selectedSort) {
            case "Цена (по возрастанию)":
                tourList.sort((t1, t2) -> Double.compare(t1.getPrice(), t2.getPrice()));
                break;
            case "Цена (по убыванию)":
                tourList.sort((t1, t2) -> Double.compare(t2.getPrice(), t1.getPrice()));
                break;
            case "Больше мест":
                tourList.sort((t1, t2) -> Integer.compare(t2.getAvailablePlaces(), t1.getAvailablePlaces()));
                break;
            case "По умолчанию":
            default:
                // Сортировка по стране (по алфавиту)
                tourList.sort((t1, t2) -> {
                    String country1 = t1.getToCountry() != null ? t1.getToCountry() : "";
                    String country2 = t2.getToCountry() != null ? t2.getToCountry() : "";
                    return country1.compareTo(country2);
                });
                break;
        }
    }

    private void loadTours() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        // ПРОСТОЙ ЗАПРОС без фильтрации по стране
        db.collection("tours")
                .whereEqualTo("active", true)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        tourList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Tour tour = document.toObject(Tour.class);
                            tour.setId(document.getId());
                            tourList.add(tour);
                        }

                        // Применяем сортировку по умолчанию
                        applyLocalSorting();

                        if (tourList.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                        } else {
                            emptyText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void processQueryResult(com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task) {
        progressBar.setVisibility(View.GONE);

        if (task.isSuccessful()) {
            tourList.clear();
            for (QueryDocumentSnapshot document : task.getResult()) {
                try {
                    Tour tour = document.toObject(Tour.class);
                    tour.setId(document.getId());

                    // Если поля не заполнились через toObject, заполняем вручную
                    if (tour.getTitle() == null) {
                        tour.setTitle(document.getString("title"));
                    }
                    if (tour.getDescription() == null) {
                        tour.setDescription(document.getString("description"));
                    }
                    if (tour.getToCountry() == null) {
                        // Пробуем получить из поля "country" или "toCountry"
                        String country = document.getString("country");
                        if (country != null) {
                            tour.setToCountry(country);
                        } else {
                            tour.setToCountry(document.getString("toCountry"));
                        }
                    }
                    if (tour.getFromCountry() == null) {
                        tour.setFromCountry(document.getString("fromCountry"));
                    }
                    if (tour.getImageUrl() == null) {
                        tour.setImageUrl(document.getString("imageUrl"));
                    }

                    // Числовые поля
                    Object priceObj = document.get("price");
                    if (priceObj instanceof Long) {
                        tour.setPrice(((Long) priceObj).doubleValue());
                    } else if (priceObj instanceof Double) {
                        tour.setPrice((Double) priceObj);
                    } else {
                        tour.setPrice(0.0);
                    }

                    Object totalPlacesObj = document.get("totalPlaces");
                    if (totalPlacesObj instanceof Long) {
                        tour.setTotalPlaces(((Long) totalPlacesObj).intValue());
                    } else if (totalPlacesObj instanceof Integer) {
                        tour.setTotalPlaces((Integer) totalPlacesObj);
                    } else {
                        tour.setTotalPlaces(20);
                    }

                    Object availablePlacesObj = document.get("availablePlaces");
                    if (availablePlacesObj instanceof Long) {
                        tour.setAvailablePlaces(((Long) availablePlacesObj).intValue());
                    } else if (availablePlacesObj instanceof Integer) {
                        tour.setAvailablePlaces((Integer) availablePlacesObj);
                    } else {
                        tour.setAvailablePlaces(tour.getTotalPlaces());
                    }

                    Boolean active = document.getBoolean("active");
                    tour.setActive(active != null ? active : true);

                    tourList.add(tour);
                } catch (Exception e) {
                    Log.e("FILTER", "Ошибка парсинга тура: " + e.getMessage());
                }
            }

            Log.d("FILTER", "Загружено туров: " + tourList.size());

            // Применяем текущую сортировку
            applyLocalSorting();

            if (tourList.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e("FILTER", "Ошибка загрузки: " + task.getException());
            Toast.makeText(getContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список при возвращении на фрагмент
        if (db != null) {
            loadTours();
        }
    }
}