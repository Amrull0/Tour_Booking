package com.example.tourbooking;

import java.io.Serializable;

public class Booking implements Serializable {
    private static final long serialVersionUID = 2L;

    private String id;
    private String userId;
    private String tourId;
    private String status; // pending, confirmed, cancelled
    private int participants;
    private double totalPrice;
    private long createdAt;

    // ДОБАВИТЬ ЭТИ ПОЛЯ:
    private String tourTitle;
    private String tourRoute;

    // Конструкторы
    public Booking() {}

    public Booking(String userId, String tourId, int participants) {
        this.userId = userId;
        this.tourId = tourId;
        this.participants = participants;
        this.status = "pending";
        this.createdAt = System.currentTimeMillis();
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getParticipants() { return participants; }
    public void setParticipants(int participants) { this.participants = participants; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }

    public String getTourRoute() { return tourRoute; }
    public void setTourRoute(String tourRoute) { this.tourRoute = tourRoute; }
}