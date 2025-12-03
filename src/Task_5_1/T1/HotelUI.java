package Task_5_1.T1;

import Task_5.T1.NavigationManager;
import Task_5.T1.controller.HotelController;
import Task_5.T1.factory.HotelMenuFactory;
import Task_5.T1.model.*;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import java.time.LocalDate;

public class HotelUI extends Task_5.T1.HotelUI {
    private static HotelUI instance;
    private HotelController controller;
    private HotelMenuFactory menuFactory;
    private Task_5.T1.NavigationManager navigator;
    private Scanner scanner;
    private boolean running;

    private HotelUI() {
        super();
        this.controller = new HotelController("Гранд Отель");
        this.menuFactory = new HotelMenuFactory(this);
        this.navigator = NavigationManager.getInstance();
        this.scanner = new Scanner(System.in);
        this.running = true;
        initializeTestData();
    }

    public static HotelUI getInstance() {
        if (instance == null) {
            instance = new HotelUI();
        }
        return instance;
    }

    private void initializeTestData() {
        // Добавляем номера
        controller.addRoom(new Room(101, "Стандарт", 2500, 2, 3));
        controller.addRoom(new Room(102, "Стандарт", 2300, 2, 3));
        controller.addRoom(new Room(201, "Люкс", 5000, 3, 4));
        controller.addRoom(new Room(202, "Люкс", 5500, 4, 5));
        controller.addRoom(new Room(301, "Президентский", 10000, 2, 5));

        // Добавляем услуги
        controller.addService(new Service("Завтрак", 500, "Шведский стол"));
        controller.addService(new Service("SPA", 1500, "Посещение спа-комплекса"));
        controller.addService(new Service("Трансфер", 800, "Трансфер из/в аэропорт"));
        controller.addService(new Service("Прачечная", 300, "Стирка и глажка одежды"));

        // Заселяем гостей
        Guest guest1 = new Guest("Иван Иванов", "1234567890", "+7-123-456-7890");
        Guest guest2 = new Guest("Петр Петров", "0987654321", "+7-987-654-3210");
        Guest guest3 = new Guest("Анна Сидорova", "1122334455", "+7-111-222-3333");

        controller.checkIn(101, guest1, 3);
        controller.checkIn(201, guest2, 5);
        controller.checkIn(301, guest3, 2);

        controller.addServiceToGuest(101, "Завтрак");
        controller.addServiceToGuest(201, "SPA");
        controller.addServiceToGuest(201, "Трансфер");
    }

    public void start() {
        System.out.println("=== СИСТЕМА УПРАВЛЕНИЯ ГОСТИНИЦЕЙ ===");
        System.out.println("Гостиница: " + controller.getHotelName());

        navigator.navigateTo(menuFactory.buildMainMenu(), true);
        startNavigation();
    }

    private void startNavigation() {
        while (running) {
            try {
                int choice = readIntInput("\nВыберите пункт меню: ");
                if (choice == 0 && navigator.getCurrentMenu().getTitle().equals("ГОСТИНИЦА - ГЛАВНОЕ МЕНЮ")) {
                    stop();
                    break;
                }
                navigator.executeMenuItem(choice);
            } catch (Exception e) {
                System.out.println("Ошибка ввода. Пожалуйста, введите число.");
            }
        }
    }

    public void stop() {
        running = false;
        System.out.println("Программа завершена. До свидания!");
        scanner.close();
    }

    // Методы меню
    public void showRoomManagementMenu() {
        navigator.navigateTo(menuFactory.buildRoomManagementMenu(), false);
    }

    public void showGuestManagementMenu() {
        navigator.navigateTo(menuFactory.buildGuestManagementMenu(), false);
    }

    public void showServiceManagementMenu() {
        navigator.navigateTo(menuFactory.buildServiceManagementMenu(), false);
    }

    public void showReportsMenu() {
        navigator.navigateTo(menuFactory.buildReportsMenu(), false);
    }

    public void showSearchMenu() {
        navigator.navigateTo(menuFactory.buildSearchMenu(), false);
    }

    // Методы отображения
    public void displayAllRooms() {
        System.out.println("\n--- ВСЕ НОМЕРА ---");
        List<Room> rooms = controller.getRooms();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    public void displayAvailableRooms() {
        System.out.println("\n--- СВОБОДНЫЕ НОМЕРА ---");
        List<Room> availableRooms = controller.getRooms().stream()
                .filter(room -> room.getStatus().equals(RoomStatus.AVAILABLE.getDescription()))
                .toList();
        if (availableRooms.isEmpty()) {
            System.out.println("Нет свободных номеров");
        } else {
            availableRooms.forEach(System.out::println);
        }
    }

    public void displayAllGuests() {
        System.out.println("\n--- ВСЕ ПОСТОЯЛЬЦЫ ---");
        List<Map.Entry<Guest, Room>> guests = controller.getGuestsWithRooms();
        if (guests.isEmpty()) {
            System.out.println("Нет постояльцев");
        } else {
            guests.forEach(entry -> {
                Guest guest = entry.getKey();
                Room room = entry.getValue();
                System.out.println(guest.getName() + " (паспорт: " + guest.getPassportNumber() +
                        ", тел: " + guest.getPhoneNumber() + ") - Номер " + room.getNumber());
            });
        }
    }

    public void displayGuestsSortedByName() {
        System.out.println("\n--- ПОСТОЯЛЬЦЫ (СОРТИРОВКА ПО ИМЕНИ) ---");
        List<Map.Entry<Guest, Room>> guests = controller.getGuestsSortedByName();
        if (guests.isEmpty()) {
            System.out.println("Нет постояльцев");
        } else {
            guests.forEach(entry ->
                    System.out.println(entry.getKey().getName() + " - Номер " + entry.getValue().getNumber()));
        }
    }

    public void displayGuestsSortedByCheckOutDate() {
        System.out.println("\n--- ПОСТОЯЛЬЦЫ (СОРТИРОВКА ПО ДАТЕ ВЫЕЗДА) ---");
        List<Map.Entry<Guest, Room>> guests = controller.getGuestsSortedByCheckOutDate();
        if (guests.isEmpty()) {
            System.out.println("Нет постояльцев");
        } else {
            guests.forEach(entry ->
                    System.out.println(entry.getKey().getName() + " - Выезд: " + entry.getValue().getCheckOutDate()));
        }
    }

    public void displayAllServices() {
        System.out.println("\n--- ВСЕ УСЛУГИ ---");
        List<Service> services = controller.getServices();
        if (services.isEmpty()) {
            System.out.println("Нет услуг");
        } else {
            services.forEach(System.out::println);
        }
    }

    public void displayServicesSortedByPrice() {
        System.out.println("\n--- УСЛУГИ (СОРТИРОВКА ПО ЦЕНЕ) ---");
        List<Service> services = controller.getServicesSortedByPrice();
        if (services.isEmpty()) {
            System.out.println("Нет услуг");
        } else {
            services.forEach(System.out::println);
        }
    }

    public void displayRoomsSortedByPrice() {
        System.out.println("\n--- НОМЕРА (СОРТИРОВКА ПО ЦЕНЕ) ---");
        List<Room> rooms = controller.getRoomsSortedByPrice();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    public void displayRoomsSortedByCapacity() {
        System.out.println("\n--- НОМЕРА (СОРТИРОВКА ПО ВМЕСТИМОСТИ) ---");
        List<Room> rooms = controller.getRoomsSortedByCapacity();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    public void displayRoomsSortedByStars() {
        System.out.println("\n--- НОМЕРА (СОРТИРОВКА ПО ЗВЕЗДАМ) ---");
        List<Room> rooms = controller.getRoomsSortedByStars();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    public void displayAvailableRoomsSortedByPrice() {
        System.out.println("\n--- СВОБОДНЫЕ НОМЕРА (СОРТИРОВКА ПО ЦЕНЕ) ---");
        List<Room> rooms = controller.getAvailableRoomsSortedByPrice();
        if (rooms.isEmpty()) {
            System.out.println("Нет свободных номеров");
        } else {
            rooms.forEach(System.out::println);
        }
    }

    // Методы управления номерами с проверкой ввода
    public void checkInGuest() {
        System.out.println("\n--- ЗАСЕЛЕНИЕ ГОСТЯ ---");

        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        String name = readNonEmptyString("Введите имя гостя: ");
        String passport = readNonEmptyString("Введите номер паспорта: ");
        String phone = readNonEmptyString("Введите телефон: ");

        int nights = readIntInputWithValidation("Введите количество ночей: ", 1, 365);

        Guest guest = new Guest(name, passport, phone);
        if (controller.checkIn(roomNumber, guest, nights)) {
            System.out.println("Гость успешно заселен!");
        } else {
            System.out.println("Ошибка при заселении! Проверьте номер комнаты и ее доступность.");
        }
    }

    public void checkOutGuest() {
        System.out.println("\n--- ВЫСЕЛЕНИЕ ГОСТЯ ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        if (controller.checkOut(roomNumber)) {
            System.out.println("Гость успешно выселен!");
        } else {
            System.out.println("Ошибка при выселении! Проверьте номер комнаты.");
        }
    }

    public void changeRoomStatus() {
        System.out.println("\n--- ИЗМЕНЕНИЕ СТАТУСА НОМЕРА ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        System.out.println("Доступные статусы:");
        for (String status : RoomStatus.getAllDescriptions()) {
            System.out.println("- " + status);
        }

        String status = readNonEmptyString("Введите новый статус: ");

        if (controller.setRoomStatus(roomNumber, status)) {
            System.out.println("Статус успешно изменен!");
        } else {
            System.out.println("Ошибка при изменении статуса! Проверьте номер комнаты и корректность статуса.");
        }
    }

    public void changeRoomPrice() {
        System.out.println("\n--- ИЗМЕНЕНИЕ ЦЕНЫ НОМЕРА ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        double price = readDoubleInputWithValidation("Введите новую цену: ", 1, 100000);

        if (controller.changeRoomPrice(roomNumber, price)) {
            System.out.println("Цена номера успешно изменена!");
        } else {
            System.out.println("Ошибка при изменении цены! Проверьте номер комнаты.");
        }
    }

    public void showRoomDetails() {
        System.out.println("\n--- ДЕТАЛИ НОМЕРА ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        String details = controller.getRoomDetails(roomNumber);
        System.out.println("\n" + details);
    }

    // Методы управления гостями с проверкой ввода
    public void addServiceToGuest() {
        System.out.println("\n--- ДОБАВЛЕНИЕ УСЛУГИ ГОСТЮ ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты гостя: ", 1, 999);

        String serviceName = readNonEmptyString("Введите название услуги: ");

        if (controller.addServiceToGuest(roomNumber, serviceName)) {
            System.out.println("Услуга успешно добавлена гостю!");
        } else {
            System.out.println("Ошибка при добавлении услуги! Проверьте номер комнаты и название услуги.");
        }
    }

    public void showGuestServices() {
        System.out.println("\n--- УСЛУГИ ГОСТЯ ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты гостя: ", 1, 999);

        Guest guest = controller.getGuestByRoomNumber(roomNumber);
        if (guest != null) {
            List<RoomService> services = controller.getGuestServices(guest);
            System.out.println("\n--- УСЛУГИ ГОСТЯ " + guest.getName() + " ---");
            if (services.isEmpty()) {
                System.out.println("У гостя нет услуг");
            } else {
                services.forEach(service ->
                        System.out.println("- " + service.getService().getName() + " (" +
                                service.getPrice() + " руб.) - " + service.getDate()));
            }
        } else {
            System.out.println("Гость не найден в указанном номере!");
        }
    }

    public void showGuestDetails() {
        System.out.println("\n--- ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ГОСТЕ ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты гостя: ", 1, 999);

        Guest guest = controller.getGuestByRoomNumber(roomNumber);
        if (guest != null) {
            Room room = controller.findRoomByNumber(roomNumber);
            System.out.println("\n=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ГОСТЕ ===");
            System.out.println("Имя: " + guest.getName());
            System.out.println("Паспорт: " + guest.getPassportNumber());
            System.out.println("Телефон: " + guest.getPhoneNumber());
            System.out.println("Номер комнаты: " + roomNumber);

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
                List<RoomService> guestServices = controller.getGuestServices(guest);
                if (!guestServices.isEmpty()) {
                    System.out.println("\nУслуги гостя:");
                    guestServices.forEach(service ->
                            System.out.println("  - " + service.getService().getName() +
                                    " (" + service.getPrice() + " руб.) - " + service.getDate()));
                }
            }
        } else {
            System.out.println("Гость не найден в указанном номере!");
        }
    }

    public void findGuestByPassport() {
        System.out.println("\n--- ПОИСК ГОСТЯ ПО ПАСПОРТУ ---");
        String passport = readNonEmptyString("Введите номер паспорта: ");

        boolean found = false;
        for (Room room : controller.getRooms()) {
            if (room.getCurrentGuest() != null &&
                    room.getCurrentGuest().getPassportNumber().equals(passport)) {
                Guest guest = room.getCurrentGuest();
                System.out.println("\n=== НАЙДЕН ГОСТЬ ===");
                System.out.println("Имя: " + guest.getName());
                System.out.println("Паспорт: " + guest.getPassportNumber());
                System.out.println("Телефон: " + guest.getPhoneNumber());
                System.out.println("Номер комнаты: " + room.getNumber());
                System.out.println("Тип номера: " + room.getType());
                System.out.println("Дата выезда: " + room.getCheckOutDate());
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Гость с паспортом " + passport + " не найден!");
        }
    }

    // Методы управления услугами с проверкой ввода
    public void addServiceToRoom() {
        System.out.println("\n--- ДОБАВЛЕНИЕ УСЛУГИ К НОМЕРУ ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        String serviceName = readNonEmptyString("Введите название услуги: ");

        if (controller.addServiceToRoom(roomNumber, serviceName)) {
            System.out.println("Услуга успешно добавлена к номеру!");
        } else {
            System.out.println("Ошибка при добавлении услуги! Проверьте номер комнаты и название услуги.");
        }
    }

    public void changeServicePrice() {
        System.out.println("\n--- ИЗМЕНЕНИЕ ЦЕНЫ УСЛУГИ ---");
        String serviceName = readNonEmptyString("Введите название услуги: ");

        double price = readDoubleInputWithValidation("Введите новую цену: ", 1, 100000);

        if (controller.changeServicePrice(serviceName, price)) {
            System.out.println("Цена услуги успешно изменена!");
        } else {
            System.out.println("Ошибка при изменении цены! Проверьте название услуги.");
        }
    }

    // Методы отчетов
    public void showStatistics() {
        System.out.println("\n=== ОБЩАЯ СТАТИСТИКА ===");
        System.out.println("Свободных номеров: " + controller.getTotalAvailableRooms());
        System.out.println("Всего постояльцев: " + controller.getTotalGuests());
        System.out.println("Всего номеров: " + controller.getRooms().size());
        System.out.println("Всего услуг: " + controller.getServices().size());
    }

    public void showRoomPayment() {
        System.out.println("\n--- СУММА ОПЛАТЫ ЗА НОМЕР ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        double payment = controller.getRoomPayment(roomNumber);
        System.out.println("Сумма оплаты за номер " + roomNumber + ": " + payment + " руб.");
    }

    public void showRoomHistory() {
        System.out.println("\n--- ИСТОРИЯ ПРОЖИВАНИЙ НОМЕРА ---");
        int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

        List<StayHistory> history = controller.getLastThreeGuests(roomNumber);
        if (history.isEmpty()) {
            System.out.println("История проживаний отсутствует");
        } else {
            System.out.println("\n--- ИСТОРИЯ ПРОЖИВАНИЙ НОМЕРА " + roomNumber + " ---");
            history.forEach(System.out::println);
        }
    }

    // Методы поиска
    public void searchRoomsByDate() {
        System.out.println("\n--- ПОИСК НОМЕРОВ ПО ДАТЕ ---");
        String dateString = readNonEmptyString("Введите дату (гггг-мм-дд): ");

        try {
            LocalDate date = LocalDate.parse(dateString);
            List<Room> availableRooms = controller.getRoomsAvailableOnDate(date);
            System.out.println("\n--- НОМЕРА ДОСТУПНЫЕ " + date + " ---");
            if (availableRooms.isEmpty()) {
                System.out.println("Нет доступных номеров на указанную дату");
            } else {
                availableRooms.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("Неверный формат даты! Используйте гггг-мм-дд");
        }
    }

    // Улучшенные методы ввода с валидацией
    private int readIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Пожалуйста, введите число.");
                    continue;
                }
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат! Введите целое число.");
            }
        }
    }

    private int readIntInputWithValidation(String prompt, int min, int max) {
        while (true) {
            int value = readIntInput(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Значение должно быть от " + min + " до " + max);
        }
    }

    private double readDoubleInputWithValidation(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Пожалуйста, введите число.");
                    continue;
                }
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Значение должно быть от " + min + " до " + max);
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат! Введите число.");
            }
        }
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Это поле не может быть пустым. Пожалуйста, введите значение.");
        }
    }
}