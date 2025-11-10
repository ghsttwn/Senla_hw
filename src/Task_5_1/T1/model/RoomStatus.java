package Task_5_1.T1.model;


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

    public static String[] getAllDescriptions() {
        RoomStatus[] statuses = values();
        String[] descriptions = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            descriptions[i] = statuses[i].getDescription();
        }
        return descriptions;
    }
}