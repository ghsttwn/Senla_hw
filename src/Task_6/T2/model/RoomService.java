package Task_6.T2.model;

import java.time.LocalDate;
import java.util.Objects;

public class RoomService {
    private Service service;
    private LocalDate date;
    private double price;

    public RoomService(Service service, LocalDate date, double price) {
        this.service = service;
        this.date = date;
        this.price = price;
    }

    public Service getService() { return service; }
    public LocalDate getDate() { return date; }
    public double getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomService that = (RoomService) o;
        return Double.compare(price, that.price) == 0 &&
                Objects.equals(service, that.service) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, date, price);
    }

    @Override
    public String toString() {
        return "RoomService{service=" + service.getName() + ", date=" + date + ", price=" + price + "}";
    }
}
