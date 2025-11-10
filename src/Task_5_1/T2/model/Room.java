package Task_5_1.T2.model;

import Task_5.T2.model.Guest;
import Task_5.T2.model.RoomService;
import Task_5.T2.model.RoomStatus;
import Task_5.T2.model.Service;
import Task_5.T2.model.StayHistory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.*;
import java.util.stream.Collectors;

public class Room implements Comparable<Room> {
    private int number;
    private String type;
    private double pricePerNight;
    private String status;
    private int capacity;
    private int stars;
    private Task_5.T2.model.Guest currentGuest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<Task_5.T2.model.Service> additionalServices;
    private List<Task_5.T2.model.StayHistory> stayHistory;
    private List<Task_5.T2.model.RoomService> roomServices;

    public Room(int number, String type, double pricePerNight, int capacity, int stars) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.stars = stars;
        this.status = Task_5.T2.model.RoomStatus.AVAILABLE.getDescription();
        this.additionalServices = new ArrayList<>();
        this.stayHistory = new ArrayList<>();
        this.roomServices = new ArrayList<>();
    }

    // Геттеры
    public int getNumber() { return number; }
    public String getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public String getStatus() { return status; }
    public int getCapacity() { return capacity; }
    public int getStars() { return stars; }
    public Task_5.T2.model.Guest getCurrentGuest() { return currentGuest; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public List<Task_5.T2.model.Service> getAdditionalServices() {
        return Collections.unmodifiableList(additionalServices);
    }
    public List<Task_5.T2.model.StayHistory> getStayHistory() {
        return Collections.unmodifiableList(stayHistory);
    }
    public List<Task_5.T2.model.RoomService> getRoomServices() {
        return Collections.unmodifiableList(roomServices);
    }

    // Сеттеры
    public void setPricePerNight(double price) { this.pricePerNight = price; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setStars(int stars) { this.stars = stars; }

    public boolean setStatus(String newStatus) {
        if (Task_5.T2.model.RoomStatus.isValidStatus(newStatus)) {
            this.status = newStatus;
            if (!newStatus.equals(Task_5.T2.model.RoomStatus.OCCUPIED.getDescription())) {
                this.currentGuest = null;
            }
            return true;
        }
        return false;
    }

    public boolean checkIn(Task_5.T2.model.Guest guest, LocalDate checkInDate, LocalDate checkOutDate) {
        if (status.equals(Task_5.T2.model.RoomStatus.AVAILABLE.getDescription())) {
            this.currentGuest = guest;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.status = Task_5.T2.model.RoomStatus.OCCUPIED.getDescription();
            return true;
        }
        return false;
    }

    public boolean checkOut() {
        if (status.equals(Task_5.T2.model.RoomStatus.OCCUPIED.getDescription()) && currentGuest != null) {
            stayHistory.add(new Task_5.T2.model.StayHistory(currentGuest, checkInDate, checkOutDate));
            this.currentGuest = null;
            this.checkInDate = null;
            this.checkOutDate = null;
            this.status = Task_5.T2.model.RoomStatus.AVAILABLE.getDescription();
            this.roomServices.clear();
            return true;
        }
        return false;
    }

    public void addService(Task_5.T2.model.Service service) {
        Optional.ofNullable(service)
                .filter(s -> !additionalServices.contains(s))
                .ifPresent(additionalServices::add);
    }

    public void addRoomService(Task_5.T2.model.Service service, LocalDate date) {
        Optional.ofNullable(service)
                .filter(s -> date != null)
                .ifPresent(s -> roomServices.add(new Task_5.T2.model.RoomService(s, date, s.getPrice())));
    }

    public double calculateTotalPrice(int nights) {
        double basePrice = pricePerNight * nights;

        double additionalServicesPrice = additionalServices.stream()
                .mapToDouble(Task_5.T2.model.Service::getPrice)
                .sum();

        double roomServicesPrice = roomServices.stream()
                .mapToDouble(Task_5.T2.model.RoomService::getPrice)
                .sum();

        return basePrice + additionalServicesPrice + roomServicesPrice;
    }

    public List<Task_5.T2.model.StayHistory> getLastThreeGuests() {
        int size = stayHistory.size();
        return stayHistory.stream()
                .skip(Math.max(0, size - 3))
                .collect(Collectors.toList());
    }

    public boolean isAvailableOnDate(LocalDate date) {
        if (!status.equals(RoomStatus.OCCUPIED.getDescription())) return true;
        return date.isBefore(checkInDate) || date.isAfter(checkOutDate);
    }

    // Новые методы с использованием Stream API
    public boolean hasService(String serviceName) {
        return additionalServices.stream()
                .anyMatch(service -> service.getName().equalsIgnoreCase(serviceName));
    }

    public List<Task_5.T2.model.RoomService> getRoomServicesByDate(LocalDate date) {
        return roomServices.stream()
                .filter(service -> service.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public double getTotalServicesPrice() {
        return additionalServices.stream()
                .mapToDouble(Service::getPrice)
                .sum() +
                roomServices.stream()
                        .mapToDouble(RoomService::getPrice)
                        .sum();
    }

    public List<Task_5.T2.model.Guest> getAllPreviousGuests() {
        return stayHistory.stream()
                .map(StayHistory::getGuest)
                .collect(Collectors.toList());
    }

    public long getTotalNightsOccupied() {
        return stayHistory.stream()
                .mapToLong(history ->
                        java.time.temporal.ChronoUnit.DAYS.between(
                                history.getCheckInDate(),
                                history.getCheckOutDate()
                        )
                )
                .sum();
    }

    @Override
    public int compareTo(Room other) {
        return Integer.compare(this.number, other.number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return number == room.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return String.format("Room{number=%d, type='%s', price=%.2f, capacity=%d, stars=%d, status='%s'}",
                number, type, pricePerNight, capacity, stars, status);
    }

    public String getDetailedInfo() {
        return String.format(
                "Детали номера %d:\n" +
                        "Тип: %s\n" +
                        "Цена за ночь: %.2f руб.\n" +
                        "Вместимость: %d чел.\n" +
                        "Звезд: %d\n" +
                        "Статус: %s\n" +
                        "Текущий гость: %s\n" +
                        "Дополнительные услуги: %d\n" +
                        "История проживаний: %d",
                number, type, pricePerNight, capacity, stars, status,
                Optional.ofNullable(currentGuest).map(Guest::getName).orElse("нет"),
                additionalServices.size(), stayHistory.size()
        );
    }
}