package Task_4.T1;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class InteractiveHotelTest {
    private static HotelAdministrator admin;
    private static Scanner scanner;

    public static void main(String[] args) {
        admin = new HotelAdministrator("Гранд Отель");
        scanner = new Scanner(System.in);

        initializeTestData();

        System.out.println("=== СИСТЕМА УПРАВЛЕНИЯ ГОСТИНИЦЕЙ ===");
        System.out.println("Гостиница: " + admin.getHotelName());

        showMainMenu();
    }

    private static void initializeTestData() {
        // Добавляем номера
        admin.addRoom(new Room(101, "Стандарт", 2500, 2, 3));
        admin.addRoom(new Room(102, "Стандарт", 2300, 2, 3));
        admin.addRoom(new Room(103, "Стандарт", 2700, 3, 3));
        admin.addRoom(new Room(201, "Люкс", 5000, 3, 4));
        admin.addRoom(new Room(202, "Люкс", 5500, 4, 5));
        admin.addRoom(new Room(301, "Президентский", 10000, 2, 5));

        // Добавляем услуги
        admin.addService(new Service("Завтрак", 500, "Шведский стол"));
        admin.addService(new Service("SPA", 1500, "Посещение спа-комплекса"));
        admin.addService(new Service("Трансфер", 800, "Трансфер из/в аэропорт"));
        admin.addService(new Service("Прачечная", 300, "Стирка и глажка одежды"));
        admin.addService(new Service("Экскурсия", 1200, "Обзорная экскурсия по городу"));

        // Заселяем тестовых гостей
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
        admin.addServiceToGuest(301, "Экскурсия");
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
            System.out.println("1. Управление номерами");
            System.out.println("2. Управление гостями");
            System.out.println("3. Управление услугами");
            System.out.println("4. Просмотр отчетов");
            System.out.println("5. Поиск и сортировка");
            System.out.println("0. Выход");
            System.out.print("Выберите опцию: ");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> showRoomManagementMenu();
                case 2 -> showGuestManagementMenu();
                case 3 -> showServiceManagementMenu();
                case 4 -> showReportsMenu();
                case 5 -> showSearchMenu();
                case 0 -> {
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Неверный выбор! Попробуйте снова.");
            }
        }
    }

    private static void showRoomManagementMenu() {
        while (true) {
            System.out.println("\n=== УПРАВЛЕНИЕ НОМЕРАМИ ===");
            System.out.println("1. Показать все номера");
            System.out.println("2. Показать свободные номера");
            System.out.println("3. Заселить гостя");
            System.out.println("4. Выселить гостя");
            System.out.println("5. Изменить статус номера");
            System.out.println("6. Изменить цену номера");
            System.out.println("7. Показать детали номера");
            System.out.println("8. Назад");
            System.out.print("Выберите опцию: ");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> admin.displayAllRooms();
                case 2 -> admin.displayAvailableRooms();
                case 3 -> checkInGuest();
                case 4 -> checkOutGuest();
                case 5 -> changeRoomStatus();
                case 6 -> changeRoomPrice();
                case 7 -> showRoomDetails();
                case 8 -> { return; }
                default -> System.out.println("Неверный выбор! Попробуйте снова.");
            }
        }
    }

    private static void showGuestManagementMenu() {
        while (true) {
            System.out.println("\n=== УПРАВЛЕНИЕ ГОСТЯМИ ===");
            System.out.println("1. Показать всех постояльцев");
            System.out.println("2. Показать постояльцев (сортировка по имени)");
            System.out.println("3. Показать постояльцев (сортировка по дате выезда)");
            System.out.println("4. Добавить услугу гостю");
            System.out.println("5. Показать услуги гостя");
            System.out.println("6. Показать детальную информацию о госте");
            System.out.println("7. Найти гостя по паспорту");
            System.out.println("8. Назад");
            System.out.print("Выберите опцию: ");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> admin.displayAllGuests();
                case 2 -> admin.displayGuestsSortedByName();
                case 3 -> admin.displayGuestsSortedByCheckOutDate();
                case 4 -> addServiceToGuest();
                case 5 -> showGuestServices();
                case 6 -> showGuestDetails();
                case 7 -> findGuestByPassport();
                case 8 -> { return; }
                default -> System.out.println("Неверный выбор! Попробуйте снова.");
            }
        }
    }

    private static void showServiceManagementMenu() {
        while (true) {
            System.out.println("\n=== УПРАВЛЕНИЕ УСЛУГАМИ ===");
            System.out.println("1. Показать все услуги");
            System.out.println("2. Показать услуги (сортировка по цене)");
            System.out.println("3. Добавить услугу к номеру");
            System.out.println("4. Изменить цену услуги");
            System.out.println("5. Назад");
            System.out.print("Выберите опцию: ");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> admin.displayAllServices();
                case 2 -> admin.displayServicesSortedByPrice();
                case 3 -> addServiceToRoom();
                case 4 -> changeServicePrice();
                case 5 -> { return; }
                default -> System.out.println("Неверный выбор! Попробуйте снова.");
            }
        }
    }

    private static void showReportsMenu() {
        while (true) {
            System.out.println("\n=== ОТЧЕТЫ ===");
            System.out.println("1. Общая статистика");
            System.out.println("2. Сумма оплаты за номер");
            System.out.println("3. История проживаний номера");
            System.out.println("4. Назад");
            System.out.print("Выберите опцию: ");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> showStatistics();
                case 2 -> showRoomPayment();
                case 3 -> showRoomHistory();
                case 4 -> { return; }
                default -> System.out.println("Неверный выбор! Попробуйте снова.");
            }
        }
    }

    private static void showSearchMenu() {
        while (true) {
            System.out.println("\n=== ПОИСК И СОРТИРОВКА ===");
            System.out.println("1. Номера по цене (сортировка)");
            System.out.println("2. Номера по вместимости (сортировка)");
            System.out.println("3. Номера по звездам (сортировка)");
            System.out.println("4. Свободные номера по цене (сортировка)");
            System.out.println("5. Номера доступные на дату");
            System.out.println("6. Назад");
            System.out.print("Выберите опцию: ");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> admin.displayRoomsSortedByPrice();
                case 2 -> admin.displayRoomsSortedByCapacity();
                case 3 -> admin.displayRoomsSortedByStars();
                case 4 -> admin.displayAvailableRoomsSortedByPrice();
                case 5 -> searchRoomsByDate();
                case 6 -> { return; }
                default -> System.out.println("Неверный выбор! Попробуйте снова.");
            }
        }
    }

    // МЕТОДЫ ДЛЯ ОБРАБОТКИ ПОЛЬЗОВАТЕЛЬСКОГО ВВОДА

    private static void checkInGuest() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();

        System.out.print("Введите имя гостя: ");
        String name = scanner.nextLine();

        System.out.print("Введите номер паспорта: ");
        String passport = scanner.nextLine();

        System.out.print("Введите телефон: ");
        String phone = scanner.nextLine();

        System.out.print("Введите количество ночей: ");
        int nights = readIntInput();

        Guest guest = new Guest(name, passport, phone);
        admin.checkIn(roomNumber, guest, nights);
    }

    private static void checkOutGuest() {
        System.out.print("Введите номер комнаты для выселения: ");
        int roomNumber = readIntInput();
        admin.checkOut(roomNumber);
    }

    private static void changeRoomStatus() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();

        System.out.println("Доступные статусы:");
        admin.displayAvailableStatuses();

        System.out.print("Введите новый статус: ");
        String status = scanner.nextLine();

        admin.setRoomStatus(roomNumber, status);
    }

    private static void changeRoomPrice() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();

        System.out.print("Введите новую цену: ");
        double price = readDoubleInput();

        admin.changeRoomPrice(roomNumber, price);
    }

    private static void showRoomDetails() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();
        admin.displayRoomDetails(roomNumber);
    }

    private static void addServiceToGuest() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();

        System.out.print("Введите название услуги: ");
        String serviceName = scanner.nextLine();

        admin.addServiceToGuest(roomNumber, serviceName);
    }

    private static void showGuestServices() {
        System.out.print("Введите номер комнаты гостя: ");
        int roomNumber = readIntInput();

        // Находим гостя по номеру комнаты
        Guest guest = findGuestByRoomNumber(roomNumber);
        if (guest != null) {
            System.out.println("1. Сортировка по цене");
            System.out.println("2. Сортировка по дате");
            System.out.print("Выберите тип сортировки: ");

            int sortChoice = readIntInput();

            if (sortChoice == 1) {
                admin.displayGuestServicesSortedByPrice(guest);
            } else if (sortChoice == 2) {
                admin.displayGuestServicesSortedByDate(guest);
            } else {
                System.out.println("Неверный выбор!");
            }
        } else {
            System.out.println("Гость не найден в указанном номере!");
        }
    }

    private static void showGuestDetails() {
        System.out.print("Введите номер комнаты гостя: ");
        int roomNumber = readIntInput();

        Guest guest = findGuestByRoomNumber(roomNumber);
        if (guest != null) {
            displayGuestFullInfo(guest, roomNumber);
        } else {
            System.out.println("Гость не найден в указанном номере!");
        }
    }

    private static void findGuestByPassport() {
        System.out.print("Введите номер паспорта: ");
        String passport = scanner.nextLine();

        boolean found = false;
        for (Room room : admin.getRooms()) {
            if (room.getCurrentGuest() != null &&
                    room.getCurrentGuest().getPassportNumber().equals(passport)) {
                displayGuestFullInfo(room.getCurrentGuest(), room.getNumber());
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Гость с паспортом " + passport + " не найден!");
        }
    }

    private static void addServiceToRoom() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();

        System.out.print("Введите название услуги: ");
        String serviceName = scanner.nextLine();

        admin.addServiceToRoom(roomNumber, serviceName);
    }

    private static void changeServicePrice() {
        System.out.print("Введите название услуги: ");
        String serviceName = scanner.nextLine();

        System.out.print("Введите новую цену: ");
        double price = readDoubleInput();

        admin.changeServicePrice(serviceName, price);
    }

    private static void showStatistics() {
        System.out.println("=== ОБЩАЯ СТАТИСТИКА ===");
        System.out.println("Свободных номеров: " + admin.getTotalAvailableRooms());
        System.out.println("Всего постояльцев: " + admin.getTotalGuests());
        System.out.println("Всего номеров: " + admin.getRooms().size());
        System.out.println("Всего услуг: " + admin.getServices().size());
    }

    private static void showRoomPayment() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();

        double payment = admin.getRoomPayment(roomNumber);
        System.out.println("Сумма оплаты за номер " + roomNumber + ": " + payment + " руб.");
    }

    private static void showRoomHistory() {
        System.out.print("Введите номер комнаты: ");
        int roomNumber = readIntInput();

        List<StayHistory> history = admin.getLastThreeGuests(roomNumber);
        if (history.isEmpty()) {
            System.out.println("История проживаний отсутствует");
        } else {
            System.out.println("Последние " + history.size() + " проживаний:");
            history.forEach(System.out::println);
        }
    }

    private static void searchRoomsByDate() {
        System.out.print("Введите дату (гггг-мм-дд): ");
        String dateString = scanner.nextLine();

        try {
            LocalDate date = LocalDate.parse(dateString);
            List<Room> availableRooms = admin.getRoomsAvailableOnDate(date);
            System.out.println("Номера доступные " + date + ": " + availableRooms.size());
            availableRooms.forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Неверный формат даты! Используйте гггг-мм-дд");
        }
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    private static Guest findGuestByRoomNumber(int roomNumber) {
        for (Room room : admin.getRooms()) {
            if (room.getNumber() == roomNumber) {
                return room.getCurrentGuest();
            }
        }
        return null;
    }

    private static void displayGuestFullInfo(Guest guest, int roomNumber) {
        System.out.println("\n=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ГОСТЕ ===");
        System.out.println("Имя: " + guest.getName());
        System.out.println("Паспорт: " + guest.getPassportNumber());
        System.out.println("Телефон: " + guest.getPhoneNumber());
        System.out.println("Номер комнаты: " + roomNumber);

        // Находим комнату для дополнительной информации
        Room room = findRoom(roomNumber);
        if (room != null) {
            System.out.println("Тип номера: " + room.getType());
            System.out.println("Дата заселения: " + room.getCheckInDate());
            System.out.println("Дата выезда: " + room.getCheckOutDate());
            System.out.println("Стоимость номера за ночь: " + room.getPricePerNight() + " руб.");

            // Расчет общей стоимости
            if (room.getCheckInDate() != null) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(
                        room.getCheckInDate(), LocalDate.now());
                double total = room.calculateTotalPrice((int) Math.max(1, nights));
                System.out.println("Общая стоимость проживания: " + total + " руб.");
            }

            // Услуги гостя
            List<RoomService> guestServices = admin.getGuestServices(guest);
            if (!guestServices.isEmpty()) {
                System.out.println("\nУслуги гостя:");
                guestServices.forEach(service ->
                        System.out.println("  - " + service.getService().getName() +
                                " (" + service.getPrice() + " руб.) - " + service.getDate()));
            }
        }
    }

    private static Room findRoom(int roomNumber) {
        return admin.getRooms().stream()
                .filter(room -> room.getNumber() == roomNumber)
                .findFirst()
                .orElse(null);
    }

    private static int readIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Неверный формат числа! Введите целое число: ");
            }
        }
    }

    private static double readDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Неверный формат числа! Введите число: ");
            }
        }
    }
}