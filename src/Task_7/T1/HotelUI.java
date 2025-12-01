package Task_7.T1;

import Task_7.T1.controller.HotelController;
import Task_7.T1.exceptions.*;
import Task_7.T1.model.*;
import Task_7.T1.factory.HotelMenuFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;


import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

import javax.management.ServiceNotFoundException;
import java.time.format.DateTimeParseException;

public class HotelUI {
    private static HotelUI instance;
    private HotelController controller;
    private HotelMenuFactory menuFactory;
    private NavigationManager navigator;
    private Scanner scanner;
    private boolean running;
    private Properties config;

    private HotelUI() {
        // Загружаем конфигурацию
        loadConfiguration();

        // Создаем контроллер с конфигурационными параметрами
        this.controller = new HotelController("Гранд Отель");
        if (config != null) {
            controller.loadConfiguration(config);
        }

        this.menuFactory = new HotelMenuFactory(this);
        this.navigator = NavigationManager.getInstance();
        this.scanner = new Scanner(System.in);
        this.running = true;
        initializeTestData();
        showConfigurationInfo();
    }

    private void loadConfiguration() {
        config = new Properties();
        try (FileInputStream fis = new FileInputStream("hotel.properties")) {
            config.load(fis);
            System.out.println("Конфигурация загружена из hotel.properties");
        } catch (IOException e) {
            // Используем значения по умолчанию
            config = new Properties();
            config.setProperty("room.status.change.enabled", "true");
            config.setProperty("room.history.size", "3");
            System.out.println("Файл конфигурации не найден, используются значения по умолчанию");
        }
    }

    private void showConfigurationInfo() {
        System.out.println("\n=== КОНФИГУРАЦИЯ ПРОГРАММЫ ===");
        System.out.println("Изменение статуса номеров: " +
                (controller.isAllowStatusChange() ? "ВКЛЮЧЕНО" : "ОТКЛЮЧЕНО"));
        System.out.println("Количество записей в истории номера: " +
                controller.getHistorySize());
        System.out.println("================================\n");
    }

    public static HotelUI getInstance() {
        if (instance == null) {
            instance = new HotelUI();
        }
        return instance;
    }

    // Добавляем метод для отображения конфигурации
    public void showConfiguration() {
        System.out.println("\n--- ТЕКУЩАЯ КОНФИГУРАЦИЯ ---");
        System.out.println("1. Изменение статуса номеров: " +
                (controller.isAllowStatusChange() ? "ВКЛЮЧЕНО" : "ОТКЛЮЧЕНО"));
        System.out.println("2. Количество записей в истории: " + controller.getHistorySize());
        System.out.println("0. Назад");

        int choice = readIntInputWithValidation("Выберите параметр для изменения (0-2): ", 0, 2);
        switch (choice) {
            case 0:
                return;
            case 1:
                toggleStatusChange();
                break;
            case 2:
                changeHistorySize();
                break;
        }
    }

    private void toggleStatusChange() {
        boolean current = controller.isAllowStatusChange();
        controller.setAllowStatusChange(!current);
        System.out.println("Изменение статуса номеров теперь " +
                (controller.isAllowStatusChange() ? "ВКЛЮЧЕНО" : "ОТКЛЮЧЕНО"));
    }

    private void changeHistorySize() {
        int size = readIntInputWithValidation("Введите новое количество записей в истории (1-20): ", 1, 20);
        controller.setHistorySize(size);
        System.out.println("Количество записей в истории изменено на " + size);
    }

    private void initializeTestData() {
        try {
            List<Room> testRooms = List.of(
                    new Room(1L, 101, "Стандарт", 2500, 2, 3),
                    new Room(2L, 102, "Стандарт", 2300, 2, 3),
                    new Room(3L, 201, "Люкс", 5000, 3, 4),
                    new Room(4L, 202, "Люкс", 5500, 4, 5),
                    new Room(5L, 301, "Президентский", 10000, 2, 5)
            );
            for (Room room : testRooms) {
                try {
                    controller.addRoom(room);
                } catch (ValidationException e) {
                    System.out.println("Ошибка при добавлении тестового номера " + room.getNumber() + ": " + e.getMessage());
                }
            }

            List<Service> testServices = List.of(
                    new Service(1L, "Завтрак", 500, "Шведский стол"),
                    new Service(2L, "SPA", 1500, "Посещение спа-комплекса"),
                    new Service(3L, "Трансфер", 800, "Трансфер из/в аэропорт"),
                    new Service(4L, "Прачечная", 300, "Стирка и глажка одежды")
            );
            for (Service service : testServices) {
                try {
                    controller.addService(service);
                } catch (ValidationException e) {
                    System.out.println("Ошибка при добавлении тестовой услуги " + service.getName() + ": " + e.getMessage());
                }
            }

            // Заселяем тестовых гостей
            try {
                Guest guest1 = new Guest(1L, "Иван Иванов", "1234567890", "+7-123-456-7890");
                Guest guest2 = new Guest(2L, "Петр Петров", "0987654321", "+7-987-654-3210");
                Guest guest3 = new Guest(3L, "Анна Сидорова", "1122334455", "+7-111-222-3333");

                controller.checkIn(101, guest1, 3);
                controller.checkIn(201, guest2, 5);

                // Добавляем услуги
                controller.addServiceToGuest(101, "Завтрак");
                controller.addServiceToGuest(201, "SPA");
                controller.addServiceToGuest(201, "Трансфер");

            } catch (HotelManagementException e) {
                System.out.println("Ошибка при заселении тестовых гостей: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Критическая ошибка при инициализации тестовых данных: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("=== СИСТЕМА УПРАВЛЕНИЯ ГОСТИНИЦЕЙ ===");
        System.out.println("Гостиница: " + controller.getHotelName());

        showMainMenu();
        startNavigation();
    }

    // Обновляем главное меню для добавления пункта конфигурации
    public void showMainMenu() {
        Menu mainMenu = new Menu("ГОСТИНИЦА - ГЛАВНОЕ МЕНЮ");

        mainMenu.addMenuItem(new MenuItem("Управление номерами", () -> showRoomManagementMenu()));
        mainMenu.addMenuItem(new MenuItem("Управление гостями", () -> showGuestManagementMenu()));
        mainMenu.addMenuItem(new MenuItem("Управление услугами", () -> showServiceManagementMenu()));
        mainMenu.addMenuItem(new MenuItem("Импорт/Экспорт данных", () -> showImportExportMenu()));
        mainMenu.addMenuItem(new MenuItem("Отчеты и статистика", () -> showReportsMenu()));
        mainMenu.addMenuItem(new MenuItem("Поиск и сортировка", () -> showSearchMenu()));
        mainMenu.addMenuItem(new MenuItem("Расширенная аналитика", () -> showAnalyticsMenu()));
        mainMenu.addMenuItem(new MenuItem("Конфигурация программы", () -> showConfiguration()));

        navigator.navigateTo(mainMenu, true);
    }

    private void startNavigation() {
        while (running) {
            try {
                Menu currentMenu = navigator.getCurrentMenu();
                if (currentMenu == null) {
                    stop();
                    break;
                }

                int choice = readIntInput("\nВыберите пункт меню: ");
                if (choice == 0) {
                    if (currentMenu.getTitle().equals("ГОСТИНИЦА - ГЛАВНОЕ МЕНЮ")) {
                        stop();
                        break;
                    } else {
                        navigator.navigateBack();
                        continue;
                    }
                }
                navigator.executeMenuItem(choice);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка ввода. Пожалуйста, введите число.");
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        System.out.println("\nПрограмма завершена. До свидания!");
        scanner.close();
    }

    // Методы импорта/экспорта с улучшенной обработкой ошибок
    public void importRoomsFromCSV() {
        try {
            System.out.println("\n--- ИМПОРТ НОМЕРОВ ИЗ CSV ---");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.importRoomsFromCSV(filename);
            System.out.println("Номера успешно импортированы из файла: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при импорте номеров: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void exportRoomsToCSV() {
        try {
            System.out.println("\n--- ЭКСПОРТ НОМЕРОВ В CSV ---");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.exportRoomsToCSV(filename);
            System.out.println("Номера успешно экспортированы в файл: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при экспорте номеров: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void importServicesFromCSV() {
        try {
            System.out.println("\n--- ИМПОРТ УСЛУГ ИЗ CSV ---");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.importServicesFromCSV(filename);
            System.out.println("Услуги успешно импортированы из файла: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при импорте услуг: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void exportServicesToCSV() {
        try {
            System.out.println("\n--- ЭКСПОРТ УСЛУГ В CSV ---");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.exportServicesToCSV(filename);
            System.out.println("Услуги успешно экспортированы в файл: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при экспорте услуг: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void importGuestsFromCSV() {
        try {
            System.out.println("\n--- ИМПОРТ ГОСТЕЙ ИЗ CSV ---");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.importGuestsFromCSV(filename);
            System.out.println("Гости успешно импортированы из файла: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при импорте гостей: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void exportGuestsToCSV() {
        try {
            System.out.println("\n--- ЭКСПОРТ ГОСТЕЙ В CSV ---");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.exportGuestsToCSV(filename);
            System.out.println("Гости успешно экспортированы в файл: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при экспорте гостей: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    // Обновленные методы с улучшенной обработкой исключений
    public void checkInGuest() {
        try {
            System.out.println("\n--- ЗАСЕЛЕНИЕ ГОСТЯ ---");

            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);
            String name = readNonEmptyString("Введите имя гостя: ");
            String passport = readNonEmptyString("Введите номер паспорта (10 цифр): ");
            String phone = readNonEmptyString("Введите телефон: ");
            int nights = readIntInputWithValidation("Введите количество ночей: ", 1, 365);

            // Проверка формата паспорта
            if (!passport.matches("\\d{10}")) {
                System.out.println("Неверный формат паспорта! Должно быть 10 цифр.");
                return;
            }

            Guest guest = new Guest(name, passport, phone);
            if (controller.checkIn(roomNumber, guest, nights)) {
                System.out.println("Гость " + name + " успешно заселен в номер " + roomNumber + " на " + nights + " ночей!");
            } else {
                System.out.println("Не удалось заселить гостя. Проверьте доступность номера.");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (RoomNotAvailableException e) {
            System.out.println("" + e.getMessage());
        } catch (GuestNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (ValidationException | InvalidDataException e) {
            System.out.println("Ошибка в данных: " + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при заселении: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void checkOutGuest() {
        try {
            System.out.println("\n--- ВЫСЕЛЕНИЕ ГОСТЯ ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

            if (controller.checkOut(roomNumber)) {
                System.out.println("Гость успешно выселен из номера " + roomNumber + "!");
            } else {
                System.out.println("Не удалось выселить гостя. Возможно, номер не занят.");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (RoomNotAvailableException e) {
            System.out.println("" + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при выселении: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void addServiceToGuest() {
        try {
            System.out.println("\n--- ДОБАВЛЕНИЕ УСЛУГИ ГОСТЮ ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты гостя: ", 1, 999);
            String serviceName = readNonEmptyString("Введите название услуги: ");

            if (controller.addServiceToGuest(roomNumber, serviceName)) {
                System.out.println("Услуга '" + serviceName + "' успешно добавлена гостю в номере " + roomNumber + "!");
            } else {
                System.out.println("Не удалось добавить услугу.");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (GuestNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при добавлении услуги: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void changeRoomPrice() {
        try {
            System.out.println("\n--- ИЗМЕНЕНИЕ ЦЕНЫ НОМЕРА ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);
            double price = readDoubleInputWithValidation("Введите новую цену: ", 1, 100000);

            if (controller.changeRoomPrice(roomNumber, price)) {
                System.out.println("Цена номера " + roomNumber + " успешно изменена на " + price + " руб.!");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (InvalidDataException e) {
            System.out.println("" + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при изменении цены: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    // Обновляем метод changeRoomStatus для учета конфигурации
    public void changeRoomStatus() {
        try {
            if (!controller.isAllowStatusChange()) {
                System.out.println("Изменение статуса номеров отключено в конфигурации программы!");
                return;
            }

            System.out.println("\n--- ИЗМЕНЕНИЕ СТАТУСА НОМЕРА ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

            System.out.println("Доступные статусы:");
            String[] statuses = RoomStatus.getAllDescriptions();
            for (int i = 0; i < statuses.length; i++) {
                System.out.println((i + 1) + ". " + statuses[i]);
            }

            int statusChoice = readIntInputWithValidation("Выберите статус (1-" + statuses.length + "): ", 1, statuses.length);
            String status = statuses[statusChoice - 1];

            if (controller.setRoomStatus(roomNumber, status)) {
                System.out.println("Статус номера " + roomNumber + " успешно изменен на '" + status + "'!");
            }
        } catch (HotelManagementException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void changeServicePrice() {
        try {
            System.out.println("\n--- ИЗМЕНЕНИЕ ЦЕНЫ УСЛУГИ ---");
            String serviceName = readNonEmptyString("Введите название услуги: ");
            double price = readDoubleInputWithValidation("Введите новую цену: ", 0, 100000);

            if (controller.changeServicePrice(serviceName, price)) {
                System.out.println("Цена услуги '" + serviceName + "' успешно изменена на " + price + " руб.!");
            }
        } catch (InvalidDataException e) {
            System.out.println("" + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при изменении цены услуги: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void addServiceToRoom() {
        try {
            System.out.println("\n--- ДОБАВЛЕНИЕ УСЛУГИ К НОМЕРУ ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);
            String serviceName = readNonEmptyString("Введите название услуги: ");

            if (controller.addServiceToRoom(roomNumber, serviceName)) {
                System.out.println("Услуга '" + serviceName + "' успешно добавлена к номеру " + roomNumber + "!");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при добавлении услуги к номеру: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void showGuestDetails() {
        try {
            System.out.println("\n--- ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ГОСТЕ ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты гостя: ", 1, 999);

            Guest guest = controller.getGuestByRoomNumber(roomNumber);
            if (guest != null) {
                Room room = controller.findRoomByNumber(roomNumber);
                displayGuestDetails(guest, room);
            } else {
                System.out.println("В номере " + roomNumber + " нет гостя");
            }
        } catch (HotelManagementException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    private void displayGuestDetails(Guest guest, Room room) throws HotelManagementException {
        System.out.println("\n=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ГОСТЕ ===");
        System.out.println("ID: " + guest.getId());
        System.out.println("Имя: " + guest.getName());
        System.out.println("Паспорт: " + guest.getPassportNumber());
        System.out.println("Телефон: " + guest.getPhoneNumber());

        if (room != null) {
            System.out.println("Номер комнаты: " + room.getNumber());
            System.out.println("Тип номера: " + room.getType());
            System.out.println("Дата заселения: " + room.getCheckInDate());
            System.out.println("Дата выезда: " + room.getCheckOutDate());
            System.out.println("Стоимость номера за ночь: " + room.getPricePerNight() + " руб.");

            if (room.getCheckInDate() != null) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(
                        room.getCheckInDate(), LocalDate.now());
                double total = room.calculateTotalPrice((int) Math.max(1, nights));
                System.out.println("Общая стоимость проживания: " + total + " руб.");
            }

            List<RoomService> guestServices = controller.getGuestServices(guest);
            if (!guestServices.isEmpty()) {
                System.out.println("\nУслуги гостя:");
                guestServices.forEach(service ->
                        System.out.println("  - " + service.getService().getName() +
                                " (" + service.getPrice() + " руб.) - " + service.getDate()));
            } else {
                System.out.println("\nУ гостя нет дополнительных услуг");
            }
        }
    }

    public void showGuestServices() {
        try {
            System.out.println("\n--- УСЛУГИ ГОСТЯ ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты гостя: ", 1, 999);

            Guest guest = controller.getGuestByRoomNumber(roomNumber);
            if (guest == null) {
                System.out.println("В номере " + roomNumber + " нет гостя");
                return;
            }

            List<RoomService> guestServices = controller.getGuestServices(guest);
            if (guestServices.isEmpty()) {
                System.out.println("У гостя " + guest.getName() + " нет дополнительных услуг");
            } else {
                System.out.println("\nУслуги гостя " + guest.getName() + ":");
                double total = 0;
                for (RoomService service : guestServices) {
                    System.out.println("  - " + service.getService().getName() +
                            " (" + service.getPrice() + " руб.) - " + service.getDate());
                    total += service.getPrice();
                }
                System.out.println("Общая стоимость услуг: " + total + " руб.");
            }
        } catch (GuestNotFoundException e) {
            System.out.println("" + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при получении услуг гостя: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void findGuestByPassport() {
        try {
            System.out.println("\n--- ПОИСК ГОСТЯ ПО ПАСПОРТУ ---");
            String passport = readNonEmptyString("Введите номер паспорта: ");

            controller.findGuestByPassport(passport)
                    .ifPresentOrElse(
                            entry -> {
                                Guest guest = entry.getKey();
                                Room room = entry.getValue();
                                System.out.println("\n=== НАЙДЕН ГОСТЬ ===");
                                System.out.println("ID: " + guest.getId());
                                System.out.println("Имя: " + guest.getName());
                                System.out.println("Паспорт: " + guest.getPassportNumber());
                                System.out.println("Телефон: " + guest.getPhoneNumber());
                                System.out.println("Номер комнаты: " + room.getNumber());
                                System.out.println("Тип номера: " + room.getType());
                                System.out.println("Дата заселения: " + room.getCheckInDate());
                                System.out.println("Дата выезда: " + room.getCheckOutDate());
                            },
                            () -> System.out.println("Гость с паспортом " + passport + " не найден!")
                    );
        } catch (IllegalArgumentException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void searchRoomsByDate() {
        try {
            System.out.println("\n--- ПОИСК НОМЕРОВ ПО ДАТЕ ---");
            String dateString = readNonEmptyString("Введите дату (гггг-мм-дд): ");

            LocalDate date = LocalDate.parse(dateString);
            List<Room> availableRooms = controller.getRoomsAvailableOnDate(date);
            System.out.println("\n--- НОМЕРА ДОСТУПНЫЕ " + date + " ---");
            if (availableRooms.isEmpty()) {
                System.out.println("Нет доступных номеров на указанную дату");
            } else {
                availableRooms.forEach(room ->
                        System.out.printf("№%d: %s, %d чел., %.2f руб./ночь\n",
                                room.getNumber(), room.getType(), room.getCapacity(), room.getPricePerNight()));
            }
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат даты! Используйте гггг-мм-дд");
        } catch (IllegalArgumentException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void findRoomsByCriteria() {
        try {
            System.out.println("\n--- ПОИСК НОМЕРОВ ПО КРИТЕРИЯМ ---");

            int minStars = readIntInputWithValidation("Минимальное количество звезд (1-5): ", 1, 5);
            int maxCapacity = readIntInputWithValidation("Максимальная вместимость (1-10): ", 1, 10);
            double maxPrice = readDoubleInputWithValidation("Максимальная цена за ночь: ", 1, 100000);

            List<Room> filteredRooms = controller.getRoomsByCriteria(minStars, maxCapacity, maxPrice);

            System.out.println("\n--- НАЙДЕННЫЕ НОМЕРА ---");
            if (filteredRooms.isEmpty()) {
                System.out.println("Номера по заданным критериям не найдены");
            } else {
                filteredRooms.forEach(room ->
                        System.out.printf("№%d: %s, %d звезд, %d чел., %.2f руб./ночь, статус: %s\n",
                                room.getNumber(), room.getType(), room.getStars(),
                                room.getCapacity(), room.getPricePerNight(), room.getStatus().getDescription()));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void showServicesByPriceRange() {
        try {
            System.out.println("\n--- УСЛУГИ ПО ЦЕНОВОМУ ДИАПАЗОНУ ---");

            double minPrice = readDoubleInputWithValidation("Минимальная цена: ", 0, 10000);
            double maxPrice = readDoubleInputWithValidation("Максимальная цена: ", minPrice, 10000);

            List<Service> servicesInRange = controller.getServicesByPriceRange(minPrice, maxPrice);

            System.out.println("\n--- УСЛУГИ В ДИАПАЗОНЕ " + minPrice + " - " + maxPrice + " РУБ. ---");
            if (servicesInRange.isEmpty()) {
                System.out.println("Услуги в заданном диапазоне не найдены");
            } else {
                servicesInRange.forEach(service ->
                        System.out.printf("- %s: %.2f руб. (%s)\n",
                                service.getName(), service.getPrice(), service.getDescription()));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    // Методы отображения данных
    public void displayAllRooms() {
        System.out.println("\n--- ВСЕ НОМЕРА ---");
        List<Room> rooms = controller.getRooms();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(room ->
                    System.out.printf("№%d: %s, %d чел., %d звезд, %.2f руб./ночь, статус: %s\n",
                            room.getNumber(), room.getType(), room.getCapacity(),
                            room.getStars(), room.getPricePerNight(), room.getStatus().getDescription()));
        }
    }

    public void displayAvailableRooms() {
        System.out.println("\n--- СВОБОДНЫЕ НОМЕРА ---");
        List<Room> availableRooms = controller.getRooms().stream()
                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
                .toList();
        if (availableRooms.isEmpty()) {
            System.out.println("Нет свободных номеров");
        } else {
            availableRooms.forEach(room ->
                    System.out.printf("№%d: %s, %d чел., %d звезд, %.2f руб./ночь\n",
                            room.getNumber(), room.getType(), room.getCapacity(),
                            room.getStars(), room.getPricePerNight()));
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
                System.out.printf("%s (паспорт: %s, тел: %s) - Номер %d, выезд: %s\n",
                        guest.getName(), guest.getPassportNumber(), guest.getPhoneNumber(),
                        room.getNumber(), room.getCheckOutDate());
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
                    System.out.printf("%s - Номер %d, выезд: %s\n",
                            entry.getKey().getName(), entry.getValue().getNumber(),
                            entry.getValue().getCheckOutDate()));
        }
    }

    public void displayGuestsSortedByCheckOutDate() {
        System.out.println("\n--- ПОСТОЯЛЬЦЫ (СОРТИРОВКА ПО ДАТЕ ВЫЕЗДА) ---");
        List<Map.Entry<Guest, Room>> guests = controller.getGuestsSortedByCheckOutDate();
        if (guests.isEmpty()) {
            System.out.println("Нет постояльцев");
        } else {
            guests.forEach(entry ->
                    System.out.printf("%s - Номер %d, выезд: %s\n",
                            entry.getKey().getName(), entry.getValue().getNumber(),
                            entry.getValue().getCheckOutDate()));
        }
    }

    public void displayAllServices() {
        System.out.println("\n--- ВСЕ УСЛУГИ ---");
        List<Service> services = controller.getServices();
        if (services.isEmpty()) {
            System.out.println("Нет услуг");
        } else {
            services.forEach(service ->
                    System.out.printf("%s: %.2f руб. - %s\n",
                            service.getName(), service.getPrice(), service.getDescription()));
        }
    }

    public void displayServicesSortedByPrice() {
        System.out.println("\n--- УСЛУГИ (СОРТИРОВКА ПО ЦЕНЕ) ---");
        List<Service> services = controller.getServicesSortedByPrice();
        if (services.isEmpty()) {
            System.out.println("Нет услуг");
        } else {
            services.forEach(service ->
                    System.out.printf("%s: %.2f руб. - %s\n",
                            service.getName(), service.getPrice(), service.getDescription()));
        }
    }

    public void displayRoomsSortedByPrice() {
        System.out.println("\n--- НОМЕРА (СОРТИРОВКА ПО ЦЕНЕ) ---");
        List<Room> rooms = controller.getRoomsSortedByPrice();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(room ->
                    System.out.printf("№%d: %s, %.2f руб./ночь, статус: %s\n",
                            room.getNumber(), room.getType(), room.getPricePerNight(),
                            room.getStatus().getDescription()));
        }
    }

    public void displayRoomsSortedByCapacity() {
        System.out.println("\n--- НОМЕРА (СОРТИРОВКА ПО ВМЕСТИМОСТИ) ---");
        List<Room> rooms = controller.getRoomsSortedByCapacity();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(room ->
                    System.out.printf("№%d: %s, %d чел., статус: %s\n",
                            room.getNumber(), room.getType(), room.getCapacity(),
                            room.getStatus().getDescription()));
        }
    }

    public void displayRoomsSortedByStars() {
        System.out.println("\n--- НОМЕРА (СОРТИРОВКА ПО ЗВЕЗДАМ) ---");
        List<Room> rooms = controller.getRoomsSortedByStars();
        if (rooms.isEmpty()) {
            System.out.println("Нет номеров");
        } else {
            rooms.forEach(room ->
                    System.out.printf("№%d: %s, %d звезд, статус: %s\n",
                            room.getNumber(), room.getType(), room.getStars(),
                            room.getStatus().getDescription()));
        }
    }

    public void displayAvailableRoomsSortedByPrice() {
        System.out.println("\n--- СВОБОДНЫЕ НОМЕРА (СОРТИРОВКА ПО ЦЕНЕ) ---");
        List<Room> rooms = controller.getAvailableRoomsSortedByPrice();
        if (rooms.isEmpty()) {
            System.out.println("Нет свободных номеров");
        } else {
            rooms.forEach(room ->
                    System.out.printf("№%d: %s, %d чел., %d звезд, %.2f руб./ночь\n",
                            room.getNumber(), room.getType(), room.getCapacity(),
                            room.getStars(), room.getPricePerNight()));
        }
    }

    // методы для аналитики
    public void showExtendedStatistics() {
        System.out.println("\n=== РАСШИРЕННАЯ СТАТИСТИКА ===");

        Map<String, Long> roomTypeStats = controller.getRoomTypeStatistics();
        System.out.println("\nРаспределение номеров по типам:");
        roomTypeStats.forEach((type, count) ->
                System.out.printf("  %s: %d номеров\n", type, count));

        Map<String, Long> popularServices = controller.getPopularServices();
        if (!popularServices.isEmpty()) {
            System.out.println("\nПопулярные услуги:");
            popularServices.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry ->
                            System.out.printf("  %s: %d раз\n", entry.getKey(), entry.getValue()));
        } else {
            System.out.println("\nНет данных об использовании услуг");
        }

        long budgetRooms = controller.getRooms().stream()
                .filter(room -> room.getPricePerNight() <= 3000)
                .count();
        long midRangeRooms = controller.getRooms().stream()
                .filter(room -> room.getPricePerNight() > 3000 && room.getPricePerNight() <= 7000)
                .count();
        long luxuryRooms = controller.getRooms().stream()
                .filter(room -> room.getPricePerNight() > 7000)
                .count();

        System.out.println("\nРаспределение номеров по ценовым категориям:");
        System.out.printf("  Бюджетные (до 3000 руб.): %d\n", budgetRooms);
        System.out.printf("  Средние (3000-7000 руб.): %d\n", midRangeRooms);
        System.out.printf("  Люкс (свыше 7000 руб.): %d\n", luxuryRooms);

        // Статистика по занятости
        long occupiedRooms = controller.getTotalGuests();
        long totalRooms = controller.getRooms().size();
        if (totalRooms > 0) {
            double occupancyRate = (double) occupiedRooms / totalRooms * 100;
            System.out.printf("\nТекущая загруженность: %.1f%% (%d/%d номеров)\n",
                    occupancyRate, occupiedRooms, totalRooms);
        }
    }

    public void displayGuestsGroupedByRoomType() {
        System.out.println("\n--- ПОСТОЯЛЬЦЫ ПО ТИПАМ НОМЕРОВ ---");

        Map<String, List<Map.Entry<Guest, Room>>> guestsByRoomType = controller.getGuestsWithRooms().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().getType()
                ));

        if (guestsByRoomType.isEmpty()) {
            System.out.println("Нет постояльцев");
        } else {
            guestsByRoomType.forEach((roomType, guests) -> {
                System.out.println("\n" + roomType + ":");
                guests.forEach(entry ->
                        System.out.println("  - " + entry.getKey().getName() + " (номер " +
                                entry.getValue().getNumber() + ", выезд: " +
                                entry.getValue().getCheckOutDate() + ")"));
            });
        }
    }

    // Методы отчетов
    public void showStatistics() {
        System.out.println("\n=== ОБЩАЯ СТАТИСТИКА ===");
        System.out.println("Свободных номеров: " + controller.getTotalAvailableRooms());
        System.out.println("Всего постояльцев: " + controller.getTotalGuests());
        System.out.println("Всего номеров: " + controller.getRooms().size());
        System.out.println("Всего услуг: " + controller.getServices().size());

        long totalGuestsHistory = controller.getRooms().stream()
                .mapToLong(room -> room.getStayHistory().size())
                .sum();
        System.out.println("Всего гостей за всю историю: " + totalGuestsHistory);
    }

    public void showRoomPayment() {
        try {
            System.out.println("\n--- СУММА ОПЛАТЫ ЗА НОМЕР ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

            double payment = controller.getRoomPayment(roomNumber);
            System.out.println("Сумма оплаты за номер " + roomNumber + ": " + payment + " руб.");
        } catch (HotelManagementException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    // Обновляем метод для отображения истории проживаний с учетом конфигурации
    public void showRoomHistory() {
        try {
            System.out.println("\n--- ИСТОРИЯ ПРОЖИВАНИЙ НОМЕРА (последние " + controller.getHistorySize() + " записей) ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

            List<StayHistory> history = controller.getLastThreeGuests(roomNumber);
            if (history.isEmpty()) {
                System.out.println("История проживаний отсутствует");
            } else {
                System.out.println("\n--- ИСТОРИЯ ПРОЖИВАНИЙ НОМЕРА " + roomNumber + " ---");
                history.forEach(h ->
                        System.out.printf("%s: с %s по %s\n",
                                h.getGuest().getName(), h.getCheckInDate(), h.getCheckOutDate()));
            }
        } catch (HotelManagementException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void showRoomDetails() {
        try {
            System.out.println("\n--- ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О НОМЕРЕ ---");
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

            String details = controller.getRoomDetails(roomNumber);
            System.out.println(details);
        } catch (HotelManagementException e) {
            System.out.println("" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    // Методы навигации по меню
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

    public void showAnalyticsMenu() {
        navigator.navigateTo(menuFactory.buildAnalyticsMenu(), false);
    }

    public void showImportExportMenu() {
        navigator.navigateTo(menuFactory.buildImportExportMenu(), false);
    }

    // Вспомогательные методы ввода
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