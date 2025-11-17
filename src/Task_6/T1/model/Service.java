package Task_6.T1.model;

import java.util.Objects;

public class Service implements Comparable<Service>, Identifiable {
    private Long id;
    private String name;
    private double price;
    private String description;

    public Service() {
    }

    public Service(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public Service(Long id, String name, double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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
        return "Service{id=" + id + ", name='" + name + "', price=" + price + ", description='" + description + "'}";
    }
}