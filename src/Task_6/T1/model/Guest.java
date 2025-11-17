package Task_6.T1.model;

import java.util.Objects;

public class Guest implements Comparable<Guest>, Identifiable {
    private Long id;
    private String name;
    private String passportNumber;
    private String phoneNumber;

    public Guest() {
    }

    public Guest(String name, String passportNumber, String phoneNumber) {
        this.name = name;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
    }

    public Guest(Long id, String name, String passportNumber, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

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
        return "Guest{id=" + id + ", name='" + name + "', passport='" + passportNumber + "', phone='" + phoneNumber + "'}";
    }
}