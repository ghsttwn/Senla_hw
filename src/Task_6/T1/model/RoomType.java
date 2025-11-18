package Task_6.T1.model;

public enum RoomType {
    STANDARD("Стандарт"),
    LUXURY("Люкс"),
    PRESIDENTIAL("Президентский");

    private final String description;

    RoomType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}