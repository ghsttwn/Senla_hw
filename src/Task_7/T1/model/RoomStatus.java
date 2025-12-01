package Task_7.T1.model;

import Task_7.T1.exceptions.InvalidDataException;

public enum RoomStatus {
    AVAILABLE("Доступен"),
    OCCUPIED("Занят"),
    UNDER_MAINTENANCE("На ремонте"),
    UNDER_SERVICE("На обслуживании");

    private final String description;

    RoomStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValidStatus(String status) {
        if (status == null) return false;
        for (RoomStatus roomStatus : values()) {
            if (roomStatus.getDescription().equals(status)) {
                return true;
            }
        }
        return false;
    }

    public static RoomStatus fromDescription(String description) throws InvalidDataException {
        if (description == null) {
            throw new InvalidDataException("Описание статуса не может быть null");
        }
        for (RoomStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        throw new InvalidDataException("статус номера", description,
                "Допустимые значения: " + String.join(", ", getAllDescriptions()));
    }

    public static String[] getAllDescriptions() {
        RoomStatus[] statuses = values();
        String[] descriptions = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            descriptions[i] = statuses[i].getDescription();
        }
        return descriptions;
    }
}