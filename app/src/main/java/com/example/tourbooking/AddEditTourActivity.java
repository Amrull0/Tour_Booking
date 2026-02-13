package com.example.tourbooking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddEditTourActivity extends AppCompatActivity {

    private EditText titleInput, descriptionInput, fromCountryInput, toCountryInput,
            priceInput, imageUrlInput, totalPlacesInput;
    private Button saveButton, cancelButton;
    private FirebaseFirestore db;
    private String tourId;
    private Button selectStartDateBtn, selectEndDateBtn;
    private TextView startDateText, endDateText;
    private long startDate = 0;
    private long endDate = 0;
    private int duration = 0;

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
        selectStartDateBtn = findViewById(R.id.select_start_date_button);
        selectEndDateBtn = findViewById(R.id.select_end_date_button);
        startDateText = findViewById(R.id.start_date_text);
        endDateText = findViewById(R.id.end_date_text);

        db = FirebaseFirestore.getInstance();

        // Настройка DatePicker
        setupDatePickers();

        // Проверяем, редактируем ли существующий тур
        tourId = getIntent().getStringExtra("tourId");
        if (tourId != null) {
            // Загружаем данные тура для редактирования
            loadTourData();
        }

        saveButton.setOnClickListener(v -> saveTour());
        cancelButton.setOnClickListener(v -> finish());
    }
    private void setupDatePickers() {
        selectStartDateBtn.setOnClickListener(v -> showDatePicker(true));
        selectEndDateBtn.setOnClickListener(v -> showDatePicker(false));
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "Дата не выбрана";

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);

                    if (isStartDate) {
                        startDate = selected.getTimeInMillis();
                        startDateText.setText(formatDate(startDate));

                        // Если уже выбрана дата окончания, пересчитываем продолжительность
                        if (endDate > 0) {
                            calculateDuration();
                        }
                    } else {
                        // Проверяем, что дата окончания позже даты начала
                        if (startDate > 0 && selected.getTimeInMillis() <= startDate) {
                            Toast.makeText(this, "Дата окончания должна быть позже даты начала",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        endDate = selected.getTimeInMillis();
                        endDateText.setText(formatDate(endDate));

                        // Пересчитываем продолжительность
                        if (startDate > 0) {
                            calculateDuration();
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private void calculateDuration() {
        if (startDate > 0 && endDate > 0) {
            long diff = endDate - startDate;
            duration = (int) (diff / (1000 * 60 * 60 * 24)) + 1; // +1 чтобы включить и начальный день
            Toast.makeText(this, "Продолжительность: " + duration + " дней",
                    Toast.LENGTH_SHORT).show();
        }
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

                                // ЗАГРУЗКА ДАТ:
                                if (tour.getStartDate() > 0) {
                                    startDate = tour.getStartDate();
                                    startDateText.setText(formatDate(startDate));
                                }
                                if (tour.getEndDate() > 0) {
                                    endDate = tour.getEndDate();
                                    endDateText.setText(formatDate(endDate));
                                }
                                if (tour.getDuration() > 0) {
                                    duration = tour.getDuration();
                                }
                            }
                        }
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
        tour.put("availablePlaces", totalPlaces);
        tour.put("price", price);
        tour.put("imageUrl", imageUrl.isEmpty() ? "" : imageUrl);
        tour.put("active", true);
        tour.put("startDate", startDate);
        tour.put("endDate", endDate);
        tour.put("duration", duration);

        if (tourId == null) {
            tour.put("availablePlaces", totalPlaces);
        } else {
            tour.put("availablePlaces", totalPlaces);
        }

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
        // ВАЛИДАЦИЯ ДАТ:
        if (startDate == 0 || endDate == 0) {
            Toast.makeText(this, "Выберите даты начала и окончания тура",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDate <= startDate) {
            Toast.makeText(this, "Дата окончания должна быть позже даты начала",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Пересчитываем продолжительность перед сохранением
        calculateDuration();
    }
}