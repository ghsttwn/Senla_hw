package Task_5.T2.controller;

import Task_5.T2.model.*;
import Task_5.T2.model.comparators.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.*;


public class HotelController {
    private List<Room> rooms;
    private List<Service> services;
    private String hotelName;

    // Компараторы
    private final Comparator<Room> roomPriceComparator = Comparator.comparingDouble(Room::getPricePerNight);
    private final Comparator<Room> roomCapacityComparator = Comparator.comparingInt(Room::getCapacity);
    private final Comparator<Room> roomStarsComparator = Comparator.comparingInt(Room::getStars);
    private final Comparator<Service> servicePriceComparator = Comparator.comparingDouble(Service::getPrice);
    private final Comparator<Map.Entry<Guest, Room>> guestCheckOutDateComparator =
            Comparator.comparing(entry -> entry.getValue().getCheckOutDate());

    public HotelController(String hotelName) {
        this.hotelName = hotelName;
        this.rooms = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    public String getHotelName() { return hotelName; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
    public List<Service> getServices() { return Collections.unmodifiableList(services); }

    // Основные методы бизнес-логики с использованием Optional и Stream API
    public void addRoom(Room room) {
        Optional.ofNullable(room)
                .filter(r -> !rooms.contains(r))
                .ifPresent(rooms::add);
    }

    public boolean checkIn(int roomNumber, Guest guest, int nights) {
        return findRoom(roomNumber)
                .filter(room -> room.getStatus().equals(RoomStatus.AVAILABLE.getDescription()))
                .map(room -> {
                    LocalDate checkInDate = LocalDate.now();
                    LocalDate checkOutDate = checkInDate.plusDays(nights);
                    return room.checkIn(guest, checkInDate, checkOutDate);
                })
                .orElse(false);
    }

    public boolean checkOut(int roomNumber) {
        return findRoom(roomNumber)
                .filter(room -> room.getStatus().equals(RoomStatus.OCCUPIED.getDescription()))
                .map(Room::checkOut)
                .orElse(false);
    }

    public boolean setRoomStatus(int roomNumber, String status) {
        return findRoom(roomNumber)
                .filter(room -> RoomStatus.isValidStatus(status))
                .map(room -> room.setStatus(status))
                .orElse(false);
    }

    public boolean changeRoomPrice(int roomNumber, double newPrice) {
        return findRoom(roomNumber)
                .map(room -> {
                    room.setPricePerNight(newPrice);
                    return true;
                })
                .orElse(false);
    }

    public void addService(Service service) {
        Optional.ofNullable(service)
                .filter(s -> !services.contains(s))
                .ifPresent(services::add);
    }

    public boolean changeServicePrice(String serviceName, double newPrice) {
        return findService(serviceName)
                .map(service -> {
                    service.setPrice(newPrice);
                    return true;
                })
                .orElse(false);
    }

    public boolean addServiceToRoom(int roomNumber, String serviceName) {
        return findRoom(roomNumber)
                .flatMap(room -> findService(serviceName)
                        .map(service -> {
                            room.addService(service);
                            return true;
                        }))
                .orElse(false);
    }

    public boolean addServiceToGuest(int roomNumber, String serviceName) {
        return findRoom(roomNumber)
                .filter(room -> room.getCurrentGuest() != null)
                .flatMap(room -> findService(serviceName)
                        .map(service -> {
                            room.addRoomService(service, LocalDate.now());
                            return true;
                        }))
                .orElse(false);
    }

    // Методы для получения данных с использованием Stream API
    public List<Room> getRoomsSortedByPrice() {
        return rooms.stream()
                .sorted(roomPriceComparator)
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsSortedByCapacity() {
        return rooms.stream()
                .sorted(roomCapacityComparator)
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsSortedByStars() {
        return rooms.stream()
                .sorted(roomStarsComparator)
                .collect(Collectors.toList());
    }

    public List<Room> getAvailableRoomsSortedByPrice() {
        return rooms.stream()
                .filter(room -> room.getStatus().equals(RoomStatus.AVAILABLE.getDescription()))
                .sorted(roomPriceComparator)
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Guest, Room>> getGuestsWithRooms() {
        return rooms.stream()
                .filter(room -> room.getCurrentGuest() != null)
                .map(room -> Map.entry(room.getCurrentGuest(), room))
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Guest, Room>> getGuestsSortedByName() {
        return getGuestsWithRooms().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Guest, Room>> getGuestsSortedByCheckOutDate() {
        return getGuestsWithRooms().stream()
                .sorted(guestCheckOutDateComparator)
                .collect(Collectors.toList());
    }

    public List<Service> getServicesSortedByPrice() {
        return services.stream()
                .sorted(servicePriceComparator)
                .collect(Collectors.toList());
    }

    public long getTotalAvailableRooms() {
        return rooms.stream()
                .filter(room -> room.getStatus().equals(RoomStatus.AVAILABLE.getDescription()))
                .count();
    }

    public long getTotalGuests() {
        return rooms.stream()
                .filter(room -> room.getCurrentGuest() != null)
                .count();
    }

    public List<Room> getRoomsAvailableOnDate(LocalDate date) {
        return rooms.stream()
                .filter(room -> room.isAvailableOnDate(date))
                .sorted()
                .collect(Collectors.toList());
    }

    public double getRoomPayment(int roomNumber) {
        return findRoom(roomNumber)
                .filter(room -> room.getCurrentGuest() != null && room.getCheckInDate() != null)
                .map(room -> {
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(
                            room.getCheckInDate(), LocalDate.now());
                    return room.calculateTotalPrice((int) Math.max(1, nights));
                })
                .orElse(0.0);
    }

    public List<StayHistory> getLastThreeGuests(int roomNumber) {
        return findRoom(roomNumber)
                .map(Room::getLastThreeGuests)
                .orElse(Collections.emptyList());
    }

    public String getRoomDetails(int roomNumber) {
        return findRoom(roomNumber)
                .map(Room::getDetailedInfo)
                .orElse("Номер не найден");
    }

    public List<RoomService> getGuestServices(Guest guest) {
        return rooms.stream()
                .filter(room -> guest.equals(room.getCurrentGuest()))
                .flatMap(room -> room.getRoomServices().stream())
                .collect(Collectors.toList());
    }

    public Guest getGuestByRoomNumber(int roomNumber) {
        return findRoom(roomNumber)
                .map(Room::getCurrentGuest)
                .orElse(null);
    }

    public Room findRoomByNumber(int roomNumber) {
        return findRoom(roomNumber).orElse(null);
    }

    public Service findServiceByName(String serviceName) {
        return findService(serviceName).orElse(null);
    }

    // Вспомогательные методы с использованием Optional
    private Optional<Room> findRoom(int roomNumber) {
        return rooms.stream()
                .filter(room -> room.getNumber() == roomNumber)
                .findFirst();
    }

    private Optional<Service> findService(String serviceName) {
        return services.stream()
                .filter(service -> service.getName().equalsIgnoreCase(serviceName))
                .findFirst();
    }

    // Метод для поиска гостя по паспорту с использованием Stream API
    public Optional<Map.Entry<Guest, Room>> findGuestByPassport(String passport) {
        return rooms.stream()
                .filter(room -> room.getCurrentGuest() != null)
                .filter(room -> room.getCurrentGuest().getPassportNumber().equals(passport))
                .map(room -> Map.entry(room.getCurrentGuest(), room))
                .findFirst();
    }

    // Метод для получения всех услуг определенного типа
    public List<Service> getServicesByPriceRange(double minPrice, double maxPrice) {
        return services.stream()
                .filter(service -> service.getPrice() >= minPrice && service.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    // Метод для получения номеров по критериям с использованием Stream API
    public List<Room> getRoomsByCriteria(int minStars, int maxCapacity, double maxPrice) {
        return rooms.stream()
                .filter(room -> room.getStars() >= minStars)
                .filter(room -> room.getCapacity() <= maxCapacity)
                .filter(room -> room.getPricePerNight() <= maxPrice)
                .collect(Collectors.toList());
    }

    // Метод для статистики по типам номеров
    public Map<String, Long> getRoomTypeStatistics() {
        return rooms.stream()
                .collect(Collectors.groupingBy(
                        Room::getType,
                        Collectors.counting()
                ));
    }

    // Метод для получения самых популярных услуг
    public Map<String, Long> getPopularServices() {
        return rooms.stream()
                .flatMap(room -> room.getRoomServices().stream())
                .collect(Collectors.groupingBy(
                        roomService -> roomService.getService().getName(),
                        Collectors.counting()
                ));
    }
}