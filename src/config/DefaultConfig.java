package config;

import java.util.Properties;

public class DefaultConfig {
    public static Properties getDefaultProperties() {
        Properties props = new Properties();

        // Hotel Configuration
        props.setProperty("HOTEL.NAME", "Гранд Отель");
        props.setProperty("ROOM.STATUS.CHANGE.ENABLED", "true");
        props.setProperty("ROOM.HISTORY.SIZE", "5");
        props.setProperty("HOTEL.DEFAULT.ROOMS", "10");
        props.setProperty("HOTEL.DEFAULT.SERVICES", "5");
        props.setProperty("CSV.DELIMITER", ",");
        props.setProperty("EXPORT.ENABLED", "true");
        props.setProperty("BACKUP.PATH", "./backups/");
        props.setProperty("SUPPORTED.ROOM.TYPES", "Стандарт,Люкс,Президентский");
        props.setProperty("MAX.GUESTS.PER.ROOM", "4");
        props.setProperty("ROOM.MIN.PRICE", "1000.0");
        props.setProperty("ROOM.MAX.PRICE", "50000.0");
        props.setProperty("DEFAULT.CHECKIN.TIME", "14:00");
        props.setProperty("DEFAULT.CHECKOUT.TIME", "12:00");

        // UI Configuration
        props.setProperty("UI.REFRESH.RATE", "1000");
        props.setProperty("UI.DATE.FORMAT", "dd.MM.yyyy");
        props.setProperty("UI.SHOW.WARNINGS", "true");
        props.setProperty("UI.AUTO.SAVE", "true");
        props.setProperty("UI.AUTO.SAVE.INTERVAL", "300");
        props.setProperty("UI.MAX.INPUT.ATTEMPTS", "3");

        return props;
    }
}