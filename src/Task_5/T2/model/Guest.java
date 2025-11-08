package Task_5.T2.model;

import java.util.Objects;

public class Guest implements Comparable<Guest> {
    private String name;
    private String passportNumber;
    private String phoneNumber;

    public Guest(String name, String passportNumber, String phoneNumber) {
        this.name = name;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
    }

    public String getName() { return name; }
    public String getPassportNumber() { return passportNumber; }
    public String getPhoneNumber() { return phoneNumber; }

    @Override
    public int compareTo(Guest other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(passportNumber, guest.passportNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passportNumber);
    }

    @Override
    public String toString() {
        return "Guest{name='" + name + "', passport='" + passportNumber + "', phone='" + phoneNumber + "'}";
    }
}