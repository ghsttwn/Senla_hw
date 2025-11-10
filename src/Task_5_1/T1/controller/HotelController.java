package Task_5_1.T1.controller;

import Task_5.T1.model.*;
import Task_5.T1.model.comparators.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class HotelController {
    private List<Room> rooms;
    private List<Service> services;
    private String hotelName;

    // Компараторы
    private final Comparator<Room> roomPriceComparator = new RoomPriceComparator();
    private final Comparator<Room> roomCapacityComparator = new RoomCapacityComparator();
    private final Comparator<Room> roomStarsComparator = new RoomStarsComparator();
    private final Comparator<Service> servicePriceComparator = new ServicePriceComparator();
    private final Comparator<Map.Entry<Guest, Room>> guestCheckOutDateComparator = new GuestCheckOutDateComparator();

    public HotelController(String hotelName) {
        this.hotelName = hotelName;
        this.rooms = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    public String getHotelName() { return hotelName; }
    public List<Room> getRooms() { return new ArrayList<>(rooms); }
    public List<Service> getServices() { return new ArrayList<>(services); }

    public void addRoom(Room room) {
        if (room != null && !rooms.contains(room)) {
            rooms.add(room);
        }
    }

    public boolean checkIn(int roomNumber, Guest guest, int nights) {
        Room room = findRoom(roomNumber);
        if (room != null) {
            LocalDate checkInDate = LocalDate.now();
            LocalDate checkOutDate = checkInDate.plusDays(nights);
            return room.checkIn(guest, checkInDate, checkOutDate);
        }
        return false;
    }

    public boolean checkOut(int roomNumber) {
        Room room = findRoom(roomNumber);
        return room != null && room.checkOut();
    }

    public boolean setRoomStatus(int roomNumber, String status) {
        Room room = findRoom(roomNumber);
        return room != null && room.setStatus(status);
    }

    public boolean changeRoomPrice(int roomNumber, double newPrice) {
        Room room = findRoom(roomNumber);
        if (room != null) {
            room.setPricePerNight(newPrice);
            return true;
        }
        return false;
    }

    public void addService(Service service) {
        if (service != null && !services.contains(service)) {
            services.add(service);
        }
    }

    public boolean changeServicePrice(String serviceName, double newPrice) {
        Service service = findService(serviceName);
        if (service != null) {
            service.setPrice(newPrice);
            return true;
        }
        return false;
    }

    public boolean addServiceToRoom(int roomNumber, String serviceName) {
        Room room = findRoom(roomNumber);
        Service service = findService(serviceName);
        if (room != null && service != null) {
            room.addService(service);
            return true;
        }
        return false;
    }

    public boolean addServiceToGuest(int roomNumber, String serviceName) {
        Room room = findRoom(roomNumber);
        Service service = findService(serviceName);
        if (room != null && service != null && room.getCurrentGuest() != null) {
            room.addRoomService(service, LocalDate.now());
            return true;
        }
        return false;
    }

    // Методы для получения данных
    public List<Room> getRoomsSortedByPrice() {
        return rooms.stream().sorted(roomPriceComparator).collect(Collectors.toList());
    }

    public List<Room> getRoomsSortedByCapacity() {
        return rooms.stream().sorted(roomCapacityComparator).collect(Collectors.toList());
    }

    public List<Room> getRoomsSortedByStars() {
        return rooms.stream().sorted(roomStarsComparator).collect(Collectors.toList());
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
        return services.stream().sorted(servicePriceComparator).collect(Collectors.toList());
    }

    public long getTotalAvailableRooms() {
        return rooms.stream()
                .filter(room -> room.getStatus().equals(RoomStatus.AVAILABLE.getDescription()))
                .count();
    }

    public long getTotalGuests() {
        return rooms.stream().filter(room -> room.getCurrentGuest() != null).count();
    }

    public List<Room> getRoomsAvailableOnDate(LocalDate date) {
        return rooms.stream()
                .filter(room -> room.isAvailableOnDate(date))
                .sorted()
                .collect(Collectors.toList());
    }

    public double getRoomPayment(int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room != null && room.getCurrentGuest() != null && room.getCheckInDate() != null) {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                    room.getCheckInDate(), LocalDate.now());
            return room.calculateTotalPrice((int) Math.max(1, nights));
        }
        return 0;
    }

    public List<StayHistory> getLastThreeGuests(int roomNumber) {
        Room room = findRoom(roomNumber);
        return room != null ? room.getLastThreeGuests() : new ArrayList<>();
    }

    public String getRoomDetails(int roomNumber) {
        Room room = findRoom(roomNumber);
        return room != null ? room.getDetailedInfo() : "Номер не найден";
    }

    public List<RoomService> getGuestServices(Guest guest) {
        return rooms.stream()
                .filter(room -> guest.equals(room.getCurrentGuest()))
                .flatMap(room -> room.getRoomServices().stream())
                .collect(Collectors.toList());
    }

    public Guest getGuestByRoomNumber(int roomNumber) {
        Room room = findRoom(roomNumber);
        return room != null ? room.getCurrentGuest() : null;
    }

    public Room findRoomByNumber(int roomNumber) {
        return findRoom(roomNumber);
    }

    public Service findServiceByName(String serviceName) {
        return findService(serviceName);
    }

    // Вспомогательные методы
    private Room findRoom(int roomNumber) {
        return rooms.stream()
                .filter(room -> room.getNumber() == roomNumber)
                .findFirst()
                .orElse(null);
    }

    private Service findService(String serviceName) {
        return services.stream()
                .filter(service -> service.getName().equalsIgnoreCase(serviceName))
                .findFirst()
                .orElse(null);
    }
}