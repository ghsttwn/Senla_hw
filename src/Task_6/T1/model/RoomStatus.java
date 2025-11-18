package Task_6.T1.model;

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
        for (RoomStatus roomStatus : values()) {
            if (roomStatus.getDescription().equals(status)) {
                return true;
            }
        }
        return false;
    }

    public static RoomStatus fromDescription(String description) {
        for (RoomStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + description);
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