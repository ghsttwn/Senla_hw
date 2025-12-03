package Task_7.T2.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StayHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Guest guest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public StayHistory(Guest guest, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public Guest getGuest() { return guest; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StayHistory that = (StayHistory) o;
        return Objects.equals(guest, that.guest) &&
                Objects.equals(checkInDate, that.checkInDate) &&
                Objects.equals(checkOutDate, that.checkOutDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guest, checkInDate, checkOutDate);
    }

    @Override
    public String toString() {
        return "StayHistory{guest=" + guest.getName() + ", checkInDate=" + checkInDate + ", checkOutDate=" + checkOutDate + "}";
    }
}