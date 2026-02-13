package com.example.tourbooking;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TourDetailActivity extends AppCompatActivity {

    private Tour tour;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText participantsInput;
    private ImageView tourImage;
    private TextView tourTitle, tourRoute, tourPrice, tourDescription, tourPlaces;
    private Button bookButton, backButton;
    private TextView tourDates, tourDuration;
    private Button selectDatesButton;
    private long selectedStartDate = 0;
    private long selectedEndDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Получаем тур из Intent
        tour = (Tour) getIntent().getSerializableExtra("tour");

        if (tour == null) {
            Toast.makeText(this, "Ошибка загрузки тура", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализация views
        tourImage = findViewById(R.id.tour_detail_image);
        tourTitle = findViewById(R.id.tour_detail_title);
        tourRoute = findViewById(R.id.tour_detail_route);
        tourPrice = findViewById(R.id.tour_detail_price);
        tourDescription = findViewById(R.id.tour_detail_description);
        tourPlaces = findViewById(R.id.tour_detail_places);
        bookButton = findViewById(R.id.book_button);
        backButton = findViewById(R.id.back_button);
        participantsInput = findViewById(R.id.participants_input);
        tourDates = findViewById(R.id.tour_dates);
        tourDuration = findViewById(R.id.tour_duration);
        selectDatesButton = findViewById(R.id.select_dates_button);

        // Заполняем данные
        displayTourData();

        displayTourDates();

        selectDatesButton.setOnClickListener(v -> showDatePickerDialog());


        // Кнопка бронирования
        bookButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Для бронирования нужно войти в систему", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверяем доступность мест
            if (tour.getAvailablePlaces() <= 0) {
                Toast.makeText(this, "К сожалению, мест больше нет", Toast.LENGTH_SHORT).show();
                return;
            }

            createBooking();
        });

        // Кнопка назад
        backButton.setOnClickListener(v -> finish());
    }
    private void displayTourDates() {
        if (tour != null) {
            // Даты самого тура
            if (tour.getStartDate() > 0 && tour.getEndDate() > 0) {
                tourDates.setText(tour.getFormattedDateRange());
            } else {
                tourDates.setText("Даты не указаны");
            }

            // Продолжительность
            if (tour.getDuration() > 0) {
                tourDuration.setText("Продолжительность: " + tour.getDuration() + " дней");
            }
        }
    }

    private void showDatePickerDialog() {
        // Получаем даты тура из базы
        long tourStartDate = tour.getStartDate();  // Дата начала тура (админская)
        long tourEndDate = tour.getEndDate();      // Дата окончания тура (админская)

        Calendar calendar = Calendar.getInstance();

        // Если у тура есть даты, используем их как ограничения
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        if (tourStartDate > 0 && tourEndDate > 0) {
            // Устанавливаем ограничения по датам тура
            minDate.setTimeInMillis(tourStartDate);
            maxDate.setTimeInMillis(tourEndDate);
        } else {
            // Если дат нет, используем текущий год
            minDate.add(Calendar.DAY_OF_MONTH, 1); // Завтра
            maxDate.add(Calendar.DAY_OF_MONTH, 365); // Через год
        }

        DatePickerDialog startDatePicker = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(selectedYear, selectedMonth, selectedDay);

                    // ПРОВЕРКА: не раньше даты начала тура
                    if (tourStartDate > 0 && startCal.getTimeInMillis() < tourStartDate) {
                        Toast.makeText(this, "Нельзя выбрать дату раньше " + formatDate(tourStartDate),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ПРОВЕРКА: не позже даты окончания тура
                    if (tourEndDate > 0 && startCal.getTimeInMillis() > tourEndDate) {
                        Toast.makeText(this, "Нельзя выбрать дату позже " + formatDate(tourEndDate),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedStartDate = startCal.getTimeInMillis();
                    showEndDatePickerDialog(startCal, tourStartDate, tourEndDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Устанавливаем ограничения для DatePicker
        if (tourStartDate > 0) {
            startDatePicker.getDatePicker().setMinDate(tourStartDate);
        }
        if (tourEndDate > 0) {
            startDatePicker.getDatePicker().setMaxDate(tourEndDate);
        }

        startDatePicker.show();
    }

    private void showEndDatePickerDialog(Calendar startDate, long tourStartDate, long tourEndDate) {
        Calendar minDate = (Calendar) startDate.clone();
        minDate.add(Calendar.DAY_OF_MONTH, 1); // Минимум 1 день после начала

        DatePickerDialog endDatePicker = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar endCal = Calendar.getInstance();
                    endCal.set(selectedYear, selectedMonth, selectedDay);

                    // ПРОВЕРКА: не позже даты окончания тура
                    if (tourEndDate > 0 && endCal.getTimeInMillis() > tourEndDate) {
                        Toast.makeText(this, "Нельзя выбрать дату позже " + formatDate(tourEndDate),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedEndDate = endCal.getTimeInMillis();

                    // Показываем выбранные даты
                    String dateRange = formatDate(selectedStartDate) + " - " + formatDate(selectedEndDate);
                    tourDates.setText("Вы выбрали: " + dateRange);

                    // Рассчитываем продолжительность
                    long diff = selectedEndDate - selectedStartDate;
                    int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1;
                    tourDuration.setText("Продолжительность: " + days + " дней");

                    Toast.makeText(this, "Выбраны даты: " + dateRange, Toast.LENGTH_SHORT).show();
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        );

        // Устанавливаем ограничения
        endDatePicker.getDatePicker().setMinDate(minDate.getTimeInMillis());

        if (tourEndDate > 0) {
            endDatePicker.getDatePicker().setMaxDate(tourEndDate);
        } else {
            Calendar maxDate = (Calendar) startDate.clone();
            maxDate.add(Calendar.DAY_OF_MONTH, 60); // Максимум 60 дней
            endDatePicker.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        }

        endDatePicker.show();
    }

    // Метод форматирования даты (добавь если нет)
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    private void displayTourData() {
        // Название
        tourTitle.setText(tour.getTitle() != null ? tour.getTitle() : "Без названия");

        // Маршрут
        String route = "Маршрут: ";
        if (tour.getFromCountry() != null && tour.getToCountry() != null) {
            route += tour.getFromCountry() + " → " + tour.getToCountry();
        } else if (tour.getToCountry() != null) {
            route += tour.getToCountry();
        } else if (tour.getFromCountry() != null) {
            route += tour.getFromCountry();
        }
        tourRoute.setText(route);

        // Цена с форматированием
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        String priceText = "Цена: " + format.format(tour.getPrice()) + " ₽";
        tourPrice.setText(priceText);

        // Описание
        tourDescription.setText(tour.getDescription() != null ? tour.getDescription() : "Нет описания");

        // Информация о местах
        updatePlacesInfo();

        // Загружаем изображение
        loadTourImage();
    }

    private void loadTourImage() {
        String imageUrl = tour.getImageUrl();

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "https://" + imageUrl;
                }

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(tourImage);
            } catch (Exception e) {
                tourImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            tourImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void updatePlacesInfo() {
        String placesInfo = "Доступно мест: " + tour.getAvailablePlaces() + " из " + tour.getTotalPlaces();
        tourPlaces.setText(placesInfo);

        // Цвет текста в зависимости от доступности мест
        if (tour.getAvailablePlaces() == 0) {
            tourPlaces.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            bookButton.setEnabled(false);
            bookButton.setText("Мест нет");
            bookButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
            participantsInput.setEnabled(false);
        } else if (tour.getAvailablePlaces() < 5) {
            tourPlaces.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            bookButton.setEnabled(true);
            bookButton.setText("Забронировать тур");
            bookButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
            participantsInput.setEnabled(true);
        } else {
            tourPlaces.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            bookButton.setEnabled(true);
            bookButton.setText("Забронировать тур");
            bookButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
            participantsInput.setEnabled(true);
        }
    }

    private void createBooking() {
        String participantsText = participantsInput.getText().toString().trim();

        if (participantsText.isEmpty()) {
            Toast.makeText(this, "Введите количество участников", Toast.LENGTH_SHORT).show();
            return;
        }

        int participants;
        try {
            participants = Integer.parseInt(participantsText);

            // Проверка минимального количества
            if (participants <= 0) {
                Toast.makeText(this, "Количество участников должно быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверяем, достаточно ли мест
            if (participants > tour.getAvailablePlaces()) {
                Toast.makeText(this, "Недостаточно мест. Доступно: " + tour.getAvailablePlaces(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStartDate == 0 || selectedEndDate == 0) {
            Toast.makeText(this, "Выберите даты поездки", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Рассчитываем общую стоимость
        double totalPrice = tour.getPrice() * participants;

        // Создаем бронирование
        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("tourId", tour.getId());
        booking.put("tourTitle", tour.getTitle());
        booking.put("tourRoute", tour.getFromCountry() + " → " + tour.getToCountry());
        booking.put("participants", participants);
        booking.put("status", "confirmed");
        booking.put("totalPrice", totalPrice);
        booking.put("createdAt", System.currentTimeMillis());

        // Формируем маршрут для бронирования
        String route = "";
        if (tour.getFromCountry() != null && tour.getToCountry() != null) {
            route = tour.getFromCountry() + " → " + tour.getToCountry();
        } else if (tour.getToCountry() != null) {
            route = tour.getToCountry();
        }
        booking.put("tourRoute", route);

        booking.put("participants", participants);
        booking.put("status", "confirmed"); // СРАЗУ подтверждено
        booking.put("totalPrice", totalPrice);
        booking.put("createdAt", System.currentTimeMillis());
        booking.put("selectedStartDate", selectedStartDate);
        booking.put("selectedEndDate", selectedEndDate);
        booking.put("selectedDuration", (int) ((selectedEndDate - selectedStartDate) / (1000 * 60 * 60 * 24)) + 1);

        // Сначала уменьшаем количество доступных мест в туре
        db.collection("tours")
                .document(tour.getId())
                .update("availablePlaces", FieldValue.increment(-participants))
                .addOnSuccessListener(aVoid -> {
                    // Затем создаем бронирование
                    db.collection("bookings")
                            .add(booking)
                            .addOnSuccessListener(documentReference -> {
                                // Обновляем локальные данные тура
                                tour.setAvailablePlaces(tour.getAvailablePlaces() - participants);

                                Toast.makeText(this,
                                        "Бронирование успешно оформлено!\n" +
                                                "Участников: " + participants + "\n" +
                                                "Общая стоимость: " + formatPrice(totalPrice) + " ₽",
                                        Toast.LENGTH_LONG).show();

                                // Обновляем отображаемое количество мест
                                updatePlacesInfo();

                                // Можно очистить поле ввода участников
                                participantsInput.setText("1");
                            })
                            .addOnFailureListener(e -> {
                                // Если не удалось создать бронирование, ВОЗВРАЩАЕМ места
                                db.collection("tours")
                                        .document(tour.getId())
                                        .update("availablePlaces", FieldValue.increment(participants))
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(this,
                                                    "Ошибка бронирования. Места возвращены.",
                                                    Toast.LENGTH_SHORT).show();
                                        });

                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка обновления мест: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String formatPrice(double price) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        return format.format(price);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран можно обновить данные тура
        if (tour != null && tour.getId() != null) {
            db.collection("tours")
                    .document(tour.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Tour updatedTour = documentSnapshot.toObject(Tour.class);
                            if (updatedTour != null) {
                                tour.setAvailablePlaces(updatedTour.getAvailablePlaces());
                                updatePlacesInfo();
                            }
                        }
                    });
        }
    }
}