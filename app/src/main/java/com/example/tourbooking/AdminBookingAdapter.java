package com.example.tourbooking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private OnBookingActionListener listener;
    private FirebaseFirestore db;

    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking);
    }

    public AdminBookingAdapter(Context context, List<Booking> bookingList, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_admin, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Отображаем информацию о бронировании
        holder.bookingInfo.setText("Бронирование #" + (booking.getId() != null && booking.getId().length() > 8 ?
                booking.getId().substring(0, 8) : booking.getId()));
        holder.bookingStatus.setText("Статус: " + getStatusText(booking.getStatus()));
        holder.bookingParticipants.setText("Участники: " + booking.getParticipants());

        // Загружаем название тура
        loadTourInfo(booking.getTourId(), holder);

        // Настраиваем кнопку отмены
        if (booking.getStatus() != null && !booking.getStatus().equals("cancelled")) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelBooking(booking);
                }
            });
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }
    }

    private void loadTourInfo(String tourId, BookingViewHolder holder) {
        if (tourId == null || tourId.isEmpty()) return;

        db.collection("tours")
                .document(tourId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Tour tour = task.getResult().toObject(Tour.class);
                        if (tour != null && tour.getTitle() != null) {
                            holder.bookingInfo.setText("Тур: " + tour.getTitle());
                        }
                    }
                });
    }

    private String getStatusText(String status) {
        if (status == null) return "Неизвестно";
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
        TextView bookingInfo, bookingStatus, bookingParticipants;
        Button cancelButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingInfo = itemView.findViewById(R.id.booking_info);
            bookingStatus = itemView.findViewById(R.id.booking_status);
            bookingParticipants = itemView.findViewById(R.id.booking_participants);
            cancelButton = itemView.findViewById(R.id.cancel_button);
        }
    }
}