package com.example.tourbooking;

import android.content.Intent;
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

        // Заполняем данные
        displayTourData();

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

        String userId = mAuth.getCurrentUser().getUid();

        // Рассчитываем общую стоимость
        double totalPrice = tour.getPrice() * participants;

        // Создаем бронирование
        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("tourId", tour.getId());
        booking.put("tourTitle", tour.getTitle());

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