package Task_5.T2.factory;

import Task_5.T2.HotelUI;
import Task_5.T2.Menu;
import Task_5.T2.MenuItem;

public class HotelMenuFactory implements MenuFactory {
    private HotelUI ui;

    public HotelMenuFactory(HotelUI ui) {
        this.ui = ui;
    }

    @Override
    public Menu createMenu() {
        return buildMainMenu();
    }

    public Menu buildMainMenu() {
        Menu mainMenu = new Menu("ГОСТИНИЦА - ГЛАВНОЕ МЕНЮ");

        mainMenu.addMenuItem(new MenuItem("Управление номерами", () -> ui.showRoomManagementMenu()));
        mainMenu.addMenuItem(new MenuItem("Управление гостями", () -> ui.showGuestManagementMenu()));
        mainMenu.addMenuItem(new MenuItem("Управление услугами", () -> ui.showServiceManagementMenu()));
        mainMenu.addMenuItem(new MenuItem("Отчеты и статистика", () -> ui.showReportsMenu()));
        mainMenu.addMenuItem(new MenuItem("Поиск и сортировка", () -> ui.showSearchMenu()));
        mainMenu.addMenuItem(new MenuItem("Расширенная аналитика", () -> ui.showAnalyticsMenu()));

        return mainMenu;
    }

    public Menu buildAnalyticsMenu() {
        Menu menu = new Menu("РАСШИРЕННАЯ АНАЛИТИКА");

        menu.addMenuItem(new MenuItem("Расширенная статистика", () -> ui.showExtendedStatistics()));
        menu.addMenuItem(new MenuItem("Поиск номеров по критериям", () -> ui.findRoomsByCriteria()));
        menu.addMenuItem(new MenuItem("Услуги по ценовому диапазону", () -> ui.showServicesByPriceRange()));
        menu.addMenuItem(new MenuItem("Гости по типам номеров", () -> ui.displayGuestsGroupedByRoomType()));

        return menu;
    }

    public Menu buildRoomManagementMenu() {
        Menu menu = new Menu("УПРАВЛЕНИЕ НОМЕРАМИ");

        menu.addMenuItem(new MenuItem("Показать все номера", () -> ui.displayAllRooms()));
        menu.addMenuItem(new MenuItem("Показать свободные номера", () -> ui.displayAvailableRooms()));
        menu.addMenuItem(new MenuItem("Заселить гостя", () -> ui.checkInGuest()));
        menu.addMenuItem(new MenuItem("Выселить гостя", () -> ui.checkOutGuest()));
        menu.addMenuItem(new MenuItem("Изменить статус номера", () -> ui.changeRoomStatus()));
        menu.addMenuItem(new MenuItem("Изменить цену номера", () -> ui.changeRoomPrice()));
        menu.addMenuItem(new MenuItem("Показать детали номера", () -> ui.showRoomDetails()));

        return menu;
    }

    public Menu buildGuestManagementMenu() {
        Menu menu = new Menu("УПРАВЛЕНИЕ ГОСТЯМИ");

        menu.addMenuItem(new MenuItem("Показать всех постояльцев", () -> ui.displayAllGuests()));
        menu.addMenuItem(new MenuItem("Постояльцы (сорт. по имени)", () -> ui.displayGuestsSortedByName()));
        menu.addMenuItem(new MenuItem("Постояльцы (сорт. по дате выезда)", () -> ui.displayGuestsSortedByCheckOutDate()));
        menu.addMenuItem(new MenuItem("Добавить услугу гостю", () -> ui.addServiceToGuest()));
        menu.addMenuItem(new MenuItem("Показать услуги гостя", () -> ui.showGuestServices()));
        menu.addMenuItem(new MenuItem("Детальная информация о госте", () -> ui.showGuestDetails()));
        menu.addMenuItem(new MenuItem("Найти гостя по паспорту", () -> ui.findGuestByPassport()));

        return menu;
    }

    public Menu buildServiceManagementMenu() {
        Menu menu = new Menu("УПРАВЛЕНИЕ УСЛУГАМИ");

        menu.addMenuItem(new MenuItem("Показать все услуги", () -> ui.displayAllServices()));
        menu.addMenuItem(new MenuItem("Услуги (сорт. по цене)", () -> ui.displayServicesSortedByPrice()));
        menu.addMenuItem(new MenuItem("Добавить услугу к номеру", () -> ui.addServiceToRoom()));
        menu.addMenuItem(new MenuItem("Изменить цену услуги", () -> ui.changeServicePrice()));

        return menu;
    }

    public Menu buildReportsMenu() {
        Menu menu = new Menu("ОТЧЕТЫ И СТАТИСТИКА");

        menu.addMenuItem(new MenuItem("Общая статистика", () -> ui.showStatistics()));
        menu.addMenuItem(new MenuItem("Сумма оплаты за номер", () -> ui.showRoomPayment()));
        menu.addMenuItem(new MenuItem("История проживаний номера", () -> ui.showRoomHistory()));

        return menu;
    }

    public Menu buildSearchMenu() {
        Menu menu = new Menu("ПОИСК И СОРТИРОВКА");

        menu.addMenuItem(new MenuItem("Номера по цене", () -> ui.displayRoomsSortedByPrice()));
        menu.addMenuItem(new MenuItem("Номера по вместимости", () -> ui.displayRoomsSortedByCapacity()));
        menu.addMenuItem(new MenuItem("Номера по звездам", () -> ui.displayRoomsSortedByStars()));
        menu.addMenuItem(new MenuItem("Свободные номера по цене", () -> ui.displayAvailableRoomsSortedByPrice()));
        menu.addMenuItem(new MenuItem("Номера доступные на дату", () -> ui.searchRoomsByDate()));

        return menu;
    }
}