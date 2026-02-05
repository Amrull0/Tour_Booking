package com.example.tourbooking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class AdminBookingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private AdminBookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.admin_bookings_recycler);
        emptyText = view.findViewById(R.id.empty_text);

        bookingList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Создаем адаптер с поддержкой отмены
        bookingAdapter = new AdminBookingAdapter(getContext(), bookingList, new AdminBookingAdapter.OnBookingActionListener() {
            @Override
            public void onCancelBooking(Booking booking) {
                showCancelConfirmationDialog(booking);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(bookingAdapter);

        loadAllBookings();
    }

    private void loadAllBookings() {
        db.collection("bookings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Booking booking = new Booking();
                            booking.setId(document.getId());
                            booking.setUserId(document.getString("userId"));
                            booking.setTourId(document.getString("tourId"));
                            booking.setStatus(document.getString("status"));

                            Object participants = document.get("participants");
                            if (participants instanceof Long) {
                                booking.setParticipants(((Long) participants).intValue());
                            } else if (participants instanceof Integer) {
                                booking.setParticipants((Integer) participants);
                            }

                            bookingList.add(booking);
                        }

                        bookingAdapter.notifyDataSetChanged();

                        if (bookingList.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void showCancelConfirmationDialog(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Отмена бронирования")
                .setMessage("Вы уверены, что хотите отменить это бронирование?")
                .setPositiveButton("Отменить", (dialog, which) -> cancelBooking(booking))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void cancelBooking(Booking booking) {
        // Обновляем статус бронирования на "cancelled"
        db.collection("bookings")
                .document(booking.getId())
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    // Возвращаем места в тур
                    if (booking.getTourId() != null && booking.getParticipants() > 0) {
                        returnPlacesToTour(booking.getTourId(), booking.getParticipants());
                    }

                    Toast.makeText(getContext(), "Бронирование отменено", Toast.LENGTH_SHORT).show();
                    loadAllBookings(); // Обновляем список
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка отмены: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void returnPlacesToTour(String tourId, int participants) {
        // Получаем текущее количество мест в туре
        db.collection("tours")
                .document(tourId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Tour tour = documentSnapshot.toObject(Tour.class);
                        if (tour != null) {
                            int currentPlaces = tour.getAvailablePlaces();
                            int newPlaces = currentPlaces + participants;

                            // Обновляем количество мест
                            db.collection("tours")
                                    .document(tourId)
                                    .update("availablePlaces", newPlaces)
                                    .addOnSuccessListener(aVoid -> {
                                        // Успешно
                                    });
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllBookings();
    }
}