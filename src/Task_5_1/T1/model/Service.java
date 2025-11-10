package Task_5_1.T1.model;

import java.util.Objects;

public class Service implements Comparable<Service> {
    private String name;
    private double price;
    private String description;

    public Service(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public void setPrice(double price) { this.price = price; }

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
        return "Service{name='" + name + "', price=" + price + ", description='" + description + "'}";
    }
}