package T8.T1.model;

import java.io.Serializable;
import java.util.Objects;
import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;


public class Service implements Comparable<Service>, Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    @ConfigProperty(propertyName = "service.default.id", type = PropertyType.LONG)
    private Long id;

    @ConfigProperty(propertyName = "service.default.name", type = PropertyType.STRING)
    private String name;

    @ConfigProperty(propertyName = "service.default.price", type = PropertyType.DOUBLE)
    private double price;

    @ConfigProperty(propertyName = "service.default.description", type = PropertyType.STRING)
    private String description;

    @ConfigProperty(propertyName = "service.auto.generate.id", type = PropertyType.BOOLEAN)
    private static boolean autoGenerateId = true;

    @ConfigProperty(propertyName = "service.max.name.length", type = PropertyType.INTEGER)
    private static int maxNameLength = 100;

    @ConfigProperty(propertyName = "service.max.description.length", type = PropertyType.INTEGER)
    private static int maxDescriptionLength = 500;

    @ConfigProperty(propertyName = "service.min.price", type = PropertyType.DOUBLE)
    private static double minPrice = 0.0;

    @ConfigProperty(propertyName = "service.max.price", type = PropertyType.DOUBLE)
    private static double maxPrice = 10000.0;

    @ConfigProperty(propertyName = "service.default.category", type = PropertyType.STRING)
    private static String defaultCategory = "Общие";

    @ConfigProperty(propertyName = "service.available.categories", type = PropertyType.ARRAY)
    private static String[] availableCategories = {"Общие", "Питание", "СПА", "Транспорт", "Развлечения"};

    @ConfigProperty(propertyName = "service.validation.enabled", type = PropertyType.BOOLEAN)
    private static boolean validationEnabled = true;

    public Service() {
    }

    public Service(String name, double price, String description) {
        setName(name);
        setPrice(price);
        setDescription(description);
    }

    public Service(Long id, String name, double price, String description) {
        this(name, price, description);
        setId(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) {
        if (autoGenerateId && id == null) {
            this.id = generateAutoId();
        } else {
            this.id = id;
        }
    }

    private Long generateAutoId() {
        return System.currentTimeMillis() % 1000000 + 1000000;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (validationEnabled) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Название услуги не может быть пустым");
            }
            if (name.length() > maxNameLength) {
                throw new IllegalArgumentException("Название услуги не может превышать " + maxNameLength + " символов");
            }
        }
        this.name = name;
    }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (validationEnabled) {
            if (price < minPrice) {
                throw new IllegalArgumentException("Цена услуги не может быть меньше " + minPrice);
            }
            if (price > maxPrice) {
                throw new IllegalArgumentException("Цена услуги не может превышать " + maxPrice);
            }
        }
        this.price = price;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        if (validationEnabled) {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Описание услуги не может быть пустым");
            }
            if (description.length() > maxDescriptionLength) {
                throw new IllegalArgumentException("Описание услуги не может превышать " + maxDescriptionLength + " символов");
            }
        }
        this.description = description;
    }

    @Override
    public int compareTo(Service other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Double.compare(price, service.price) == 0 &&
                Objects.equals(name, service.name) &&
                Objects.equals(description, service.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, description);
    }

    @Override
    public String toString() {
        return String.format("Service{id=%d, name='%s', price=%.2f, description='%s'}",
                id, name, price, description);
    }

    // Дополнительные методы
    public String getShortDescription() {
        if (description.length() > 50) {
            return description.substring(0, 47) + "...";
        }
        return description;
    }

    public boolean isFree() {
        return price == 0.0;
    }

    public boolean isPremium() {
        return price > 1000.0;
    }

    // Статические геттеры и сеттеры для конфигурации
    public static boolean isAutoGenerateId() { return autoGenerateId; }
    public static void setAutoGenerateId(boolean autoGenerateId) {
        Service.autoGenerateId = autoGenerateId;
    }

    public static int getMaxNameLength() { return maxNameLength; }
    public static void setMaxNameLength(int maxNameLength) {
        Service.maxNameLength = maxNameLength;
    }

    public static int getMaxDescriptionLength() { return maxDescriptionLength; }
    public static void setMaxDescriptionLength(int maxDescriptionLength) {
        Service.maxDescriptionLength = maxDescriptionLength;
    }

    public static double getMinPrice() { return minPrice; }
    public static void setMinPrice(double minPrice) {
        Service.minPrice = minPrice;
    }

    public static double getMaxPrice() { return maxPrice; }
    public static void setMaxPrice(double maxPrice) {
        Service.maxPrice = maxPrice;
    }

    public static String getDefaultCategory() { return defaultCategory; }
    public static void setDefaultCategory(String defaultCategory) {
        Service.defaultCategory = defaultCategory;
    }

    public static String[] getAvailableCategories() { return availableCategories; }
    public static void setAvailableCategories(String[] availableCategories) {
        Service.availableCategories = availableCategories;
    }

    public static boolean isValidationEnabled() { return validationEnabled; }
    public static void setValidationEnabled(boolean validationEnabled) {
        Service.validationEnabled = validationEnabled;
    }
}