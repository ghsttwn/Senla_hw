package T8.T1.controller;

import T8.T1.CSVImportExport;
import T8.T1.exceptions.*;
import T8.T1.model.*;
import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;
import T8.T1.annotations.Inject;

import java.time.LocalDate;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class HotelController implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Room> rooms;
    private List<Service> services;
    private List<Guest> guests;

    @ConfigProperty(propertyName = "hotel.name", type = PropertyType.STRING)
    private String hotelName = "Гранд Отель";

    @ConfigProperty(propertyName = "room.status.change.enabled", type = PropertyType.BOOLEAN)
    private boolean allowStatusChange = true;

    @ConfigProperty(propertyName = "room.history.size", type = PropertyType.INTEGER)
    private int historySize = 3;

    @ConfigProperty(propertyName = "hotel.default.rooms", type = PropertyType.INTEGER)
    private int defaultRooms = 10;

    @ConfigProperty(propertyName = "hotel.default.services", type = PropertyType.INTEGER)
    private int defaultServices = 5;

    @ConfigProperty(propertyName = "csv.delimiter", type = PropertyType.STRING)
    private String csvDelimiter = ",";

    @ConfigProperty(propertyName = "export.enabled", type = PropertyType.BOOLEAN)
    private boolean exportEnabled = true;

    @ConfigProperty(propertyName = "backup.path", type = PropertyType.STRING)
    private String backupPath = "./backups/";

    @ConfigProperty(propertyName = "supported.room.types", type = PropertyType.STRING_ARRAY)
    private String[] supportedRoomTypes = {"Стандарт", "Люкс", "Президентский"};

    @ConfigProperty(propertyName = "max.guests.per.room", type = PropertyType.INTEGER)
    private int maxGuestsPerRoom = 4;

    @ConfigProperty(propertyName = "room.min.price", type = PropertyType.DOUBLE)
    private double minRoomPrice = 1000.0;

    @ConfigProperty(propertyName = "room.max.price", type = PropertyType.DOUBLE)
    private double maxRoomPrice = 50000.0;

    @ConfigProperty(propertyName = "default.checkin.time", type = PropertyType.STRING)
    private String defaultCheckInTime = "14:00";

    @ConfigProperty(propertyName = "default.checkout.time", type = PropertyType.STRING)
    private String defaultCheckOutTime = "12:00";

    private transient final Comparator<Room> roomPriceComparator = Comparator.comparingDouble(Room::getPricePerNight);
    private transient final Comparator<Room> roomCapacityComparator = Comparator.comparingInt(Room::getCapacity);
    private transient final Comparator<Room> roomStarsComparator = Comparator.comparingInt(Room::getStars);
    private transient final Comparator<Service> servicePriceComparator = Comparator.comparingDouble(Service::getPrice);
    private transient final Comparator<Map.Entry<Guest, Room>> guestCheckOutDateComparator =
            Comparator.comparing(entry -> entry.getValue().getCheckOutDate());

    @Inject
    public HotelController() {
        this.rooms = new ArrayList<>();
        this.services = new ArrayList<>();
        this.guests = new ArrayList<>();
        System.out.println("HotelController создан через DI");
    }

    // Методы для доступа к конфигурации
    public boolean isAllowStatusChange() {
        return allowStatusChange;
    }

    public void setAllowStatusChange(boolean allowStatusChange) {
        this.allowStatusChange = allowStatusChange;
    }

    public int getHistorySize() {
        return historySize;
    }

    public void setHistorySize(int historySize) {
        if (historySize < 1) {
            throw new IllegalArgumentException("Размер истории должен быть положительным числом");
        }
        this.historySize = historySize;
    }

    // Метод для загрузки конфигурации из property-файла (оставлен для обратной совместимости)
    public void loadConfiguration(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties не может быть null");
        }

        String allowStatusChangeStr = properties.getProperty("room.status.change.enabled", "true");
        this.allowStatusChange = Boolean.parseBoolean(allowStatusChangeStr);

        String historySizeStr = properties.getProperty("room.history.size", "3");
        try {
            this.historySize = Integer.parseInt(historySizeStr);
            if (this.historySize < 1) {
                this.historySize = 3;
            }
        } catch (NumberFormatException e) {
            this.historySize = 3;
        }

        this.hotelName = properties.getProperty("hotel.name", "Гранд Отель");
        this.defaultRooms = Integer.parseInt(properties.getProperty("hotel.default.rooms", "10"));
        this.defaultServices = Integer.parseInt(properties.getProperty("hotel.default.services", "5"));
        this.csvDelimiter = properties.getProperty("csv.delimiter", ",");
        this.exportEnabled = Boolean.parseBoolean(properties.getProperty("export.enabled", "true"));
        this.backupPath = properties.getProperty("backup.path", "./backups/");

        String roomTypes = properties.getProperty("supported.room.types", "Стандарт,Люкс,Президентский");
        this.supportedRoomTypes = roomTypes.split(",");

        this.maxGuestsPerRoom = Integer.parseInt(properties.getProperty("max.guests.per.room", "4"));
        this.minRoomPrice = Double.parseDouble(properties.getProperty("room.min.price", "1000"));
        this.maxRoomPrice = Double.parseDouble(properties.getProperty("room.max.price", "50000"));
        this.defaultCheckInTime = properties.getProperty("default.checkin.time", "14:00");
        this.defaultCheckOutTime = properties.getProperty("default.checkout.time", "12:00");
    }

    // Методы для сохранения и загрузки состояния
    public void saveState(String filename) throws HotelManagementException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("Состояние программы сохранено в файл: " + filename);
        } catch (IOException e) {
            throw new HotelManagementException("Ошибка при сохранении состояния: " + e.getMessage(), e);
        }
    }

    public static HotelController loadState(String filename) throws HotelManagementException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            HotelController controller = (HotelController) ois.readObject();
            System.out.println("Состояние программы загружено из файла: " + filename);
            return controller;
        } catch (FileNotFoundException e) {
            throw new HotelManagementException("Файл состояния не найден: " + filename, e);
        } catch (IOException e) {
            throw new HotelManagementException("Ошибка чтения файла состояния: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new HotelManagementException("Неверный формат файла состояния: " + e.getMessage(), e);
        }
    }

    public String getHotelName() {
        return hotelName != null ? hotelName : "Неизвестный отель";
    }

    public void setHotelName(String hotelName) {
        if (hotelName == null || hotelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Название отеля не может быть пустым");
        }
        this.hotelName = hotelName;
    }

    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }
    public List<Service> getServices() { return Collections.unmodifiableList(services); }
    public List<Guest> getGuests() { return Collections.unmodifiableList(guests); }

    public String getCsvDelimiter() { return csvDelimiter; }
    public boolean isExportEnabled() { return exportEnabled; }
    public String getBackupPath() { return backupPath; }
    public String[] getSupportedRoomTypes() {
        return supportedRoomTypes != null ? supportedRoomTypes : new String[0];
    }

    public String getSupportedRoomTypesAsString() {
        if (supportedRoomTypes == null || supportedRoomTypes.length == 0) {
            return "не заданы";
        }
        return String.join(", ", supportedRoomTypes);
    }

    public int getMaxGuestsPerRoom() { return maxGuestsPerRoom; }
    public double getMinRoomPrice() { return minRoomPrice; }
    public double getMaxRoomPrice() { return maxRoomPrice; }
    public String getDefaultCheckInTime() { return defaultCheckInTime; }
    public String getDefaultCheckOutTime() { return defaultCheckOutTime; }

    // Методы импорта/экспорта
    public void importRoomsFromCSV(String filename) throws ImportExportException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        try {
            CSVImportExport<Room> csvHandler = new CSVImportExport<>(Room.class, csvDelimiter);
            List<Room> importedRooms = csvHandler.importFromFile(filename);

            if (importedRooms.isEmpty()) {
                throw new ImportExportException("Файл '" + filename + "' не содержит данных о номерах");
            }

            for (Room importedRoom : importedRooms) {
                validateRoomData(importedRoom);
                Optional<Room> existingRoom = findRoomById(importedRoom.getId());
                if (existingRoom.isPresent()) {
                    updateExistingRoom(existingRoom.get(), importedRoom);
                } else {
                    addNewRoom(importedRoom);
                }
            }

            System.out.println("Импортировано " + importedRooms.size() + " номеров");
        } catch (ImportExportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportExportException("импорте номеров", filename, e);
        }
    }

    public void exportRoomsToCSV(String filename) throws ImportExportException {
        if (!exportEnabled) {
            throw new ImportExportException("Экспорт отключен в настройках");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        if (rooms.isEmpty()) {
            throw new ImportExportException("Нет данных о номерах для экспорта");
        }

        try {
            CSVImportExport<Room> csvHandler = new CSVImportExport<>(Room.class, csvDelimiter);
            csvHandler.exportToFile(rooms, filename);
            System.out.println("Экспортировано " + rooms.size() + " номеров");
        } catch (ImportExportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportExportException("экспорте номеров", filename, e);
        }
    }

    public void importServicesFromCSV(String filename) throws ImportExportException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        try {
            CSVImportExport<Service> csvHandler = new CSVImportExport<>(Service.class, csvDelimiter);
            List<Service> importedServices = csvHandler.importFromFile(filename);

            if (importedServices.isEmpty()) {
                throw new ImportExportException("Файл '" + filename + "' не содержит данных об услугах");
            }

            for (Service importedService : importedServices) {
                validateServiceData(importedService);
                Optional<Service> existingService = findServiceById(importedService.getId());
                if (existingService.isPresent()) {
                    updateExistingService(existingService.get(), importedService);
                } else {
                    addNewService(importedService);
                }
            }

            System.out.println("Импортировано " + importedServices.size() + " услуг");
        } catch (ImportExportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportExportException("импорте услуг", filename, e);
        }
    }

    public void exportServicesToCSV(String filename) throws ImportExportException {
        if (!exportEnabled) {
            throw new ImportExportException("Экспорт отключен в настройках");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        if (services.isEmpty()) {
            throw new ImportExportException("Нет данных об услугах для экспорта");
        }

        try {
            CSVImportExport<Service> csvHandler = new CSVImportExport<>(Service.class, csvDelimiter);
            csvHandler.exportToFile(services, filename);
            System.out.println("Экспортировано " + services.size() + " услуг");
        } catch (ImportExportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportExportException("экспорте услуг", filename, e);
        }
    }

    public void importGuestsFromCSV(String filename) throws ImportExportException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        try {
            CSVImportExport<Guest> csvHandler = new CSVImportExport<>(Guest.class, csvDelimiter);
            List<Guest> importedGuests = csvHandler.importFromFile(filename);

            if (importedGuests.isEmpty()) {
                throw new ImportExportException("Файл '" + filename + "' не содержит данных о гостях");
            }

            for (Guest importedGuest : importedGuests) {
                validateGuestData(importedGuest);
                Optional<Guest> existingGuest = findGuestById(importedGuest.getId());
                if (existingGuest.isPresent()) {
                    updateExistingGuest(existingGuest.get(), importedGuest);
                } else {
                    addNewGuest(importedGuest);
                }
            }

            System.out.println("Импортировано " + importedGuests.size() + " гостей");
        } catch (ImportExportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportExportException("импорте гостей", filename, e);
        }
    }

    public void exportGuestsToCSV(String filename) throws ImportExportException {
        if (!exportEnabled) {
            throw new ImportExportException("Экспорт отключен в настройках");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new ImportExportException("Имя файла не может быть пустым");
        }

        if (guests.isEmpty()) {
            throw new ImportExportException("Нет данных о гостях для экспорта");
        }

        try {
            CSVImportExport<Guest> csvHandler = new CSVImportExport<>(Guest.class, csvDelimiter);
            csvHandler.exportToFile(guests, filename);
            System.out.println("Экспортировано " + guests.size() + " гостей");
        } catch (ImportExportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportExportException("экспорте гостей", filename, e);
        }
    }

    // Основные методы бизнес-логики
    public void addRoom(Room room) throws ValidationException {
        if (room == null) {
            throw new ValidationException("Номер не может быть null");
        }
        validateRoomData(room);

        if (rooms.stream().anyMatch(r -> r.getNumber() == room.getNumber())) {
            throw new ValidationException("Номер с таким номером " + room.getNumber() + " уже существует");
        }

        if (!rooms.contains(room)) {
            if (room.getId() == null) {
                room.setId(generateRoomId());
            }
            rooms.add(room);
            System.out.println("Добавлен номер: " + room.getNumber());
        }
    }

    public boolean checkIn(int roomNumber, Guest guest, int nights) throws HotelManagementException {
        if (guest == null) {
            throw new ValidationException("Гость не может быть null");
        }
        if (nights <= 0) {
            throw new InvalidDataException("количество ночей", String.valueOf(nights), "Должно быть положительным числом");
        }

        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomNotAvailableException(roomNumber, room.getStatus().getDescription());
        }

        validateGuestData(guest);

        if (findGuestByPassport(guest.getPassportNumber()).isPresent()) {
            throw new GuestNotFoundException("Гость с паспортом " + guest.getPassportNumber() +
                    " уже проживает в отеле");
        }

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = checkInDate.plusDays(nights);

        if (!guests.contains(guest)) {
            if (guest.getId() == null) {
                guest.setId(generateGuestId());
            }
            guests.add(guest);
        }

        boolean result = room.checkIn(guest, checkInDate, checkOutDate);
        if (result) {
            System.out.println("Гость " + guest.getName() + " заселен в номер " + roomNumber);
        }
        return result;
    }

    public boolean checkOut(int roomNumber) throws HotelManagementException {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));

        if (room.getStatus() != RoomStatus.OCCUPIED) {
            throw new RoomNotAvailableException(roomNumber, "Номер не занят");
        }

        boolean result = room.checkOut();
        if (result) {
            Guest guest = room.getCurrentGuest();
            if (guest != null && rooms.stream().noneMatch(r -> guest.equals(r.getCurrentGuest()))) {
                guests.remove(guest);
            }
            System.out.println("Гость выселен из номера " + roomNumber);
        }
        return result;
    }

    public boolean setRoomStatus(int roomNumber, String status) throws HotelManagementException {
        if (!allowStatusChange) {
            throw new HotelManagementException("Изменение статуса номеров отключено в конфигурации");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Статус не может быть пустым");
        }

        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));

        if (!RoomStatus.isValidStatus(status)) {
            throw new InvalidDataException("статус номера", status,
                    "Допустимые значения: " + String.join(", ", RoomStatus.getAllDescriptions()));
        }

        RoomStatus newStatus = RoomStatus.fromDescription(status);

        if (newStatus != RoomStatus.OCCUPIED && room.getStatus() == RoomStatus.OCCUPIED) {
            throw new RoomNotAvailableException(roomNumber,
                    "Нельзя изменить статус занятого номера. Сначала выполните выселение.");
        }

        room.setStatus(newStatus);
        System.out.println("Статус номера " + roomNumber + " изменен на: " + status);
        return true;
    }

    public boolean changeRoomPrice(int roomNumber, double newPrice) throws HotelManagementException {
        if (newPrice < minRoomPrice) {
            throw new InvalidDataException("цена номера", String.valueOf(newPrice),
                    "Минимальная цена: " + minRoomPrice);
        }

        if (newPrice > maxRoomPrice) {
            throw new InvalidDataException("цена номера", String.valueOf(newPrice),
                    "Максимальная цена: " + maxRoomPrice);
        }

        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));

        room.setPricePerNight(newPrice);
        System.out.println("Цена номера " + roomNumber + " изменена на: " + newPrice);
        return true;
    }

    public void addService(Service service) throws ValidationException {
        if (service == null) {
            throw new ValidationException("Услуга не может быть null");
        }
        validateServiceData(service);

        if (services.stream().anyMatch(s -> s.getName().equalsIgnoreCase(service.getName()))) {
            throw new ValidationException("Услуга с названием '" + service.getName() + "' уже существует");
        }

        if (!services.contains(service)) {
            if (service.getId() == null) {
                service.setId(generateServiceId());
            }
            services.add(service);
            System.out.println("Добавлена услуга: " + service.getName());
        }
    }

    public boolean changeServicePrice(String serviceName, double newPrice) throws HotelManagementException {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new ValidationException("Название услуги не может быть пустым");
        }
        if (newPrice < 0) {
            throw new InvalidDataException("цена услуги", String.valueOf(newPrice), "Не может быть отрицательной");
        }

        Service service = findService(serviceName)
                .orElseThrow(() -> new ServiceNotFoundException(serviceName));

        service.setPrice(newPrice);
        System.out.println("Цена услуги '" + serviceName + "' изменена на: " + newPrice);
        return true;
    }

    public boolean addServiceToRoom(int roomNumber, String serviceName) throws HotelManagementException {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));

        Service service = findService(serviceName)
                .orElseThrow(() -> new ServiceNotFoundException(serviceName));

        if (room.getAdditionalServices().contains(service)) {
            throw new HotelManagementException("Услуга '" + serviceName + "' уже добавлена к номеру " + roomNumber);
        }

        room.addService(service);
        System.out.println("Услуга '" + serviceName + "' добавлена к номеру " + roomNumber);
        return true;
    }

    public boolean addServiceToGuest(int roomNumber, String serviceName) throws HotelManagementException {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));

        if (room.getCurrentGuest() == null) {
            throw new GuestNotFoundException("В номере " + roomNumber + " нет гостя");
        }

        Service service = findService(serviceName)
                .orElseThrow(() -> new ServiceNotFoundException(serviceName));

        room.addRoomService(service, LocalDate.now());
        System.out.println("Услуга '" + serviceName + "' добавлена гостю в номере " + roomNumber);
        return true;
    }

    private void validateRoomData(Room room) throws ValidationException {
        if (room.getNumber() <= 0) {
            throw new ValidationException("номер", "номер комнаты", "Должен быть положительным числом");
        }

        if (room.getPricePerNight() < this.minRoomPrice) {
            throw new ValidationException("номер", "цена за ночь",
                    "Минимальная цена: " + this.minRoomPrice);
        }
        if (room.getPricePerNight() > this.maxRoomPrice) {
            throw new ValidationException("номер", "цена за ночь",
                    "Максимальная цена: " + this.maxRoomPrice);
        }

        if (room.getCapacity() <= 0) {
            throw new ValidationException("номер", "вместимость", "Должна быть положительным числом");
        }
        if (room.getCapacity() > this.maxGuestsPerRoom) {
            throw new ValidationException("номер", "вместимость",
                    "Максимальная вместимость: " + this.maxGuestsPerRoom);
        }
        if (room.getStars() < 1 || room.getStars() > 5) {
            throw new ValidationException("номер", "количество звезд", "Должно быть от 1 до 5");
        }
        if (room.getType() == null || room.getType().trim().isEmpty()) {
            throw new ValidationException("номер", "тип номера", "Не может быть пустым");
        }

        // Проверка поддерживаемого типа номера с отладкой
        if (supportedRoomTypes != null && supportedRoomTypes.length > 0) {
            boolean supported = false;
            String roomType = room.getType().trim();

            System.out.println("Checking room type: '" + roomType + "'");
            System.out.println("Supported types: " + Arrays.toString(supportedRoomTypes));

            for (String supportedType : supportedRoomTypes) {
                if (supportedType != null && supportedType.trim().equalsIgnoreCase(roomType)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw new ValidationException("номер", "тип номера",
                        "Неподдерживаемый тип '" + roomType + "'. Поддерживаемые: " + getSupportedRoomTypesAsString());
            }
        } else {
            System.out.println("Warning: No supported room types configured");
        }
    }

    private void validateGuestData(Guest guest) throws ValidationException {
        if (guest.getName() == null || guest.getName().trim().isEmpty()) {
            throw new ValidationException("гость", "имя", "Не может быть пустым");
        }
        if (guest.getPassportNumber() == null || guest.getPassportNumber().trim().isEmpty()) {
            throw new ValidationException("гость", "номер паспорта", "Не может быть пустым");
        }
        if (guest.getPhoneNumber() == null || guest.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("гость", "телефон", "Не может быть пустым");
        }

        if (!guest.getPassportNumber().matches("\\d{10}")) {
            throw new ValidationException("гость", "номер паспорта", "Должен содержать 10 цифр");
        }
    }

    private void validateServiceData(Service service) throws ValidationException {
        if (service.getName() == null || service.getName().trim().isEmpty()) {
            throw new ValidationException("услуга", "название", "Не может быть пустым");
        }
        if (service.getPrice() < 0) {
            throw new ValidationException("услуга", "цена", "Не может быть отрицательной");
        }
        if (service.getDescription() == null || service.getDescription().trim().isEmpty()) {
            throw new ValidationException("услуга", "описание", "Не может быть пустым");
        }
    }

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

    private void updateExistingRoom(Room existing, Room imported) {
        existing.setNumber(imported.getNumber());
        existing.setType(imported.getType());
        existing.setPricePerNight(imported.getPricePerNight());
        existing.setCapacity(imported.getCapacity());
        existing.setStars(imported.getStars());
    }

    private void updateExistingService(Service existing, Service imported) {
        existing.setName(imported.getName());
        existing.setPrice(imported.getPrice());
        existing.setDescription(imported.getDescription());
    }

    private void updateExistingGuest(Guest existing, Guest imported) {
        existing.setName(imported.getName());
        existing.setPassportNumber(imported.getPassportNumber());
        existing.setPhoneNumber(imported.getPhoneNumber());
    }

    private void addNewRoom(Room room) {
        if (room.getId() == null) {
            room.setId(generateRoomId());
        }
        rooms.add(room);
    }

    private void addNewService(Service service) {
        if (service.getId() == null) {
            service.setId(generateServiceId());
        }
        services.add(service);
    }

    private void addNewGuest(Guest guest) {
        if (guest.getId() == null) {
            guest.setId(generateGuestId());
        }
        guests.add(guest);
    }

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
        if (date == null) {
            throw new IllegalArgumentException("Дата не может быть null");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Нельзя искать номера на прошедшую дату: " + date);
        }
        return rooms.stream()
                .filter(room -> room.isAvailableOnDate(date))
                .sorted()
                .collect(Collectors.toList());
    }

    public double getRoomPayment(int roomNumber) throws HotelManagementException {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));

        if (room.getCurrentGuest() == null || room.getCheckInDate() == null) {
            throw new HotelManagementException("Номер " + roomNumber + " не занят или отсутствует дата заселения");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                room.getCheckInDate(), LocalDate.now());
        return room.calculateTotalPrice((int) Math.max(1, nights));
    }

    public List<StayHistory> getLastGuests(int roomNumber) throws HotelManagementException {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));
        return room.getLastGuests(historySize);
    }

    public List<StayHistory> getLastThreeGuests(int roomNumber) throws HotelManagementException {
        return getLastGuests(roomNumber);
    }

    public String getRoomDetails(int roomNumber) throws HotelManagementException {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));
        return room.getDetailedInfo();
    }

    public List<RoomService> getGuestServices(Guest guest) throws HotelManagementException {
        if (guest == null) {
            throw new ValidationException("Гость не может быть null");
        }

        if (!guests.contains(guest)) {
            throw new GuestNotFoundException("Гость не найден в системе");
        }

        List<RoomService> guestServices = new ArrayList<>();
        for (Room room : rooms) {
            if (guest.equals(room.getCurrentGuest())) {
                guestServices.addAll(room.getRoomServices());
            }
        }
        return guestServices;
    }

    public Guest getGuestByRoomNumber(int roomNumber) throws HotelManagementException {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));
        return room.getCurrentGuest();
    }

    public Room findRoomByNumber(int roomNumber) {
        return findRoom(roomNumber).orElse(null);
    }

    public Service findServiceByName(String serviceName) {
        return findService(serviceName).orElse(null);
    }

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
        if (passport == null || passport.trim().isEmpty()) {
            throw new IllegalArgumentException("Номер паспорта не может быть пустым");
        }
        return rooms.stream()
                .filter(room -> room.getCurrentGuest() != null)
                .filter(room -> room.getCurrentGuest().getPassportNumber().equals(passport))
                .map(room -> Map.entry(room.getCurrentGuest(), room))
                .findFirst();
    }

    public List<Service> getServicesByPriceRange(double minPrice, double maxPrice) {
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            throw new IllegalArgumentException("Неверный диапазон цен. Минимальная цена: " +
                    minPrice + ", максимальная: " + maxPrice);
        }
        return services.stream()
                .filter(service -> service.getPrice() >= minPrice && service.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsByCriteria(int minStars, int maxCapacity, double maxPrice) {
        if (minStars < 1 || minStars > 5) {
            throw new IllegalArgumentException("Количество звезд должно быть от 1 до 5");
        }

        return rooms.stream()
                .filter(room -> room.getStars() >= minStars)
                .filter(room -> room.getCapacity() <= maxCapacity)
                .filter(room -> room.getPricePerNight() <= maxPrice)
                .collect(Collectors.toList());
    }

    public void printConfiguration() {
        System.out.println("Hotel Configuration:");
        System.out.println("  Name: " + hotelName);
        System.out.println("  Allow status change: " + allowStatusChange);
        System.out.println("  History size: " + historySize);
        System.out.println("  Max guests per room: " + maxGuestsPerRoom);
        System.out.println("  Room price range: " + minRoomPrice + " - " + maxRoomPrice);
        System.out.println("  Check-in time: " + defaultCheckInTime);
        System.out.println("  Check-out time: " + defaultCheckOutTime);
    }

    public void backupData() {
        try {
            String backupFilename = backupPath + "backup_" + System.currentTimeMillis() + ".ser";
            saveState(backupFilename);
            System.out.println("Backup created: " + backupFilename);
        } catch (Exception e) {
            System.out.println("Backup failed: " + e.getMessage());
        }
    }

    public Map<String, Long> getRoomTypeStatistics() {
        return rooms.stream()
                .collect(Collectors.groupingBy(
                        Room::getType,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getPopularServices() {
        Map<String, Long> serviceUsage = new HashMap<>();

        for (Room room : rooms) {
            for (RoomService roomService : room.getRoomServices()) {
                String serviceName = roomService.getService().getName();
                serviceUsage.put(serviceName, serviceUsage.getOrDefault(serviceName, 0L) + 1);
            }
        }

        return serviceUsage;
    }
}