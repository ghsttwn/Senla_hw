package T4;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room {
    public static final String AVAILABLE = "Доступен";
    public static final String OCCUPIED = "Занят";
    public static final String UNDER_MAINTENANCE = "На ремонте";
    public static final String UNDER_SERVICE = "На обслуживании";

    private int number;
    private String type;
    private double pricePerNight;
    private String status;
    private Guest currentGuest;
    private List<Service> additionalServices;

    public Room(int number, String type, double pricePerNight) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = AVAILABLE;
        this.additionalServices = new ArrayList<>();
    }

    public int getNumber() { return number; }
    public String getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public String getStatus() { return status; }
    public Guest getCurrentGuest() { return currentGuest; }
    public List<Service> getAdditionalServices() { return new ArrayList<>(additionalServices); }

    public void setPricePerNight(double price) { this.pricePerNight = price; }

    public boolean setStatus(String newStatus) {
        if (isValidStatus(newStatus)) {
            this.status = newStatus;
            if (!newStatus.equals(OCCUPIED)) {
                this.currentGuest = null;
            }
            return true;
        }
        return false;
    }

    private boolean isValidStatus(String status) {
        return status.equals(AVAILABLE) || status.equals(OCCUPIED) ||
                status.equals(UNDER_MAINTENANCE) || status.equals(UNDER_SERVICE);
    }

    public boolean checkIn(Guest guest) {
        if (status.equals(AVAILABLE)) {
            this.currentGuest = guest;
            this.status = OCCUPIED;
            return true;
        }
        return false;
    }

    public boolean checkOut() {
        if (status.equals(OCCUPIED) && currentGuest != null) {
            this.currentGuest = null;
            this.status = AVAILABLE;
            return true;
        }
        return false;
    }

    public void addService(Service service) {
        if (service != null) {
            additionalServices.add(service);
        }
    }

    public double calculateTotalPrice(int nights) {
        double total = pricePerNight * nights;
        for (Service service : additionalServices) {
            total += service.getPrice();
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return number == room.number &&
                Double.compare(pricePerNight, room.pricePerNight) == 0 &&
                Objects.equals(type, room.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, type, pricePerNight);
    }

    @Override
    public String toString() {
        return "Room{" +
                "\n\tnumber=" + number +
                ", \n\ttype='" + type + '\'' +
                ", \n\tpricePerNight=" + pricePerNight +
                ", \n\tstatus='" + status + '\'' +
                ", \n\tcurrentGuest=" + currentGuest +
                ", \n\tadditionalServices=" + additionalServices +
                '}';
    }
}