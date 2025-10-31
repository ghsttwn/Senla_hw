package Task_4.T1;

import java.time.LocalDate;
import java.util.List;

public class HotelTest {
    public static void main(String[] args) {
        HotelAdministrator admin = new HotelAdministrator("Гранд Отель");

        // Добавляем номера
        admin.addRoom(new Room(101, "Стандарт", 2500, 2, 3));
        admin.addRoom(new Room(102, "Стандарт", 2300, 2, 3));
        admin.addRoom(new Room(201, "Люкс", 5000, 3, 4));
        admin.addRoom(new Room(202, "Люкс", 5500, 4, 5));
        admin.addRoom(new Room(301, "Президентский", 10000, 2, 5));

        // Добавляем услуги
        admin.addService(new Service("Завтрак", 500, "Шведский стол"));
        admin.addService(new Service("SPA", 1500, "Посещение спа-комплекса"));
        admin.addService(new Service("Трансфер", 800, "Трансфер из/в аэропорт"));

        // Заселяем гостей
        Guest guest1 = new Guest("Иван Иванов", "1234567890", "+7-123-456-7890");
        Guest guest2 = new Guest("Петр Петров", "0987654321", "+7-987-654-3210");
        Guest guest3 = new Guest("Анна Сидорова", "1122334455", "+7-111-222-3333");

        admin.checkIn(101, guest1, 3);
        admin.checkIn(201, guest2, 5);
        admin.checkIn(301, guest3, 2);

        // Добавляем услуги
        admin.addServiceToGuest(101, "Завтрак");
        admin.addServiceToGuest(201, "SPA");
        admin.addServiceToGuest(201, "Трансфер");

        // Демонстрация просмотра информации о гостях
        System.out.println("=== ИНФОРМАЦИЯ О ГОСТЯХ ===");

        // Показываем всех гостей с деталями
        System.out.println("\n--- Все постояльцы с контактными данными ---");
        admin.getGuestsWithRooms().forEach(entry -> {
            Guest guest = entry.getKey();
            Room room = entry.getValue();
            System.out.println("Гость: " + guest.getName());
            System.out.println("  Паспорт: " + guest.getPassportNumber());
            System.out.println("  Телефон: " + guest.getPhoneNumber());
            System.out.println("  Номер: " + room.getNumber());
            System.out.println("  Тип номера: " + room.getType());
            System.out.println("  Дата выезда: " + room.getCheckOutDate());
            System.out.println();
        });

        // Поиск гостя по паспорту
        System.out.println("--- Поиск гостя по паспорту '1234567890' ---");
        for (Room room : admin.getRooms()) {
            if (room.getCurrentGuest() != null &&
                    room.getCurrentGuest().getPassportNumber().equals("1234567890")) {
                Guest guest = room.getCurrentGuest();
                System.out.println("Найден гость:");
                System.out.println("  Имя: " + guest.getName());
                System.out.println("  Паспорт: " + guest.getPassportNumber());
                System.out.println("  Телефон: " + guest.getPhoneNumber());
                System.out.println("  Номер комнаты: " + room.getNumber());
                break;
            }
        }

        // Детальная информация о конкретном госте
        System.out.println("\n--- Детальная информация о госте номера 201 ---");
        Room room201 = admin.getRooms().stream()
                .filter(room -> room.getNumber() == 201)
                .findFirst()
                .orElse(null);

        if (room201 != null && room201.getCurrentGuest() != null) {
            Guest guest = room201.getCurrentGuest();
            System.out.println("Имя: " + guest.getName());
            System.out.println("Паспорт: " + guest.getPassportNumber());
            System.out.println("Телефон: " + guest.getPhoneNumber());
            System.out.println("Номер: " + room201.getNumber());
            System.out.println("Тип номера: " + room201.getType());
            System.out.println("Цена за ночь: " + room201.getPricePerNight() + " руб.");
            System.out.println("Дата заселения: " + room201.getCheckInDate());
            System.out.println("Дата выезда: " + room201.getCheckOutDate());

            // Расчет стоимости
            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                    room201.getCheckInDate(), LocalDate.now());
            double total = room201.calculateTotalPrice((int) Math.max(1, nights));
            System.out.println("Общая стоимость: " + total + " руб.");
        }

        // Остальная демонстрация...
        System.out.println("\n=== ОСТАЛЬНЫЕ ФУНКЦИИ ===");
        admin.displayAllRooms();
        admin.displayGuestsSortedByName();
        admin.displayServicesSortedByPrice();
    }
}