package com.example.tourbooking;

import java.io.Serializable;

public class Tour implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private String fromCountry;    // ОТКУДА
    private String toCountry;      // КУДА
    private double price;
    private String imageUrl;
    private boolean active;
    private int totalPlaces;       // ОБЩЕЕ КОЛИЧЕСТВО МЕСТ
    private int availablePlaces;   // СВОБОДНЫЕ МЕСТА

    // Конструкторы
    public Tour() {}

    public Tour(String title, String description, String fromCountry, String toCountry, double price, int totalPlaces) {
        this.title = title;
        this.description = description;
        this.fromCountry = fromCountry;
        this.toCountry = toCountry;
        this.price = price;
        this.totalPlaces = totalPlaces;
        this.availablePlaces = totalPlaces; // изначально все места свободны
        this.active = true;
    }

    // Геттеры и сеттеры (ВСЕ ДОЛЖНЫ БЫТЬ!)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFromCountry() { return fromCountry; }
    public void setFromCountry(String fromCountry) { this.fromCountry = fromCountry; }

    public String getToCountry() { return toCountry; }
    public void setToCountry(String toCountry) { this.toCountry = toCountry; }

    // Для обратной совместимости с полем "country" в БД
    public String getCountry() { return toCountry; }
    public void setCountry(String country) { this.toCountry = country; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getTotalPlaces() { return totalPlaces; }
    public void setTotalPlaces(int totalPlaces) { this.totalPlaces = totalPlaces; }

    public int getAvailablePlaces() { return availablePlaces; }
    public void setAvailablePlaces(int availablePlaces) { this.availablePlaces = availablePlaces; }

    // Метод для проверки доступности мест
    public boolean hasAvailablePlaces(int requiredPlaces) {
        return availablePlaces >= requiredPlaces;
    }

    // Метод для бронирования мест
    public void bookPlaces(int places) {
        if (hasAvailablePlaces(places)) {
            availablePlaces -= places;
        }
    }

    // Метод для освобождения мест (при отмене)
    public void freePlaces(int places) {
        availablePlaces += places;
        if (availablePlaces > totalPlaces) {
            availablePlaces = totalPlaces;
        }
    }
}