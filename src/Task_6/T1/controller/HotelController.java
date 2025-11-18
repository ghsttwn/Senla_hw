package Task_6.T1.controller;

import Task_6.T1.model.*;
import Task_6.T1.model.comparators.*;
import Task_6.T1.CSVImportExport;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HotelController {
    private List<Room> rooms;
    private List<Service> services;
    private List<Guest> guests;
    private String hotelName;

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
        this.guests = new ArrayList<>();
    }

    public String getHotelName() { return hotelName; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
    public List<Service> getServices() { return Collections.unmodifiableList(services); }
    public List<Guest> getGuests() { return Collections.unmodifiableList(guests); }

    // Методы импорта/экспорта
    public void importRoomsFromCSV(String filename) throws Exception {
        CSVImportExport<Room> csvHandler = new CSVImportExport<>(Room.class);
        List<Room> importedRooms = csvHandler.importFromFile(filename);

        for (Room importedRoom : importedRooms) {
            // Вручную устанавливаем статус на основе данных из CSV
            if (importedRoom.getStatus() == null) {
                importedRoom.setStatus(RoomStatus.AVAILABLE);
            }

            Optional<Room> existingRoom = findRoomById(importedRoom.getId());
            if (existingRoom.isPresent()) {
                Room room = existingRoom.get();
                room.setNumber(importedRoom.getNumber());
                room.setType(importedRoom.getType());
                room.setPricePerNight(importedRoom.getPricePerNight());
                room.setCapacity(importedRoom.getCapacity());
                room.setStars(importedRoom.getStars());
                // Статус не обновляем при импорте, чтобы не нарушить текущее состояние
            } else {
                if (importedRoom.getId() == null) {
                    importedRoom.setId(generateRoomId());
                }
                rooms.add(importedRoom);
            }
        }
    }

    public void exportRoomsToCSV(String filename) throws Exception {
        CSVImportExport<Room> csvHandler = new CSVImportExport<>(Room.class);
        csvHandler.exportToFile(rooms, filename);
    }

    public void importServicesFromCSV(String filename) throws Exception {
        CSVImportExport<Service> csvHandler = new CSVImportExport<>(Service.class);
        List<Service> importedServices = csvHandler.importFromFile(filename);

        for (Service importedService : importedServices) {
            Optional<Service> existingService = findServiceById(importedService.getId());
            if (existingService.isPresent()) {
                Service service = existingService.get();
                service.setName(importedService.getName());
                service.setPrice(importedService.getPrice());
                service.setDescription(importedService.getDescription());
            } else {
                if (importedService.getId() == null) {
                    importedService.setId(generateServiceId());
                }
                services.add(importedService);
            }
        }
    }

    public void exportServicesToCSV(String filename) throws Exception {
        CSVImportExport<Service> csvHandler = new CSVImportExport<>(Service.class);
        csvHandler.exportToFile(services, filename);
    }

    public void importGuestsFromCSV(String filename) throws Exception {
        CSVImportExport<Guest> csvHandler = new CSVImportExport<>(Guest.class);
        List<Guest> importedGuests = csvHandler.importFromFile(filename);

        for (Guest importedGuest : importedGuests) {
            Optional<Guest> existingGuest = findGuestById(importedGuest.getId());
            if (existingGuest.isPresent()) {
                Guest guest = existingGuest.get();
                guest.setName(importedGuest.getName());
                guest.setPassportNumber(importedGuest.getPassportNumber());
                guest.setPhoneNumber(importedGuest.getPhoneNumber());
            } else {
                if (importedGuest.getId() == null) {
                    importedGuest.setId(generateGuestId());
                }
                guests.add(importedGuest);
            }
        }
    }

    public void exportGuestsToCSV(String filename) throws Exception {
        CSVImportExport<Guest> csvHandler = new CSVImportExport<>(Guest.class);
        csvHandler.exportToFile(guests, filename);
    }

    // Вспомогательные методы для работы с ID
    private Long generateRoomId() {
        return rooms.stream()
                .map(Room::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    private Long generateServiceId() {
        return services.stream()
                .map(Service::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    private Long generateGuestId() {
        return guests.stream()
                .map(Guest::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    private Optional<Room> findRoomById(Long id) {
        return rooms.stream()
                .filter(room -> id != null && id.equals(room.getId()))
                .findFirst();
    }

    private Optional<Service> findServiceById(Long id) {
        return services.stream()
                .filter(service -> id != null && id.equals(service.getId()))
                .findFirst();
    }

    private Optional<Guest> findGuestById(Long id) {
        return guests.stream()
                .filter(guest -> id != null && id.equals(guest.getId()))
                .findFirst();
    }

    // Остальные методы остаются без изменений...
    public void addRoom(Room room) {
        Optional.ofNullable(room)
                .filter(r -> !rooms.contains(r))
                .ifPresent(r -> {
                    if (r.getId() == null) {
                        r.setId(generateRoomId());
                    }
                    rooms.add(r);
                });
    }

    public boolean checkIn(int roomNumber, Guest guest, int nights) {
        return findRoom(roomNumber)
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
                .map(room -> {
                    LocalDate checkInDate = LocalDate.now();
                    LocalDate checkOutDate = checkInDate.plusDays(nights);

                    if (guest.getId() == null) {
                        guest.setId(generateGuestId());
                    }
                    if (!guests.contains(guest)) {
                        guests.add(guest);
                    }

                    return room.checkIn(guest, checkInDate, checkOutDate);
                })
                .orElse(false);
    }

    public boolean checkOut(int roomNumber) {
        return findRoom(roomNumber)
                .filter(room -> room.getStatus() == RoomStatus.OCCUPIED)
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
                .ifPresent(s -> {
                    if (s.getId() == null) {
                        s.setId(generateServiceId());
                    }
                    services.add(s);
                });
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

    // Методы для получения данных
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
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
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
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
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

    // Вспомогательные методы
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

    public Optional<Map.Entry<Guest, Room>> findGuestByPassport(String passport) {
        return rooms.stream()
                .filter(room -> room.getCurrentGuest() != null)
                .filter(room -> room.getCurrentGuest().getPassportNumber().equals(passport))
                .map(room -> Map.entry(room.getCurrentGuest(), room))
                .findFirst();
    }

    public List<Service> getServicesByPriceRange(double minPrice, double maxPrice) {
        return services.stream()
                .filter(service -> service.getPrice() >= minPrice && service.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsByCriteria(int minStars, int maxCapacity, double maxPrice) {
        return rooms.stream()
                .filter(room -> room.getStars() >= minStars)
                .filter(room -> room.getCapacity() <= maxCapacity)
                .filter(room -> room.getPricePerNight() <= maxPrice)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getRoomTypeStatistics() {
        return rooms.stream()
                .collect(Collectors.groupingBy(
                        Room::getType,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getPopularServices() {
        return rooms.stream()
                .flatMap(room -> room.getRoomServices().stream())
                .collect(Collectors.groupingBy(
                        roomService -> roomService.getService().getName(),
                        Collectors.counting()
                ));
    }
}