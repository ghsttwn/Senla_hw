package T8.T1.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import T8.T1.annotations.ConfigProperty;
import T8.T1.annotations.PropertyType;

public class StayHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @ConfigProperty(propertyName = "history.max.duration.days", type = PropertyType.INTEGER)
    private static int maxDurationDays = 365;

    @ConfigProperty(propertyName = "history.min.duration.days", type = PropertyType.INTEGER)
    private static int minDurationDays = 1;

    @ConfigProperty(propertyName = "history.validation.enabled", type = PropertyType.BOOLEAN)
    private static boolean validationEnabled = true;

    private Guest guest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    @ConfigProperty(propertyName = "history.default.notes", type = PropertyType.STRING)
    private String notes;

    @ConfigProperty(propertyName = "history.default.rating", type = PropertyType.INTEGER)
    private int rating; // 1-5

    public StayHistory(Guest guest, LocalDate checkInDate, LocalDate checkOutDate) {
        setGuest(guest);
        setCheckInDate(checkInDate);
        setCheckOutDate(checkOutDate);
        this.notes = "";
        this.rating = 5;
    }

    public StayHistory(Guest guest, LocalDate checkInDate, LocalDate checkOutDate, String notes, int rating) {
        this(guest, checkInDate, checkOutDate);
        setNotes(notes);
        setRating(rating);
    }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) {
        if (validationEnabled && guest == null) {
            throw new IllegalArgumentException("Гость не может быть null");
        }
        this.guest = guest;
    }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) {
        if (validationEnabled && checkInDate == null) {
            throw new IllegalArgumentException("Дата заселения не может быть null");
        }
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) {
        if (validationEnabled && checkOutDate == null) {
            throw new IllegalArgumentException("Дата выезда не может быть null");
        }
        if (validationEnabled && checkInDate != null && checkOutDate.isBefore(checkInDate)) {
            throw new IllegalArgumentException("Дата выезда не может быть раньше даты заселения");
        }
        if (validationEnabled && checkInDate != null &&
                checkInDate.plusDays(maxDurationDays).isBefore(checkOutDate)) {
            throw new IllegalArgumentException("Продолжительность проживания не может превышать " +
                    maxDurationDays + " дней");
        }
        if (validationEnabled && checkInDate != null &&
                checkInDate.plusDays(minDurationDays).isAfter(checkOutDate)) {
            throw new IllegalArgumentException("Продолжительность проживания должна быть не менее " +
                    minDurationDays + " дня");
        }
        this.checkOutDate = checkOutDate;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }

    public int getRating() { return rating; }
    public void setRating(int rating) {
        if (validationEnabled && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Рейтинг должен быть от 1 до 5");
        }
        this.rating = rating;
    }

    public long getDurationDays() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public double calculateTotalPrice(double pricePerNight) {
        return pricePerNight * getDurationDays();
    }

    public boolean isLongStay() {
        return getDurationDays() > 30;
    }

    public boolean isRecent() {
        return checkOutDate != null &&
                checkOutDate.isAfter(LocalDate.now().minusMonths(6));
    }

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
        return String.format("StayHistory{guest=%s, checkInDate=%s, checkOutDate=%s, duration=%d дней, rating=%d, notes='%s'}",
                guest.getName(), checkInDate, checkOutDate, getDurationDays(), rating,
                notes.length() > 20 ? notes.substring(0, 17) + "..." : notes);
    }

    // Статические геттеры и сеттеры для конфигурации
    public static int getMaxDurationDays() { return maxDurationDays; }
    public static void setMaxDurationDays(int maxDurationDays) {
        StayHistory.maxDurationDays = maxDurationDays;
    }

    public static int getMinDurationDays() { return minDurationDays; }
    public static void setMinDurationDays(int minDurationDays) {
        StayHistory.minDurationDays = minDurationDays;
    }

    public static boolean isValidationEnabled() { return validationEnabled; }
    public static void setValidationEnabled(boolean validationEnabled) {
        StayHistory.validationEnabled = validationEnabled;
    }
}