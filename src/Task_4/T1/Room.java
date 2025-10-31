package Task_4.T1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room implements Comparable<Room> {
    public static final String AVAILABLE = "Доступен";
    public static final String OCCUPIED = "Занят";
    public static final String UNDER_MAINTENANCE = "На ремонте";
    public static final String UNDER_SERVICE = "На обслуживании";

    private int number;
    private String type;
    private double pricePerNight;
    private String status;
    private int capacity;
    private int stars;
    private Guest currentGuest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<Service> additionalServices;
    private List<StayHistory> stayHistory;
    private List<RoomService> roomServices;

    public Room(int number, String type, double pricePerNight, int capacity, int stars) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.stars = stars;
        this.status = AVAILABLE;
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
    public Guest getCurrentGuest() { return currentGuest; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public List<Service> getAdditionalServices() { return new ArrayList<>(additionalServices); }
    public List<StayHistory> getStayHistory() { return new ArrayList<>(stayHistory); }
    public List<RoomService> getRoomServices() { return new ArrayList<>(roomServices); }

    // Сеттеры
    public void setPricePerNight(double price) { this.pricePerNight = price; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setStars(int stars) { this.stars = stars; }

    public boolean setStatus(String newStatus) {
        if (isValidStatus(newStatus)) {
            this.status = newStatus;
            if (!newStatus.equals(OCCUPIED)) {
                this.currentGuest = null;
            }
            return true;
        }
        return false;
    }

    private boolean isValidStatus(String status) {
        return status.equals(AVAILABLE) || status.equals(OCCUPIED) ||
                status.equals(UNDER_MAINTENANCE) || status.equals(UNDER_SERVICE);
    }

    public boolean checkIn(Guest guest, LocalDate checkInDate, LocalDate checkOutDate) {
        if (status.equals(AVAILABLE)) {
            this.currentGuest = guest;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.status = OCCUPIED;
            return true;
        }
        return false;
    }

    public boolean checkOut() {
        if (status.equals(OCCUPIED) && currentGuest != null) {
            stayHistory.add(new StayHistory(currentGuest, checkInDate, checkOutDate));
            this.currentGuest = null;
            this.checkInDate = null;
            this.checkOutDate = null;
            this.status = AVAILABLE;
            this.roomServices.clear();
            return true;
        }
        return false;
    }

    public void addService(Service service) {
        if (service != null) {
            additionalServices.add(service);
        }
    }

    public void addRoomService(Service service, LocalDate date) {
        if (service != null && date != null) {
            roomServices.add(new RoomService(service, date, service.getPrice()));
        }
    }

    public double calculateTotalPrice(int nights) {
        double total = pricePerNight * nights;
        for (Service service : additionalServices) {
            total += service.getPrice();
        }
        for (RoomService roomService : roomServices) {
            total += roomService.getPrice();
        }
        return total;
    }

    public List<StayHistory> getLastThreeGuests() {
        int size = stayHistory.size();
        return stayHistory.subList(Math.max(0, size - 3), size);
    }

    public boolean isAvailableOnDate(LocalDate date) {
        if (!status.equals(OCCUPIED)) return true;
        return date.isBefore(checkInDate) || date.isAfter(checkOutDate);
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
        return "Room{number=" + number + ", type='" + type + "', price=" + pricePerNight +
                ", capacity=" + capacity + ", stars=" + stars + ", status='" + status + "'}";
    }

    public String getDetailedInfo() {
        return "Детали номера " + number + ":\n" +
                "Тип: " + type + "\n" +
                "Цена за ночь: " + pricePerNight + " руб.\n" +
                "Вместимость: " + capacity + " чел.\n" +
                "Звезд: " + stars + "\n" +
                "Статус: " + status + "\n" +
                "Текущий гость: " + (currentGuest != null ? currentGuest.getName() : "нет") + "\n" +
                "Дополнительные услуги: " + additionalServices.size() + "\n" +
                "История проживаний: " + stayHistory.size();
    }
}