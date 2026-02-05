package com.example.tourbooking;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Временно показываем "Бронирование..."
        holder.bookingId.setText("Бронирование...");

        // Загружаем название тура из Firebase
        loadTourTitle(booking.getTourId(), holder);

        holder.status.setText("Статус: " + getStatusText(booking.getStatus()));
        holder.participants.setText("Участники: " + booking.getParticipants());
    }

    private void loadTourTitle(String tourId, BookingViewHolder holder) {
        if (tourId == null || tourId.isEmpty()) {
            holder.bookingId.setText("Бронирование");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tours")
                .document(tourId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Tour tour = task.getResult().toObject(Tour.class);
                        if (tour != null && tour.getTitle() != null) {
                            // Показываем "Бронирование тура 'Название'"
                            String title = tour.getTitle();
                            if (title.length() > 20) {
                                title = title.substring(0, 17) + "...";
                            }
                            holder.bookingId.setText("Тур: " + title);
                        } else {
                            holder.bookingId.setText("Бронирование");
                        }
                    } else {
                        holder.bookingId.setText("Бронирование");
                    }
                });
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Ожидание";
            case "confirmed": return "Подтверждено";
            case "cancelled": return "Отменено";
            default: return "Неизвестно";
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView bookingId, status, participants;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingId = itemView.findViewById(R.id.booking_id);
            status = itemView.findViewById(R.id.booking_status);
            participants = itemView.findViewById(R.id.booking_participants);
        }
    }
}