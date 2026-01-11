package T8.T1.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import java.util.*;
import T8.T1.exceptions.*;
import java.io.Serializable;
import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;

public class Room implements Comparable<Room>, Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    @ConfigProperty(propertyName = "room.default.id", type = PropertyType.LONG)
    private Long id;

    private int number;
    private String type;

    @ConfigProperty(propertyName = "room.default.price", type = PropertyType.DOUBLE)
    private double pricePerNight;

    @ConfigProperty(propertyName = "room.default.status", type = PropertyType.STRING)
    private RoomStatus status;

    @ConfigProperty(propertyName = "room.default.capacity", type = PropertyType.INTEGER)
    private int capacity;

    @ConfigProperty(propertyName = "room.default.stars", type = PropertyType.INTEGER)
    private int stars;

    private Guest currentGuest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<Service> additionalServices;
    private List<StayHistory> stayHistory;
    private List<RoomService> roomServices;

    @ConfigProperty(propertyName = "room.auto.generate.id", type = PropertyType.BOOLEAN)
    private static boolean autoGenerateId = true;

    @ConfigProperty(propertyName = "room.default.type", type = PropertyType.STRING)
    private static String defaultRoomType = "Стандарт";

    @ConfigProperty(propertyName = "room.max.additional.services", type = PropertyType.INTEGER)
    private static int maxAdditionalServices = 10;

    @ConfigProperty(propertyName = "room.validation.enabled", type = PropertyType.BOOLEAN)
    private static boolean validationEnabled = true;

    @ConfigProperty(propertyName = "room.max.history.size", type = PropertyType.INTEGER)
    private static int maxHistorySize = 100;

    @ConfigProperty(propertyName = "room.min.number", type = PropertyType.INTEGER)
    private static int minRoomNumber = 1;

    @ConfigProperty(propertyName = "room.max.number", type = PropertyType.INTEGER)
    private static int maxRoomNumber = 9999;

    @ConfigProperty(propertyName = "room.auto.clean.services.on.checkout", type = PropertyType.BOOLEAN)
    private static boolean autoCleanServicesOnCheckout = true;

    public Room() {
        this.additionalServices = new ArrayList<>();
        this.stayHistory = new ArrayList<>();
        this.roomServices = new ArrayList<>();
        this.status = RoomStatus.AVAILABLE;
    }

    public Room(int number, String type, double pricePerNight, int capacity, int stars) {
        this();
        setNumber(number);
        setType(type != null ? type : defaultRoomType);
        setPricePerNight(pricePerNight);
        setCapacity(capacity);
        setStars(stars);
    }

    public Room(Long id, int number, String type, double pricePerNight, int capacity, int stars) {
        this(number, type, pricePerNight, capacity, stars);
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) {
        if (autoGenerateId && id == null) {
            this.id = generateAutoId();
        } else {
            this.id = id;
        }
    }

    private Long generateAutoId() {
        return System.currentTimeMillis() % 1000000;
    }

    public int getNumber() { return number; }
    public void setNumber(int number) {
        if (validationEnabled) {
            if (number < minRoomNumber || number > maxRoomNumber) {
                throw new IllegalArgumentException("Номер комнаты должен быть от " +
                        minRoomNumber + " до " + maxRoomNumber);
            }
        }
        this.number = number;
    }

    public String getType() { return type; }
    public void setType(String type) {
        if (validationEnabled && (type == null || type.trim().isEmpty())) {
            throw new IllegalArgumentException("Тип номера не может быть пустым");
        }
        this.type = type;
    }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) {
        if (validationEnabled && pricePerNight < 0) {
            throw new IllegalArgumentException("Цена за ночь не может быть отрицательной");
        }
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) {
        if (validationEnabled && status == null) {
            throw new IllegalArgumentException("Статус не может быть null");
        }
        this.status = status;
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        if (validationEnabled && capacity <= 0) {
            throw new IllegalArgumentException("Вместимость должна быть положительным числом");
        }
        this.capacity = capacity;
    }

    public int getStars() { return stars; }
    public void setStars(int stars) {
        if (validationEnabled && (stars < 1 || stars > 5)) {
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
        if (validationEnabled && additionalServices.size() > maxAdditionalServices) {
            throw new IllegalArgumentException("Превышено максимальное количество дополнительных услуг: " + maxAdditionalServices);
        }
        this.additionalServices = new ArrayList<>(additionalServices);
    }

    public List<StayHistory> getStayHistory() { return Collections.unmodifiableList(stayHistory); }
    public void setStayHistory(List<StayHistory> stayHistory) {
        if (validationEnabled && stayHistory.size() > maxHistorySize) {
            throw new IllegalArgumentException("Превышен максимальный размер истории: " + maxHistorySize);
        }
        this.stayHistory = new ArrayList<>(stayHistory);
    }

    public List<RoomService> getRoomServices() { return Collections.unmodifiableList(roomServices); }
    public void setRoomServices(List<RoomService> roomServices) {
        this.roomServices = new ArrayList<>(roomServices);
    }

    public boolean setStatus(String newStatus) {
        if (validationEnabled && (newStatus == null || newStatus.trim().isEmpty())) {
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

    private RoomStatus safeConvertToRoomStatus(String statusDescription) {
        for (RoomStatus status : RoomStatus.values()) {
            if (status.getDescription().equals(statusDescription)) {
                return status;
            }
        }
        return RoomStatus.AVAILABLE;
    }

    public boolean checkIn(Guest guest, LocalDate checkInDate, LocalDate checkOutDate) {
        if (validationEnabled) {
            if (guest == null) {
                throw new IllegalArgumentException("Гость не может быть null");
            }
            if (checkInDate == null || checkOutDate == null) {
                throw new IllegalArgumentException("Даты заселения и выезда не могут быть null");
            }
            if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
                throw new IllegalArgumentException("Дата выезда должна быть после даты заселения");
            }
            if (checkInDate.isAfter(LocalDate.now().plusDays(365))) {
                throw new IllegalArgumentException("Бронирование более чем на год вперед не допускается");
            }
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

            // Ограничиваем размер истории
            if (stayHistory.size() > maxHistorySize) {
                stayHistory = stayHistory.subList(stayHistory.size() - maxHistorySize, stayHistory.size());
            }

            this.currentGuest = null;
            this.checkInDate = null;
            this.checkOutDate = null;
            this.status = RoomStatus.AVAILABLE;

            if (autoCleanServicesOnCheckout) {
                this.roomServices.clear();
            }
            return true;
        }
        return false;
    }

    public void addService(Service service) {
        if (validationEnabled) {
            if (service == null) {
                throw new IllegalArgumentException("Услуга не может быть null");
            }
            if (additionalServices.size() >= maxAdditionalServices) {
                throw new IllegalArgumentException("Превышено максимальное количество дополнительных услуг: " + maxAdditionalServices);
            }
        }
        if (!additionalServices.contains(service)) {
            additionalServices.add(service);
        }
    }

    public void addRoomService(Service service, LocalDate date) {
        if (validationEnabled) {
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
        }
        roomServices.add(new RoomService(service, date, service.getPrice()));
    }

    public double calculateTotalPrice(int nights) {
        if (validationEnabled && nights <= 0) {
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

    public List<StayHistory> getLastGuests(int count) {
        if (validationEnabled && count <= 0) {
            throw new IllegalArgumentException("Количество должно быть положительным числом");
        }
        int size = stayHistory.size();
        int start = Math.max(0, size - count);
        return stayHistory.stream()
                .skip(start)
                .collect(Collectors.toList());
    }

    public List<StayHistory> getLastThreeGuests() {
        return getLastGuests(3);
    }

    public boolean isAvailableOnDate(LocalDate date) {
        if (validationEnabled && date == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
        if (status != RoomStatus.OCCUPIED) return true;
        return date.isBefore(checkInDate) || date.isAfter(checkOutDate);
    }

    public boolean hasService(String serviceName) {
        if (validationEnabled && (serviceName == null || serviceName.trim().isEmpty())) {
            throw new IllegalArgumentException("Название услуги не может быть пустым");
        }
        return additionalServices.stream()
                .anyMatch(service -> service.getName().equalsIgnoreCase(serviceName));
    }

    public List<RoomService> getRoomServicesByDate(LocalDate date) {
        if (validationEnabled && date == null) {
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
            sb.append(String.format("Осталось ночей: %d\n",
                    java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), checkOutDate)));
        } else {
            sb.append("Текущий гость: нет\n");
        }

        sb.append(String.format("Дополнительные услуги (%d):\n", additionalServices.size()));
        if (!additionalServices.isEmpty()) {
            additionalServices.forEach(service ->
                    sb.append(String.format("  - %s: %.2f руб.\n",
                            service.getName(), service.getPrice())));
        }

        sb.append(String.format("История проживаний (%d):\n", stayHistory.size()));
        if (!stayHistory.isEmpty()) {
            stayHistory.stream()
                    .limit(5)
                    .forEach(history ->
                            sb.append(String.format("  - %s: %s - %s\n",
                                    history.getGuest().getName(),
                                    history.getCheckInDate(),
                                    history.getCheckOutDate())));
        }

        sb.append(String.format("Общая стоимость всех услуг: %.2f руб.", getTotalServicesPrice()));

        return sb.toString();
    }

    // Статические геттеры для конфигурации
    public static boolean isAutoGenerateId() { return autoGenerateId; }
    public static void setAutoGenerateId(boolean autoGenerateId) {
        Room.autoGenerateId = autoGenerateId;
    }

    public static String getDefaultRoomType() { return defaultRoomType; }
    public static void setDefaultRoomType(String defaultRoomType) {
        Room.defaultRoomType = defaultRoomType;
    }

    public static int getMaxAdditionalServices() { return maxAdditionalServices; }
    public static void setMaxAdditionalServices(int maxAdditionalServices) {
        Room.maxAdditionalServices = maxAdditionalServices;
    }

    public static boolean isValidationEnabled() { return validationEnabled; }
    public static void setValidationEnabled(boolean validationEnabled) {
        Room.validationEnabled = validationEnabled;
    }

    public static int getMaxHistorySize() { return maxHistorySize; }
    public static void setMaxHistorySize(int maxHistorySize) {
        Room.maxHistorySize = maxHistorySize;
    }

    public static boolean isAutoCleanServicesOnCheckout() { return autoCleanServicesOnCheckout; }
    public static void setAutoCleanServicesOnCheckout(boolean autoCleanServicesOnCheckout) {
        Room.autoCleanServicesOnCheckout = autoCleanServicesOnCheckout;
    }
}