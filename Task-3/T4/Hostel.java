package T4;

import java.util.*;

// Класс услуги
class Service {
    private String name;
    private double price;
    private String description;

    public Service(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }

    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return name + " - " + price + " руб. (" + description + ")";
    }
}

// Класс номера
class Room {
    private int number;
    private String type;
    private double pricePerNight;
    private String status; // Используем String вместо enum
    private Guest currentGuest;
    private List<Service> additionalServices;

    // Константы для статусов
    public static final String AVAILABLE = "Доступен";
    public static final String OCCUPIED = "Занят";
    public static final String UNDER_MAINTENANCE = "На ремонте";
    public static final String UNDER_SERVICE = "На обслуживании";

    public Room(int number, String type, double pricePerNight) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = AVAILABLE;
        this.additionalServices = new ArrayList<>();
    }

    // Геттеры
    public int getNumber() { return number; }
    public String getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public String getStatus() { return status; }
    public Guest getCurrentGuest() { return currentGuest; }
    public List<Service> getAdditionalServices() { return additionalServices; }

    // Сеттеры
    public void setPricePerNight(double price) { this.pricePerNight = price; }

    public boolean setStatus(String newStatus) {
        // Проверяем, что статус корректен
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

    public boolean checkIn(Guest guest) {
        if (status.equals(AVAILABLE)) {
            this.currentGuest = guest;
            this.status = OCCUPIED;
            return true;
        }
        return false;
    }

    public boolean checkOut() {
        if (status.equals(OCCUPIED) && currentGuest != null) {
            this.currentGuest = null;
            this.status = AVAILABLE;
            return true;
        }
        return false;
    }

    public void addService(Service service) {
        additionalServices.add(service);
    }

    public double calculateTotalPrice(int nights) {
        double total = pricePerNight * nights;
        for (Service service : additionalServices) {
            total += service.getPrice();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Номер " + number + " (" + type + ") - " + pricePerNight +
                " руб./ночь - " + status +
                (currentGuest != null ? " - " + currentGuest.getName() : "");
    }
}

// Класс гостя
class Guest {
    private String name;
    private String passportNumber;
    private String phoneNumber;

    public Guest(String name, String passportNumber, String phoneNumber) {
        this.name = name;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
    }

    // Геттеры
    public String getName() { return name; }
    public String getPassportNumber() { return passportNumber; }
    public String getPhoneNumber() { return phoneNumber; }

    @Override
    public String toString() {
        return name + " (паспорт: " + passportNumber + ", тел.: " + phoneNumber + ")";
    }
}

// Основной класс администратора гостиницы
class HotelAdministrator {
    private List<Room> rooms;
    private List<Service> services;
    private String hotelName;

    public HotelAdministrator(String hotelName) {
        this.hotelName = hotelName;
        this.rooms = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    // Методы для работы с номерами
    public void addRoom(Room room) {
        rooms.add(room);
        System.out.println("Добавлен номер: " + room);
    }

    public boolean checkIn(int roomNumber, Guest guest, int nights) {
        Room room = findRoom(roomNumber);
        if (room != null && room.checkIn(guest)) {
            double total = room.calculateTotalPrice(nights);
            System.out.println("Гость " + guest.getName() + " заселен в номер " +
                    roomNumber + " на " + nights + " ночей. Общая стоимость: " + total + " руб.");
            return true;
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

    // Методы для работы с услугами
    public void addService(Service service) {
        services.add(service);
        System.out.println("Добавлена услуга: " + service);
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

    // Вспомогательные методы
    private Room findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    private Service findService(String serviceName) {
        for (Service service : services) {
            if (service.getName().equalsIgnoreCase(serviceName)) {
                return service;
            }
        }
        return null;
    }

    // Методы для получения информации
    public void displayAllRooms() {
        System.out.println("\n=== ВСЕ НОМЕРА ГОСТИНИЦЫ '" + hotelName + "' ===");
        for (Room room : rooms) {
            System.out.println(room);
        }
    }

    public void displayAllServices() {
        System.out.println("\n=== ВСЕ УСЛУГИ ГОСТИНИЦЫ ===");
        for (Service service : services) {
            System.out.println(service);
        }
    }

    public void displayAvailableRooms() {
        System.out.println("\n=== СВОБОДНЫЕ НОМЕРА ===");
        for (Room room : rooms) {
            if (room.getStatus().equals(Room.AVAILABLE)) {
                System.out.println(room);
            }
        }
    }

    // Метод для получения списка допустимых статусов
    public void displayAvailableStatuses() {
        System.out.println("\n=== ДОСТУПНЫЕ СТАТУСЫ НОМЕРОВ ===");
        System.out.println("- " + Room.AVAILABLE);
        System.out.println("- " + Room.OCCUPIED);
        System.out.println("- " + Room.UNDER_MAINTENANCE);
        System.out.println("- " + Room.UNDER_SERVICE);
    }
}

// Тестовый класс
class HotelTest {
    public static void main(String[] args) {
        // Создаем администратора гостиницы
        HotelAdministrator admin = new HotelAdministrator("Гранд Отель");

        // Добавляем номера
        admin.addRoom(new Room(101, "Стандарт", 2500));
        admin.addRoom(new Room(102, "Стандарт", 2500));
        admin.addRoom(new Room(201, "Люкс", 5000));
        admin.addRoom(new Room(202, "Люкс", 5000));
        admin.addRoom(new Room(301, "Президентский", 10000));

        // Добавляем услуги
        admin.addService(new Service("Завтрак", 500, "Шведский стол"));
        admin.addService(new Service("SPA", 1500, "Посещение спа-комплекса"));
        admin.addService(new Service("Трансфер", 800, "Трансфер из/в аэропорт"));
        admin.addService(new Service("Прачечная", 300, "Стирка и глажка одежды"));

        // Демонстрация работы

        // Заселяем гостей
        Guest guest1 = new Guest("Иван Иванов", "1234567890", "+7-123-456-7890");
        Guest guest2 = new Guest("Петр Петров", "0987654321", "+7-987-654-3210");

        admin.checkIn(101, guest1, 3);
        admin.addServiceToRoom(101, "Завтрак");

        admin.checkIn(201, guest2, 5);
        admin.addServiceToRoom(201, "SPA");
        admin.addServiceToRoom(201, "Трансфер");

        // Выводим информацию о номерах
        admin.displayAllRooms();

        // Изменяем статус номера (используем константы из Room)
        admin.setRoomStatus(102, Room.UNDER_MAINTENANCE);
        admin.setRoomStatus(202, Room.UNDER_SERVICE);

        // Изменяем цены
        admin.changeRoomPrice(301, 12000);
        admin.changeServicePrice("SPA", 1800);

        // Выселяем гостя
        admin.checkOut(101);

        // Выводим итоговую информацию
        admin.displayAllRooms();
        admin.displayAvailableRooms();
        admin.displayAllServices();
        admin.displayAvailableStatuses();

        // Пытаемся установить некорректный статус
        admin.setRoomStatus(301, "Несуществующий статус");
    }
}