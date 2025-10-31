package Task_4.T1;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HotelAdministrator {
    private List<Room> rooms;
    private List<Service> services;
    private String hotelName;

    // Компараторы (сильное сцепление)
    private final Comparator<Room> roomPriceComparator = new RoomPriceComparator();
    private final Comparator<Room> roomCapacityComparator = new RoomCapacityComparator();
    private final Comparator<Room> roomStarsComparator = new RoomStarsComparator();
    private final Comparator<Service> servicePriceComparator = new ServicePriceComparator();
    private final Comparator<Map.Entry<Guest, Room>> guestCheckOutDateComparator = new GuestCheckOutDateComparator();

    public HotelAdministrator(String hotelName) {
        this.hotelName = hotelName;
        this.rooms = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    public String getHotelName() { return hotelName; }
    public List<Room> getRooms() { return new ArrayList<>(rooms); }
    public List<Service> getServices() { return new ArrayList<>(services); }

    // Базовые методы управления
    public void addRoom(Room room) {
        if (room != null && !rooms.contains(room)) {
            rooms.add(room);
            System.out.println("Добавлен номер: " + room);
        }
    }

    public boolean checkIn(int roomNumber, Guest guest, int nights) {
        Room room = findRoom(roomNumber);
        if (room != null) {
            LocalDate checkInDate = LocalDate.now();
            LocalDate checkOutDate = checkInDate.plusDays(nights);
            if (room.checkIn(guest, checkInDate, checkOutDate)) {
                double total = room.calculateTotalPrice(nights);
                System.out.println("Гость " + guest.getName() + " заселен в номер " +
                        roomNumber + " на " + nights + " ночей. Общая стоимость: " + total + " руб.");
                return true;
            }
        }
        System.out.println("Не удалось заселить гостя в номер " + roomNumber);
        return false;
    }

    public boolean checkOut(int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room != null && room.checkOut()) {
            System.out.println("Гость выселен из номера " + roomNumber);
            return true;
        }
        System.out.println("Не удалось выселить гостя из номера " + roomNumber);
        return false;
    }

    public boolean setRoomStatus(int roomNumber, String status) {
        Room room = findRoom(roomNumber);
        if (room != null && room.setStatus(status)) {
            System.out.println("Статус номера " + roomNumber + " изменен на: " + status);
            return true;
        }
        System.out.println("Не удалось изменить статус номера " + roomNumber + " на: " + status);
        return false;
    }

    public boolean changeRoomPrice(int roomNumber, double newPrice) {
        Room room = findRoom(roomNumber);
        if (room != null) {
            room.setPricePerNight(newPrice);
            System.out.println("Цена номера " + roomNumber + " изменена на: " + newPrice + " руб./ночь");
            return true;
        }
        return false;
    }

    public void addService(Service service) {
        if (service != null && !services.contains(service)) {
            services.add(service);
            System.out.println("Добавлена услуга: " + service);
        }
    }

    public boolean changeServicePrice(String serviceName, double newPrice) {
        Service service = findService(serviceName);
        if (service != null) {
            service.setPrice(newPrice);
            System.out.println("Цена услуги '" + serviceName + "' изменена на: " + newPrice + " руб.");
            return true;
        }
        return false;
    }

    public boolean addServiceToRoom(int roomNumber, String serviceName) {
        Room room = findRoom(roomNumber);
        Service service = findService(serviceName);
        if (room != null && service != null) {
            room.addService(service);
            System.out.println("Услуга '" + serviceName + "' добавлена к номеру " + roomNumber);
            return true;
        }
        return false;
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Добавить услугу гостю
    public boolean addServiceToGuest(int roomNumber, String serviceName) {
        Room room = findRoom(roomNumber);
        Service service = findService(serviceName);
        if (room != null && service != null && room.getCurrentGuest() != null) {
            room.addRoomService(service, LocalDate.now());
            System.out.println("Услуга '" + serviceName + "' добавлена для гостя " +
                    room.getCurrentGuest().getName() + " в номер " + roomNumber);
            return true;
        }
        System.out.println("Не удалось добавить услугу гостю. Проверьте номер комнаты и наличие гостя.");
        return false;
    }

    // Методы для отображения информации
    public void displayAllRooms() {
        System.out.println("\n=== ВСЕ НОМЕРА ГОСТИНИЦЫ '" + hotelName + "' ===");
        Collections.sort(rooms); // Используем natural ordering (по номеру)
        rooms.forEach(System.out::println);
    }

    public void displayAllServices() {
        System.out.println("\n=== ВСЕ УСЛУГИ ГОСТИНИЦЫ ===");
        Collections.sort(services); // Используем natural ordering (по имени)
        services.forEach(System.out::println);
    }

    public void displayAvailableRooms() {
        System.out.println("\n=== СВОБОДНЫЕ НОМЕРА ===");
        rooms.stream()
                .filter(room -> room.getStatus().equals(Room.AVAILABLE))
                .sorted() // Natural ordering
                .forEach(System.out::println);
    }

    public void displayAvailableStatuses() {
        System.out.println("\n=== ДОСТУПНЫЕ СТАТУСЫ НОМЕРОВ ===");
        System.out.println("- " + Room.AVAILABLE);
        System.out.println("- " + Room.OCCUPIED);
        System.out.println("- " + Room.UNDER_MAINTENANCE);
        System.out.println("- " + Room.UNDER_SERVICE);
    }

    // НОВЫЕ МЕТОДЫ С ИСПОЛЬЗОВАНИЕМ COMPARATOR
    public void displayRoomsSortedByPrice() {
        System.out.println("\n=== НОМЕРА ОТСОРТИРОВАННЫЕ ПО ЦЕНЕ ===");
        rooms.stream()
                .sorted(roomPriceComparator)
                .forEach(System.out::println);
    }

    public void displayRoomsSortedByCapacity() {
        System.out.println("\n=== НОМЕРА ОТСОРТИРОВАННЫЕ ПО ВМЕСТИМОСТИ ===");
        rooms.stream()
                .sorted(roomCapacityComparator)
                .forEach(System.out::println);
    }

    public void displayRoomsSortedByStars() {
        System.out.println("\n=== НОМЕРА ОТСОРТИРОВАННЫЕ ПО ЗВЕЗДАМ ===");
        rooms.stream()
                .sorted(roomStarsComparator)
                .forEach(System.out::println);
    }

    public void displayAvailableRoomsSortedByPrice() {
        System.out.println("\n=== СВОБОДНЫЕ НОМЕРА ОТСОРТИРОВАННЫЕ ПО ЦЕНЕ ===");
        rooms.stream()
                .filter(room -> room.getStatus().equals(Room.AVAILABLE))
                .sorted(roomPriceComparator)
                .forEach(System.out::println);
    }

    public void displayGuestsSortedByName() {
        System.out.println("\n=== ПОСТОЯЛЬЦЫ ОТСОРТИРОВАННЫЕ ПО ИМЕНИ ===");
        getGuestsWithRooms().stream()
                .sorted(Map.Entry.comparingByKey()) // Используем natural ordering Guest
                .forEach(entry -> System.out.println(entry.getKey().getName() + " - Номер " + entry.getValue().getNumber()));
    }

    public void displayGuestsSortedByCheckOutDate() {
        System.out.println("\n=== ПОСТОЯЛЬЦЫ ОТСОРТИРОВАННЫЕ ПО ДАТЕ ВЫЕЗДА ===");
        getGuestsWithRooms().stream()
                .sorted(guestCheckOutDateComparator)
                .forEach(entry -> System.out.println(entry.getKey().getName() + " - Выезд: " + entry.getValue().getCheckOutDate()));
    }

    public void displayServicesSortedByPrice() {
        System.out.println("\n=== УСЛУГИ ОТСОРТИРОВАННЫЕ ПО ЦЕНЕ ===");
        services.stream()
                .sorted(servicePriceComparator)
                .forEach(System.out::println);
    }

    // ДОБАВЛЕННЫЕ МЕТОДЫ: Показать услуги гостя с сортировкой
    public void displayGuestServicesSortedByPrice(Guest guest) {
        System.out.println("\n=== УСЛУГИ ГОСТЯ " + guest.getName() + " ОТСОРТИРОВАННЫЕ ПО ЦЕНЕ ===");
        List<RoomService> guestServices = getGuestServices(guest);
        if (guestServices.isEmpty()) {
            System.out.println("У гостя нет услуг");
        } else {
            guestServices.stream()
                    .sorted(Comparator.comparingDouble(RoomService::getPrice))
                    .forEach(service -> System.out.println("  - " + service.getService().getName() +
                            " (" + service.getPrice() + " руб.) - " + service.getDate()));
        }
    }

    public void displayGuestServicesSortedByDate(Guest guest) {
        System.out.println("\n=== УСЛУГИ ГОСТЯ " + guest.getName() + " ОТСОРТИРОВАННЫЕ ПО ДАТЕ ===");
        List<RoomService> guestServices = getGuestServices(guest);
        if (guestServices.isEmpty()) {
            System.out.println("У гостя нет услуг");
        } else {
            guestServices.stream()
                    .sorted(Comparator.comparing(RoomService::getDate))
                    .forEach(service -> System.out.println("  - " + service.getService().getName() +
                            " (" + service.getPrice() + " руб.) - " + service.getDate()));
        }
    }

    // Статистические методы
    public long getTotalAvailableRooms() {
        return rooms.stream()
                .filter(room -> room.getStatus().equals(Room.AVAILABLE))
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
                .sorted() // Natural ordering
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

    public void displayRoomDetails(int roomNumber) {
        System.out.println("\n=== ДЕТАЛИ НОМЕРА " + roomNumber + " ===");
        System.out.println(getRoomDetails(roomNumber));
    }

    // Дополнительные методы для отображения всех постояльцев
    public void displayAllGuests() {
        System.out.println("\n=== ВСЕ ПОСТОЯЛЬЦЫ ===");
        List<Map.Entry<Guest, Room>> guests = getGuestsWithRooms();
        if (guests.isEmpty()) {
            System.out.println("Нет постояльцев");
        } else {
            guests.forEach(entry -> {
                Guest guest = entry.getKey();
                Room room = entry.getValue();
                System.out.println(guest.getName() +
                        " (паспорт: " + guest.getPassportNumber() +
                        ", тел: " + guest.getPhoneNumber() +
                        ") - Номер " + room.getNumber());
            });
        }
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Получить гостя по номеру комнаты
    public Guest getGuestByRoomNumber(int roomNumber) {
        Room room = findRoom(roomNumber);
        return room != null ? room.getCurrentGuest() : null;
    }

    // ДОБАВЛЕННЫЕ МЕТОДЫ ДЛЯ InteractiveHotelTest

    public void showStatistics() {
        System.out.println("=== ОБЩАЯ СТАТИСТИКА ===");
        System.out.println("Свободных номеров: " + getTotalAvailableRooms());
        System.out.println("Всего постояльцев: " + getTotalGuests());
        System.out.println("Всего номеров: " + rooms.size());
        System.out.println("Всего услуг: " + services.size());
    }

    public void showRoomPayment(int roomNumber) {
        double payment = getRoomPayment(roomNumber);
        System.out.println("Сумма оплаты за номер " + roomNumber + ": " + payment + " руб.");
    }

    public void showRoomHistory(int roomNumber) {
        List<StayHistory> history = getLastThreeGuests(roomNumber);
        if (history.isEmpty()) {
            System.out.println("История проживаний отсутствует");
        } else {
            System.out.println("Последние " + history.size() + " проживаний:");
            history.forEach(System.out::println);
        }
    }

    public void searchRoomsByDate(LocalDate date) {
        List<Room> availableRooms = getRoomsAvailableOnDate(date);
        System.out.println("Номера доступные " + date + ": " + availableRooms.size());
        availableRooms.forEach(System.out::println);
    }

    // Вспомогательные методы (ИСПРАВЛЕННЫЕ)
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

    // ИСПРАВЛЕННЫЙ МЕТОД - используем List вместо Map
    public List<Map.Entry<Guest, Room>> getGuestsWithRooms() {
        return rooms.stream()
                .filter(room -> room.getCurrentGuest() != null)
                .map(room -> Map.entry(room.getCurrentGuest(), room))
                .collect(Collectors.toList());
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Получить услуги гостя
    public List<RoomService> getGuestServices(Guest guest) {
        return rooms.stream()
                .filter(room -> guest.equals(room.getCurrentGuest()))
                .flatMap(room -> room.getRoomServices().stream())
                .collect(Collectors.toList());
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Получить услуги гостя по номеру комнаты
    public List<RoomService> getGuestServicesByRoomNumber(int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room != null && room.getCurrentGuest() != null) {
            return getGuestServices(room.getCurrentGuest());
        }
        return new ArrayList<>();
    }

    private String getRoomDetails(int roomNumber) {
        Room room = findRoom(roomNumber);
        return room != null ? room.getDetailedInfo() : "Номер не найден";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotelAdministrator that = (HotelAdministrator) o;
        return Objects.equals(hotelName, that.hotelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelName);
    }

    @Override
    public String toString() {
        return "HotelAdministrator{" +
                "hotelName='" + hotelName + '\'' +
                ", roomsCount=" + rooms.size() +
                ", servicesCount=" + services.size() +
                '}';
    }
}