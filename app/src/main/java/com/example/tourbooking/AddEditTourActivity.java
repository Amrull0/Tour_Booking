package com.example.tourbooking;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditTourActivity extends AppCompatActivity {

    private EditText titleInput, descriptionInput, fromCountryInput, toCountryInput,
            priceInput, imageUrlInput, totalPlacesInput;
    private Button saveButton, cancelButton;
    private FirebaseFirestore db;
    private String tourId; // Если редактирование

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_tour);

        // Инициализация полей ввода
        titleInput = findViewById(R.id.title_input);
        descriptionInput = findViewById(R.id.description_input);
        fromCountryInput = findViewById(R.id.from_country_input);
        toCountryInput = findViewById(R.id.to_country_input);
        totalPlacesInput = findViewById(R.id.total_places_input);
        priceInput = findViewById(R.id.price_input);
        imageUrlInput = findViewById(R.id.image_url_input);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        db = FirebaseFirestore.getInstance();

        // Проверяем, редактируем ли существующий тур
        tourId = getIntent().getStringExtra("tourId");
        if (tourId != null) {
            // Загружаем данные тура для редактирования
            loadTourData();
        }

        saveButton.setOnClickListener(v -> saveTour());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadTourData() {
        if (tourId != null) {
            db.collection("tours")
                    .document(tourId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Tour tour = documentSnapshot.toObject(Tour.class);
                            if (tour != null) {
                                titleInput.setText(tour.getTitle());
                                descriptionInput.setText(tour.getDescription());
                                fromCountryInput.setText(tour.getFromCountry());
                                toCountryInput.setText(tour.getToCountry());
                                totalPlacesInput.setText(String.valueOf(tour.getTotalPlaces()));
                                priceInput.setText(String.valueOf(tour.getPrice()));
                                imageUrlInput.setText(tour.getImageUrl());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка загрузки тура", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveTour() {
        // Получаем значения из полей
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String fromCountry = fromCountryInput.getText().toString().trim();
        String toCountry = toCountryInput.getText().toString().trim();
        String totalPlacesText = totalPlacesInput.getText().toString().trim();
        String priceText = priceInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        // Валидация обязательных полей
        if (title.isEmpty() || description.isEmpty() || fromCountry.isEmpty() ||
                toCountry.isEmpty() || totalPlacesText.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Преобразование и валидация числовых значений
        int totalPlaces;
        double price;

        try {
            totalPlaces = Integer.parseInt(totalPlacesText);
            if (totalPlaces <= 0) {
                Toast.makeText(this, "Количество мест должно быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное количество мест", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) {
                Toast.makeText(this, "Цена должна быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректную цену", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем объект тура
        Map<String, Object> tour = new HashMap<>();
        tour.put("title", title);
        tour.put("description", description);
        tour.put("fromCountry", fromCountry);
        tour.put("toCountry", toCountry);
        tour.put("totalPlaces", totalPlaces);

        // Если редактирование, сохраняем текущие доступные места
        if (tourId == null) {
            // Новый тур: все места свободны
            tour.put("availablePlaces", totalPlaces);
        } else {
            // При редактировании сохраняем текущие доступные места
            // Их нужно будет загрузить отдельно или передать в Intent
            // Пока что установим равными totalPlaces (нужно улучшить)
            tour.put("availablePlaces", totalPlaces);
        }

        tour.put("price", price);
        tour.put("imageUrl", imageUrl.isEmpty() ? "" : imageUrl);
        tour.put("active", true);

        if (tourId == null) {
            // Добавление нового тура
            db.collection("tours")
                    .add(tour)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Тур добавлен", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Редактирование существующего
            db.collection("tours")
                    .document(tourId)
                    .update(tour)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Тур обновлен", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}