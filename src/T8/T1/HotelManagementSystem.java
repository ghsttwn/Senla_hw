package T8.T1;

import java.io.File;

public class HotelManagementSystem {
    public static void main(String[] args) {
        HotelUI ui = HotelUI.getInstance();

        // Проверяем наличие конфигурационного файла
        File configFile = new File("hotel.properties");
        if (!configFile.exists()) {
            System.out.println("ВНИМАНИЕ: Файл конфигурации 'hotel.properties' не найден!");
            System.out.println("Создайте файл со следующими настройками:");
            System.out.println("HOTEL.NAME=Название вашего отеля");
            System.out.println("ROOM.STATUS.CHANGE.ENABLED=true");
            System.out.println("ROOM.HISTORY.SIZE=5");
            System.out.println("DEFAULT.SERVICES=Завтрак, WiFi, Бассейн");
            System.out.println("ROOM.PRICES=2500.0, 3500.0, 5000.0");
            System.out.println("\nЗапустите программу снова после создания файла.");
            return;
        }

        ui.start();
    }
}