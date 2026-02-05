package com.example.tourbooking;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private Context context;
    private List<Tour> tourList;
    private OnItemClickListener listener;
    private OnAdminActionListener adminActionListener;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isAdminMode = false;

    // Интерфейсы для разных действий
    public interface OnItemClickListener {
        void onItemClick(Tour tour);
    }

    public interface OnAdminActionListener {
        void onEditTour(Tour tour);
        void onDeleteTour(Tour tour);
    }

    // Конструктор для пользовательского режима
    public TourAdapter(Context context, List<Tour> tourList) {
        this(context, tourList, false);
    }

    // Конструктор с выбором режима
    public TourAdapter(Context context, List<Tour> tourList, boolean isAdminMode) {
        this.context = context;
        this.tourList = tourList;
        this.isAdminMode = isAdminMode;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    // Сеттеры для слушателей
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnAdminActionListener(OnAdminActionListener listener) {
        this.adminActionListener = listener;
    }

    public void setAdminMode(boolean adminMode) {
        isAdminMode = adminMode;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Выбираем макет в зависимости от режима
        View view;
        if (isAdminMode) {
            view = LayoutInflater.from(context).inflate(R.layout.item_tour_admin, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_tour, parent, false);
        }
        return new TourViewHolder(view, isAdminMode);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tourList.get(position);

        // Безопасно устанавливаем заголовок
        if (holder.title != null) {
            holder.title.setText(tour.getTitle() != null ? tour.getTitle() : "Без названия");
        }

        // Настраиваем маршрут для разных режимов
        if (isAdminMode) {
            // АДМИНСКИЙ РЕЖИМ - показываем полный маршрут
            if (holder.country != null) {
                String from = tour.getFromCountry() != null ? tour.getFromCountry() : "";
                String to = tour.getToCountry() != null ? tour.getToCountry() : "";
                String route = from + " → " + to;
                holder.country.setText(route);
            }

            // Показываем информацию о местах в админском режиме
            if (holder.places != null) {
                String placesInfo = "Места: " + tour.getAvailablePlaces() + "/" + tour.getTotalPlaces();
                holder.places.setText(placesInfo);

                // Цвет текста мест
                if (tour.getAvailablePlaces() == 0) {
                    holder.places.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                } else if (tour.getAvailablePlaces() < 5) {
                    holder.places.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    holder.places.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                }
            }
        } else {
            // ПОЛЬЗОВАТЕЛЬСКИЙ РЕЖИМ - показываем только страну назначения
            if (holder.country != null) {
                holder.country.setText(tour.getToCountry() != null ? tour.getToCountry() : "");
            }

            // Показываем информацию о местах в пользовательском режиме
            if (holder.places != null) {
                String placesInfo = tour.getAvailablePlaces() + "/" + tour.getTotalPlaces() + " мест";
                holder.places.setText(placesInfo);

                // Цвет текста мест
                if (tour.getAvailablePlaces() == 0) {
                    holder.places.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                } else if (tour.getAvailablePlaces() < 5) {
                    holder.places.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    holder.places.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                }
            }
        }

        // Устанавливаем цену
        if (holder.price != null) {
            String priceText = String.format("%.0f ₽", tour.getPrice());
            holder.price.setText(priceText);
        }

        // Загружаем изображение
        if (holder.image != null) {
            loadTourImage(tour, holder.image);
        }

        // Настраиваем взаимодействие в зависимости от режима
        if (!isAdminMode) {
            // РЕЖИМ ПОЛЬЗОВАТЕЛЯ
            // Проверяем, есть ли тур в избранном
            if (holder.favoriteIcon != null) {
                checkIfFavorite(tour.getId(), holder.favoriteIcon);
            }

            // Клик по карточке (открытие деталей)
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(tour);
                }
            });

            // Клик по иконке избранного
            if (holder.favoriteIcon != null) {
                holder.favoriteIcon.setOnClickListener(v -> {
                    toggleFavorite(tour, holder.favoriteIcon);
                });
            }
        } else {
            // АДМИНСКИЙ РЕЖИМ
            // Скрываем иконку избранного (если есть)
            if (holder.favoriteIcon != null) {
                holder.favoriteIcon.setVisibility(View.GONE);
            }

            // Настраиваем кнопки редактирования и удаления
            if (holder.editButton != null && holder.deleteButton != null) {
                holder.editButton.setOnClickListener(v -> {
                    if (adminActionListener != null) {
                        adminActionListener.onEditTour(tour);
                    }
                });

                holder.deleteButton.setOnClickListener(v -> {
                    if (adminActionListener != null) {
                        adminActionListener.onDeleteTour(tour);
                    }
                });
            }

            // Клик по карточке тоже открывает детали (для админа)
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(tour);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return tourList != null ? tourList.size() : 0;
    }

    // МЕТОД ДЛЯ ЗАГРУЗКИ ИЗОБРАЖЕНИЯ
    private void loadTourImage(Tour tour, ImageView imageView) {
        if (tour == null || imageView == null) return;

        String imageUrl = tour.getImageUrl();

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                // Проверяем, является ли URL валидным
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "https://" + imageUrl;
                }

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(imageView);
            } catch (Exception e) {
                Log.e("IMAGE_LOAD", "Ошибка загрузки: " + e.getMessage());
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            // Если URL нет, используем стандартную иконку
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // Избранное (только для пользовательского режима)
    private void checkIfFavorite(String tourId, ImageView favoriteIcon) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || tourId == null) {
            if (favoriteIcon != null) {
                favoriteIcon.setImageResource(android.R.drawable.star_big_off);
                favoriteIcon.setTag("not_favorite");
            }
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .whereEqualTo("tourId", tourId)
                .get()
                .addOnCompleteListener(task -> {
                    if (favoriteIcon == null) return;

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        favoriteIcon.setImageResource(android.R.drawable.star_big_on);
                        favoriteIcon.setTag("favorite");
                    } else {
                        favoriteIcon.setImageResource(android.R.drawable.star_big_off);
                        favoriteIcon.setTag("not_favorite");
                    }
                });
    }

    private void toggleFavorite(Tour tour, ImageView favoriteIcon) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Для добавления в избранное нужно войти",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String tourId = tour.getId();
        if (tourId == null) {
            Toast.makeText(context, "Ошибка: тур не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentTag = (String) favoriteIcon.getTag();

        if ("favorite".equals(currentTag)) {
            removeFromFavorites(userId, tourId, favoriteIcon);
        } else {
            addToFavorites(userId, tourId, favoriteIcon);
        }
    }

    private void addToFavorites(String userId, String tourId, ImageView favoriteIcon) {
        HashMap<String, Object> favorite = new HashMap<>();
        favorite.put("tourId", tourId);
        favorite.put("addedAt", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .add(favorite)
                .addOnSuccessListener(aVoid -> {
                    if (favoriteIcon != null) {
                        favoriteIcon.setImageResource(android.R.drawable.star_big_on);
                        favoriteIcon.setTag("favorite");
                        Toast.makeText(context, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFromFavorites(String userId, String tourId, ImageView favoriteIcon) {
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .whereEqualTo("tourId", tourId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String favoriteDocId = task.getResult().getDocuments().get(0).getId();

                        db.collection("users")
                                .document(userId)
                                .collection("favorites")
                                .document(favoriteDocId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (favoriteIcon != null) {
                                        favoriteIcon.setImageResource(android.R.drawable.star_big_off);
                                        favoriteIcon.setTag("not_favorite");
                                        Toast.makeText(context, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // Обновление данных
    public void updateList(List<Tour> newList) {
        if (newList != null) {
            tourList.clear();
            tourList.addAll(newList);
            notifyDataSetChanged();
        }
    }

    // ViewHolder с поддержкой обоих режимов
    static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView image, favoriteIcon;
        TextView title, country, price, places;
        ImageButton editButton, deleteButton;

        public TourViewHolder(@NonNull View itemView, boolean isAdminMode) {
            super(itemView);

            if (isAdminMode) {
                // АДМИНСКИЙ РЕЖИМ (item_tour_admin.xml)
                image = itemView.findViewById(R.id.tour_image);
                title = itemView.findViewById(R.id.tour_title);

                // В админском режиме используем tour_route для маршрута

                // Если нет tour_route, пробуем tour_country
                if (country == null) {
                    country = itemView.findViewById(R.id.tour_country);
                }

                price = itemView.findViewById(R.id.tour_price);
                places = itemView.findViewById(R.id.tour_places);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
                favoriteIcon = null; // В админском режиме нет иконки избранного
            } else {
                // ПОЛЬЗОВАТЕЛЬСКИЙ РЕЖИМ (item_tour.xml)
                image = itemView.findViewById(R.id.tour_image);
                favoriteIcon = itemView.findViewById(R.id.favorite_icon);
                title = itemView.findViewById(R.id.tour_title);
                country = itemView.findViewById(R.id.tour_country);
                price = itemView.findViewById(R.id.tour_price);
                places = itemView.findViewById(R.id.tour_places);
                editButton = null;
                deleteButton = null;
            }
        }
    }
}