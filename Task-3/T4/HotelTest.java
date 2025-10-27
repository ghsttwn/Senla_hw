package T4;

public class HotelTest {
    public static void main(String[] args) {
        HotelAdministrator admin = new HotelAdministrator("Гранд Отель");

        admin.addRoom(new Room(101, "Стандарт", 2500));
        admin.addRoom(new Room(102, "Стандарт", 2500));
        admin.addRoom(new Room(201, "Люкс", 5000));

        admin.addService(new Service("Завтрак", 500, "Шведский стол"));
        admin.addService(new Service("SPA", 1500, "Посещение спа-комплекса"));

        Guest guest1 = new Guest("Иван Иванов", "1234567890", "+7-123-456-7890");
        Guest guest2 = new Guest("Петр Петров", "0987654321", "+7-987-654-3210");

        admin.checkIn(101, guest1, 3);
        admin.addServiceToRoom(101, "Завтрак");

        admin.checkIn(201, guest2, 5);
        admin.addServiceToRoom(201, "SPA");

        admin.displayAllRooms();
        admin.setRoomStatus(102, Room.UNDER_MAINTENANCE);
        admin.changeRoomPrice(201, 5500);
        admin.checkOut(101);

        admin.displayAllRooms();
        admin.displayAvailableRooms();
    }
}
