package Task_6.T2.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import Task_6.T2.exceptions.*;

public class Room implements Comparable<Room>, Identifiable {
    private Long id;
    private int number;
    private String type;
    private double pricePerNight;
    private RoomStatus status;
    private int capacity;
    private int stars;
    private Guest currentGuest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<Service> additionalServices;
    private List<StayHistory> stayHistory;
    private List<RoomService> roomServices;

    public Room() {
        this.additionalServices = new ArrayList<>();
        this.stayHistory = new ArrayList<>();
        this.roomServices = new ArrayList<>();
        this.status = RoomStatus.AVAILABLE;
    }

    public Room(int number, String type, double pricePerNight, int capacity, int stars) {
        this();
        setNumber(number);
        setType(type);
        setPricePerNight(pricePerNight);
        setCapacity(capacity);
        setStars(stars);
    }

    public Room(Long id, int number, String type, double pricePerNight, int capacity, int stars) {
        this(number, type, pricePerNight, capacity, stars);
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Номер комнаты должен быть положительным числом");
        }
        this.number = number;
    }

    public String getType() { return type; }
    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Тип номера не может быть пустым");
        }
        this.type = type;
    }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight < 0) {
            throw new IllegalArgumentException("Цена за ночь не может быть отрицательной");
        }
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Статус не может быть null");
        }
        this.status = status;
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Вместимость должна быть положительным числом");
        }
        this.capacity = capacity;
    }

    public int getStars() { return stars; }
    public void setStars(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Количество звезд должно быть от 1 до 5");
        }
        this.stars = stars;
    }

    public Guest getCurrentGuest() { return currentGuest; }
    public void setCurrentGuest(Guest currentGuest) { this.currentGuest = currentGuest; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public List<Service> getAdditionalServices() { return Collections.unmodifiableList(additionalServices); }
    public void setAdditionalServices(List<Service> additionalServices) {
        this.additionalServices = new ArrayList<>(additionalServices);
    }

    public List<StayHistory> getStayHistory() { return Collections.unmodifiableList(stayHistory); }
    public void setStayHistory(List<StayHistory> stayHistory) {
        this.stayHistory = new ArrayList<>(stayHistory);
    }

    public List<RoomService> getRoomServices() { return Collections.unmodifiableList(roomServices); }
    public void setRoomServices(List<RoomService> roomServices) {
        this.roomServices = new ArrayList<>(roomServices);
    }

    public boolean setStatus(String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Статус не может быть пустым");
        }

        if (RoomStatus.isValidStatus(newStatus)) {
            this.status = safeConvertToRoomStatus(newStatus);
            if (this.status != RoomStatus.OCCUPIED) {
                this.currentGuest = null;
                this.checkInDate = null;
                this.checkOutDate = null;
            }
            return true;
        }
        return false;
    }

    // Безопасный метод конвертации статуса
    private RoomStatus safeConvertToRoomStatus(String statusDescription) {
        for (RoomStatus status : RoomStatus.values()) {
            if (status.getDescription().equals(statusDescription)) {
                return status;
            }
        }
        return RoomStatus.AVAILABLE; // Возвращаем значение по умолчанию
    }

    public boolean checkIn(Guest guest, LocalDate checkInDate, LocalDate checkOutDate) {
        if (guest == null) {
            throw new IllegalArgumentException("Гость не может быть null");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Даты заселения и выезда не могут быть null");
        }
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new IllegalArgumentException("Дата выезда должна быть после даты заселения");
        }

        if (status == RoomStatus.AVAILABLE) {
            this.currentGuest = guest;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.status = RoomStatus.OCCUPIED;
            return true;
        }
        return false;
    }

    public boolean checkOut() {
        if (status == RoomStatus.OCCUPIED && currentGuest != null) {
            stayHistory.add(new StayHistory(currentGuest, checkInDate, checkOutDate));
            this.currentGuest = null;
            this.checkInDate = null;
            this.checkOutDate = null;
            this.status = RoomStatus.AVAILABLE;
            this.roomServices.clear();
            return true;
        }
        return false;
    }

    public void addService(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("Услуга не может быть null");
        }
        if (!additionalServices.contains(service)) {
            additionalServices.add(service);
        }
    }

    public void addRoomService(Service service, LocalDate date) {
        if (service == null) {
            throw new IllegalArgumentException("Услуга не может быть null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Нельзя добавить услугу на будущую дату");
        }
        if (this.status != RoomStatus.OCCUPIED) {
            throw new IllegalStateException("Нельзя добавить услугу к незанятому номеру");
        }
        roomServices.add(new RoomService(service, date, service.getPrice()));
    }

    public double calculateTotalPrice(int nights) {
        if (nights <= 0) {
            throw new IllegalArgumentException("Количество ночей должно быть положительным числом");
        }

        double basePrice = pricePerNight * nights;
        double additionalServicesPrice = additionalServices.stream()
                .mapToDouble(Service::getPrice)
                .sum();
        double roomServicesPrice = roomServices.stream()
                .mapToDouble(RoomService::getPrice)
                .sum();
        return basePrice + additionalServicesPrice + roomServicesPrice;
    }

    public List<StayHistory> getLastThreeGuests() {
        int size = stayHistory.size();
        return stayHistory.stream()
                .skip(Math.max(0, size - 3))
                .collect(Collectors.toList());
    }

    public boolean isAvailableOnDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
        if (status != RoomStatus.OCCUPIED) return true;
        return date.isBefore(checkInDate) || date.isAfter(checkOutDate);
    }

    public boolean hasService(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Название услуги не может быть пустым");
        }
        return additionalServices.stream()
                .anyMatch(service -> service.getName().equalsIgnoreCase(serviceName));
    }

    public List<RoomService> getRoomServicesByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
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

    public List<Guest> getAllPreviousGuests() {
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
        return String.format("Room{id=%d, number=%d, type='%s', price=%.2f, capacity=%d, stars=%d, status='%s'}",
                id, number, type, pricePerNight, capacity, stars, status.getDescription());
    }

    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "Детали номера %d:\n" +
                        "ID: %d\n" +
                        "Тип: %s\n" +
                        "Цена за ночь: %.2f руб.\n" +
                        "Вместимость: %d чел.\n" +
                        "Звезд: %d\n" +
                        "Статус: %s\n",
                number, id, type, pricePerNight, capacity, stars, status.getDescription()));

        if (currentGuest != null) {
            sb.append(String.format("Текущий гость: %s (паспорт: %s)\n",
                    currentGuest.getName(), currentGuest.getPassportNumber()));
            sb.append(String.format("Дата заселения: %s\n", checkInDate));
            sb.append(String.format("Дата выезда: %s\n", checkOutDate));
        } else {
            sb.append("Текущий гость: нет\n");
        }

        sb.append(String.format("Дополнительные услуги: %d\n", additionalServices.size()));
        sb.append(String.format("История проживаний: %d", stayHistory.size()));

        return sb.toString();
    }
}