package T4;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HotelAdministrator {
    private List<Room> rooms;
    private List<Service> services;
    private String hotelName;

    public HotelAdministrator(String hotelName) {
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
            System.out.println("Добавлен номер: " + room);
        }
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

    public void displayAvailableStatuses() {
        System.out.println("\n=== ДОСТУПНЫЕ СТАТУСЫ НОМЕРОВ ===");
        System.out.println("- " + Room.AVAILABLE);
        System.out.println("- " + Room.OCCUPIED);
        System.out.println("- " + Room.UNDER_MAINTENANCE);
        System.out.println("- " + Room.UNDER_SERVICE);
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