package T8.T1;

import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.Inject;
import T8.T1.annotations.PropertyType;
import T8.T1.controller.HotelController;
import T8.T1.exceptions.*;
import T8.T1.factory.HotelMenuFactory;
import T8.T1.model.*;
import config.ConfigurationManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotelUI {
    private static HotelUI instance;
    private HotelController controller;
    private HotelMenuFactory menuFactory;
    private NavigationManager navigator;
    private Scanner scanner;
    private boolean running;
    private ScheduledExecutorService scheduler;

    @ConfigProperty(propertyName = "ui.refresh.rate", type = PropertyType.INTEGER)
    private int refreshRate = 1000;

    @ConfigProperty(propertyName = "ui.date.format", type = PropertyType.STRING)
    private String dateFormat = "dd.MM.yyyy";

    @ConfigProperty(propertyName = "ui.show.warnings", type = PropertyType.BOOLEAN)
    private boolean showWarnings = true;

    @ConfigProperty(propertyName = "ui.auto.save", type = PropertyType.BOOLEAN)
    private boolean autoSave = true;

    @ConfigProperty(propertyName = "ui.auto.save.interval", type = PropertyType.INTEGER)
    private int autoSaveInterval = 300;

    @ConfigProperty(propertyName = "ui.max.input.attempts", type = PropertyType.INTEGER)
    private int maxInputAttempts = 3;

    private DateTimeFormatter dateFormatter;
    private static final String STATE_FILENAME = "hotel_state.ser";

    @Inject
    public HotelUI() {
        this.scanner = new Scanner(System.in, "UTF-8");
        this.running = true;

        // Конфигурируем UI через аннотации
        ConfigurationManager.configure(this);

        // Инициализируем форматтер даты после конфигурации
        this.dateFormatter = DateTimeFormatter.ofPattern(dateFormat);

        // Создаем контроллер через DI
        this.controller = ConfigurationManager.createInstance(HotelController.class);

        // Создаем фабрику меню
        this.menuFactory = new HotelMenuFactory(this);

        // Получаем NavigationManager (синглтон)
        this.navigator = NavigationManager.getInstance();

        // Инициализируем планировщик
        initScheduler();

        // Инициализируем тестовые данные
        initializeTestData();

        showConfigurationInfo();
    }

    private void initScheduler() {
        if (refreshRate > 0) {
            scheduler = Executors.newScheduledThreadPool(1);

            if (autoSave && autoSaveInterval > 0) {
                scheduler.scheduleAtFixedRate(() -> {
                    try {
                        controller.saveState(STATE_FILENAME);
                        if (showWarnings) {
                            System.out.println("\n[Автосохранение] Состояние сохранено");
                        }
                    } catch (Exception e) {
                        if (showWarnings) {
                            System.out.println("\n[Автосохранение] Ошибка: " + e.getMessage());
                        }
                    }
                }, autoSaveInterval, autoSaveInterval, TimeUnit.SECONDS);
            }
        }
    }

    private void showConfigurationInfo() {
        System.out.println("\n=== КОНФИГУРАЦИЯ ПРОГРАММЫ ===");
        System.out.println("Гостиница: " + controller.getHotelName());
        System.out.println("Изменение статуса номеров: " +
                (controller.isAllowStatusChange() ? "ВКЛЮЧЕНО" : "ОТКЛЮЧЕНО"));
        System.out.println("Количество записей в истории номера: " +
                controller.getHistorySize());
        System.out.println("Формат даты: " + dateFormat);
        System.out.println("Частота автообновления: " + refreshRate + " мс");
        System.out.println("Автосохранение: " + (autoSave ? "ВКЛ" : "ВЫКЛ"));
        if (autoSave) {
            System.out.println("Интервал автосохранения: " + autoSaveInterval + " сек");
        }
        System.out.println("Максимальное количество попыток ввода: " + maxInputAttempts);
        System.out.println("================================\n");
    }

    public static HotelUI getInstance() {
        if (instance == null) {
            instance = ConfigurationManager.createInstance(HotelUI.class);
        }
        return instance;
    }

    public void showConfiguration() {
        System.out.println("\n--- ТЕКУЩАЯ КОНФИГУРАЦИЯ ---");
        controller.printConfiguration();

        System.out.println("--- НАСТРОЙКИ ИНТЕРФЕЙСА ---");
        System.out.println("1. Частота обновления: " + refreshRate + " мс");
        System.out.println("2. Формат даты: " + dateFormat);
        System.out.println("3. Показ предупреждений: " + (showWarnings ? "ВКЛ" : "ВЫКЛ"));
        System.out.println("4. Автосохранение: " + (autoSave ? "ВКЛ" : "ВЫКЛ"));
        if (autoSave) {
            System.out.println("5. Интервал автосохранения: " + autoSaveInterval + " сек");
        }
        System.out.println("6. Сохранить состояние программы");
        System.out.println("7. Загрузить состояние программы");
        System.out.println("8. Создать резервную копию");
        System.out.println("9. Обновить конфигурацию из файла");
        System.out.println("0. Назад");

        int choice = readIntInputWithValidation("Выберите параметр для изменения (0-9): ", 0, 9);
        switch (choice) {
            case 0:
                return;
            case 1:
                changeRefreshRate();
                break;
            case 2:
                changeDateFormat();
                break;
            case 3:
                toggleWarnings();
                break;
            case 4:
                toggleAutoSave();
                break;
            case 5:
                if (autoSave) {
                    changeAutoSaveInterval();
                }
                break;
            case 6:
                saveState();
                break;
            case 7:
                loadState();
                break;
            case 8:
                createBackup();
                break;
            case 9:
                reloadConfiguration();
                break;
        }

        showConfiguration();
    }

    private void changeRefreshRate() {
        int rate = readIntInputWithValidation("Введите частоту обновления в мс (0-10000): ", 0, 10000);
        this.refreshRate = rate;
        System.out.println("Частота обновления изменена на " + rate + " мс");

        if (scheduler != null) {
            scheduler.shutdown();
        }
        initScheduler();
    }

    private void changeDateFormat() {
        System.out.println("Примеры форматов:");
        System.out.println("dd.MM.yyyy - 31.12.2023");
        System.out.println("yyyy-MM-dd - 2023-12-31");
        System.out.println("MM/dd/yyyy - 12/31/2023");

        String format = readNonEmptyString("Введите новый формат даты: ");
        try {
            this.dateFormatter = DateTimeFormatter.ofPattern(format);
            this.dateFormat = format;
            System.out.println("Формат даты изменен на: " + format);
        } catch (Exception e) {
            System.out.println("Неверный формат даты: " + e.getMessage());
        }
    }

    private void toggleWarnings() {
        this.showWarnings = !this.showWarnings;
        System.out.println("Показ предупреждений теперь " + (showWarnings ? "ВКЛЮЧЕН" : "ОТКЛЮЧЕН"));
    }

    private void toggleAutoSave() {
        this.autoSave = !this.autoSave;
        System.out.println("Автосохранение теперь " + (autoSave ? "ВКЛЮЧЕНО" : "ОТКЛЮЧЕНО"));

        if (scheduler != null) {
            scheduler.shutdown();
        }
        initScheduler();
    }

    private void changeAutoSaveInterval() {
        int interval = readIntInputWithValidation("Введите интервал автосохранения в секундах (10-3600): ", 10, 3600);
        this.autoSaveInterval = interval;
        System.out.println("Интервал автосохранения изменен на " + interval + " сек");

        if (scheduler != null) {
            scheduler.shutdown();
        }
        initScheduler();
    }

    private void saveState() {
        try {
            String filename = readNonEmptyString("Введите имя файла (по умолчанию " + STATE_FILENAME + "): ");
            if (filename.trim().isEmpty()) {
                filename = STATE_FILENAME;
            }
            controller.saveState(filename);
            System.out.println("Состояние программы сохранено в файл: " + filename);
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при сохранении состояния: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    private void loadState() {
        try {
            String filename = readNonEmptyString("Введите имя файла (по умолчанию " + STATE_FILENAME + "): ");
            if (filename.trim().isEmpty()) {
                filename = STATE_FILENAME;
            }
            HotelController newController = HotelController.loadState(filename);

            ConfigurationManager.configure(newController);
            this.controller = newController;

            System.out.println("Состояние программы загружено из файла: " + filename);
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при загрузке состояния: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    private void createBackup() {
        controller.backupData();
    }

    private void reloadConfiguration() {
        ConfigurationManager.reloadConfiguration();
        ConfigurationManager.configure(this);
        ConfigurationManager.configure(controller);
        System.out.println("Конфигурация обновлена из файла");
        showConfigurationInfo();
    }

    private void initializeTestData() {
        try {
            if (controller.getRooms().isEmpty()) {
                System.out.println("Инициализация тестовых данных...");

                try {
                    // Получаем поддерживаемые типы номеров из контроллера
                    String[] supportedTypes = controller.getSupportedRoomTypes();

                    if (supportedTypes == null || supportedTypes.length == 0) {
                        System.out.println("Предупреждение: поддерживаемые типы номеров не заданы в конфигурации");
                        supportedTypes = new String[]{"Стандарт", "Люкс"};
                    }

                    System.out.println("Поддерживаемые типы номеров: " + String.join(", ", supportedTypes));

                    // Используем первый поддерживаемый тип
                    String roomType = supportedTypes[0];

                    Room room1 = new Room(101, roomType, 2500, 2, 3);
                    Room room2 = new Room(102, roomType, 2300, 2, 3);

                    // Используем второй тип если есть, иначе тот же
                    String secondType = supportedTypes.length > 1 ? supportedTypes[1] : roomType;
                    Room room3 = new Room(201, secondType, 5000, 3, 4);

                    controller.addRoom(room1);
                    controller.addRoom(room2);
                    controller.addRoom(room3);

                    System.out.println("Добавлено 3 тестовых номера");

                    Service service1 = new Service("Завтрак", 500, "Шведский стол");
                    Service service2 = new Service("SPA", 1500, "Посещение спа-комплекса");

                    controller.addService(service1);
                    controller.addService(service2);

                    System.out.println("Добавлено 2 тестовых услуги");

                    // Заселяем гостя только если есть свободные номера
                    if (controller.getTotalAvailableRooms() > 0) {
                        Guest guest = new Guest("Иван Иванов", "1234567890", "+7-123-456-7890");
                        controller.checkIn(101, guest, 3);
                        System.out.println("Заселен тестовый гость");
                    }

                    System.out.println("✓ Тестовые данные созданы успешно");

                } catch (Exception e) {
                    System.out.println("⚠ Часть тестовых данных не создана: " + e.getMessage());
                    if (showWarnings) {
                        e.printStackTrace();
                    }
                    System.out.println("Программа продолжит работу с существующими данными");
                }
            } else {
                System.out.println("Тестовые данные уже инициализированы");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при инициализации тестовых данных: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("=== СИСТЕМА УПРАВЛЕНИЯ ГОСТИНИЦЕЙ ===");
        System.out.println("Гостиница: " + controller.getHotelName());
        System.out.println("Версия с Dependency Injection и аннотациями");

        showMainMenu();
        startNavigation();
    }

    public void showMainMenu() {
        navigator.navigateTo(menuFactory.buildMainMenu(), true);
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
                if (showWarnings) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        try {
            controller.saveState(STATE_FILENAME);
            System.out.println("Состояние программы сохранено в " + STATE_FILENAME);
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при сохранении состояния: " + e.getMessage());
        }

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }

        running = false;
        System.out.println("\nПрограмма завершена. Состояние сохранено. До свидания!");
        scanner.close();

        ConfigurationManager.printConfigurationReport();
    }

    // Методы импорта/экспорта
    public void importRoomsFromCSV() {
        try {
            System.out.println("\n--- ИМПОРТ НОМЕРОВ ИЗ CSV ---");
            System.out.println("Используется разделитель: '" + controller.getCsvDelimiter() + "'");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.importRoomsFromCSV(filename);
            System.out.println("Номера успешно импортированы из файла: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при импорте номеров: " + e.getMessage());
            if (e.getCause() != null && showWarnings) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void exportRoomsToCSV() {
        try {
            System.out.println("\n--- ЭКСПОРТ НОМЕРОВ В CSV ---");
            System.out.println("Используется разделитель: '" + controller.getCsvDelimiter() + "'");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.exportRoomsToCSV(filename);
            System.out.println("Номера успешно экспортированы в файл: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при экспорте номеров: " + e.getMessage());
            if (e.getCause() != null && showWarnings) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void importServicesFromCSV() {
        try {
            System.out.println("\n--- ИМПОРТ УСЛУГ ИЗ CSV ---");
            System.out.println("Используется разделитель: '" + controller.getCsvDelimiter() + "'");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.importServicesFromCSV(filename);
            System.out.println("Услуги успешно импортированы из файла: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при импорте услуг: " + e.getMessage());
            if (e.getCause() != null && showWarnings) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void exportServicesToCSV() {
        try {
            System.out.println("\n--- ЭКСПОРТ УСЛУГ В CSV ---");
            System.out.println("Используется разделитель: '" + controller.getCsvDelimiter() + "'");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.exportServicesToCSV(filename);
            System.out.println("Услуги успешно экспортированы в файл: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при экспорте услуг: " + e.getMessage());
            if (e.getCause() != null && showWarnings) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void importGuestsFromCSV() {
        try {
            System.out.println("\n--- ИМПОРТ ГОСТЕЙ ИЗ CSV ---");
            System.out.println("Используется разделитель: '" + controller.getCsvDelimiter() + "'");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.importGuestsFromCSV(filename);
            System.out.println("Гости успешно импортированы из файла: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при импорте гостей: " + e.getMessage());
            if (e.getCause() != null && showWarnings) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void exportGuestsToCSV() {
        try {
            System.out.println("\n--- ЭКСПОРТ ГОСТЕЙ В CSV ---");
            System.out.println("Используется разделитель: '" + controller.getCsvDelimiter() + "'");
            String filename = readNonEmptyString("Введите имя файла: ");
            controller.exportGuestsToCSV(filename);
            System.out.println("Гости успешно экспортированы в файл: " + filename);
        } catch (ImportExportException e) {
            System.out.println("Ошибка при экспорте гостей: " + e.getMessage());
            if (e.getCause() != null && showWarnings) {
                System.out.println("Причина: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    // Бизнес-методы
    public void checkInGuest() {
        try {
            System.out.println("\n--- ЗАСЕЛЕНИЕ ГОСТЯ ---");
            System.out.println("Максимальное количество гостей в номере: " + controller.getMaxGuestsPerRoom());

            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);
            String name = readNonEmptyString("Введите имя гостя: ");
            String passport = readNonEmptyString("Введите номер паспорта (10 цифр): ");
            String phone = readNonEmptyString("Введите телефон: ");
            int nights = readIntInputWithValidation("Введите количество ночей: ", 1, 365);

            if (!passport.matches("\\d{10}")) {
                System.out.println("Неверный формат паспорта! Должно быть 10 цифр.");
                return;
            }

            Guest guest = new Guest(name, passport, phone);
            if (controller.checkIn(roomNumber, guest, nights)) {
                System.out.println("Гость " + name + " успешно заселен в номер " + roomNumber + " на " + nights + " ночей!");
                System.out.println("Время заселения: " + controller.getDefaultCheckInTime());
            } else {
                System.out.println("Не удалось заселить гостя. Проверьте доступность номера.");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (RoomNotAvailableException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (GuestNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
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
            System.out.println("Время выезда: " + controller.getDefaultCheckOutTime());
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);

            if (controller.checkOut(roomNumber)) {
                System.out.println("Гость успешно выселен из номера " + roomNumber + "!");
            } else {
                System.out.println("Не удалось выселить гостя. Возможно, номер не занят.");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (RoomNotAvailableException e) {
            System.out.println("Ошибка: " + e.getMessage());
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
            System.out.println("Ошибка: " + e.getMessage());
        } catch (GuestNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при добавлении услуги: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void changeRoomPrice() {
        try {
            System.out.println("\n--- ИЗМЕНЕНИЕ ЦЕНЫ НОМЕРА ---");
            System.out.println("Допустимый диапазон цен: от " + controller.getMinRoomPrice() +
                    " до " + controller.getMaxRoomPrice());
            int roomNumber = readIntInputWithValidation("Введите номер комнаты: ", 1, 999);
            double price = readDoubleInputWithValidation("Введите новую цену: ",
                    controller.getMinRoomPrice(), controller.getMaxRoomPrice());

            if (controller.changeRoomPrice(roomNumber, price)) {
                System.out.println("Цена номера " + roomNumber + " успешно изменена на " + price + " руб.!");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (InvalidDataException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (HotelManagementException e) {
            System.out.println("Ошибка при изменении цены: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

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
            System.out.println("Ошибка: " + e.getMessage());
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
            System.out.println("Ошибка: " + e.getMessage());
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
            System.out.println("Ошибка: " + e.getMessage());
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
            System.out.println("Дата заселения: " +
                    (room.getCheckInDate() != null ? dateFormatter.format(room.getCheckInDate()) : "не задана"));
            System.out.println("Дата выезда: " +
                    (room.getCheckOutDate() != null ? dateFormatter.format(room.getCheckOutDate()) : "не задана"));
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
                                " (" + service.getPrice() + " руб.) - " +
                                dateFormatter.format(service.getDate())));
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
                            " (" + service.getPrice() + " руб.) - " +
                            dateFormatter.format(service.getDate()));
                    total += service.getPrice();
                }
                System.out.println("Общая стоимость услуг: " + total + " руб.");
            }
        } catch (GuestNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
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
                                System.out.println("Дата заселения: " +
                                        (room.getCheckInDate() != null ? dateFormatter.format(room.getCheckInDate()) : "не задана"));
                                System.out.println("Дата выезда: " +
                                        (room.getCheckOutDate() != null ? dateFormatter.format(room.getCheckOutDate()) : "не задана"));
                            },
                            () -> System.out.println("Гость с паспортом " + passport + " не найден!")
                    );
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void searchRoomsByDate() {
        try {
            System.out.println("\n--- ПОИСК НОМЕРОВ ПО ДАТЕ ---");
            System.out.println("Формат даты: " + dateFormat);
            String dateString = readNonEmptyString("Введите дату (" + dateFormat + "): ");

            LocalDate date = LocalDate.parse(dateString, dateFormatter);
            List<Room> availableRooms = controller.getRoomsAvailableOnDate(date);
            System.out.println("\n--- НОМЕРА ДОСТУПНЫЕ " + dateFormatter.format(date) + " ---");
            if (availableRooms.isEmpty()) {
                System.out.println("Нет доступных номеров на указанную дату");
            } else {
                availableRooms.forEach(room ->
                        System.out.printf("№%d: %s, %d чел., %.2f руб./ночь\n",
                                room.getNumber(), room.getType(), room.getCapacity(), room.getPricePerNight()));
            }
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат даты! Используйте " + dateFormat);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

    public void findRoomsByCriteria() {
        try {
            System.out.println("\n--- ПОИСК НОМЕРОВ ПО КРИТЕРИЯМ ---");
            System.out.println("Поддерживаемые типы номеров: " +
                    String.join(", ", controller.getSupportedRoomTypes()));

            int minStars = readIntInputWithValidation("Минимальное количество звезд (1-5): ", 1, 5);
            int maxCapacity = readIntInputWithValidation("Максимальная вместимость (1-" +
                    controller.getMaxGuestsPerRoom() + "): ", 1, controller.getMaxGuestsPerRoom());
            double maxPrice = readDoubleInputWithValidation("Максимальная цена за ночь (до " +
                    controller.getMaxRoomPrice() + "): ", controller.getMinRoomPrice(), controller.getMaxRoomPrice());

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
            System.out.println("Ошибка: " + e.getMessage());
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
            System.out.println("Ошибка: " + e.getMessage());
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
                        room.getNumber(), dateFormatter.format(room.getCheckOutDate()));
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
                            dateFormatter.format(entry.getValue().getCheckOutDate())));
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
                            dateFormatter.format(entry.getValue().getCheckOutDate())));
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
                .collect(java.util.stream.Collectors.groupingBy(
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
                                dateFormatter.format(entry.getValue().getCheckOutDate()) + ")"));
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
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        }
    }

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
                                h.getGuest().getName(),
                                dateFormatter.format(h.getCheckInDate()),
                                dateFormatter.format(h.getCheckOutDate())));
            }
        } catch (HotelManagementException e) {
            System.out.println("Ошибка: " + e.getMessage());
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
            System.out.println("Ошибка: " + e.getMessage());
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
        int attempts = 0;
        while (attempts < maxInputAttempts) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Пожалуйста, введите число.");
                    attempts++;
                    continue;
                }
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                attempts++;
                if (attempts < maxInputAttempts) {
                    System.out.println("Неверный формат! Введите целое число. Попыток осталось: " +
                            (maxInputAttempts - attempts));
                } else {
                    System.out.println("Превышено максимальное количество попыток. Возврат в меню.");
                    return -1;
                }
            }
        }
        return -1;
    }

    private int readIntInputWithValidation(String prompt, int min, int max) {
        while (true) {
            int value = readIntInput(prompt);
            if (value == -1) {
                return min;
            }
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Значение должно быть от " + min + " до " + max);
        }
    }

    private double readDoubleInputWithValidation(String prompt, double min, double max) {
        int attempts = 0;
        while (attempts < maxInputAttempts) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Пожалуйста, введите число.");
                    attempts++;
                    continue;
                }
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Значение должно быть от " + min + " до " + max);
                attempts++;
            } catch (NumberFormatException e) {
                attempts++;
                if (attempts < maxInputAttempts) {
                    System.out.println("Неверный формат! Введите число. Попыток осталось: " +
                            (maxInputAttempts - attempts));
                } else {
                    System.out.println("Превышено максимальное количество попыток. Возврат в меню.");
                    return min;
                }
            }
        }
        return min;
    }

    private String readNonEmptyString(String prompt) {
        int attempts = 0;
        while (attempts < maxInputAttempts) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            attempts++;
            if (attempts < maxInputAttempts) {
                System.out.println("Это поле не может быть пустым. Пожалуйста, введите значение. Попыток осталось: " +
                        (maxInputAttempts - attempts));
            } else {
                System.out.println("Превышено максимальное количество попыток. Возврат в меню.");
                return "";
            }
        }
        return "";
    }

    // Геттеры для конфигурации UI
    public int getRefreshRate() { return refreshRate; }
    public String getDateFormat() { return dateFormat; }
    public boolean isShowWarnings() { return showWarnings; }
    public boolean isAutoSave() { return autoSave; }
    public int getAutoSaveInterval() { return autoSaveInterval; }

    // Новые методы для HotelMenuFactory
    public void saveAllData() {
        try {
            controller.saveState("full_backup.ser");
            System.out.println("Все данные сохранены");
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public void loadAllData() {
        try {
            HotelController newController = HotelController.loadState("full_backup.ser");
            ConfigurationManager.configure(newController);
            this.controller = newController;
            System.out.println("Все данные загружены");
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    public void showOccupancyByDate() {
        System.out.println("\n--- ЗАНЯТОСТЬ ПО ДНЯМ ---");
        System.out.println("Функция в разработке...");
    }

    public void showRoomProfitability() {
        System.out.println("\n--- ДОХОДНОСТЬ НОМЕРОВ ---");
        System.out.println("Функция в разработке...");
    }

    public void showPopularServicesRanking() {
        System.out.println("\n--- РЕЙТИНГ ПОПУЛЯРНЫХ УСЛУГ ---");
        System.out.println("Функция в разработке...");
    }

    public void exportAnalyticsReports() {
        System.out.println("\n--- ЭКСПОРТ ОТЧЕТОВ ---");
        System.out.println("Функция в разработке...");
    }
}