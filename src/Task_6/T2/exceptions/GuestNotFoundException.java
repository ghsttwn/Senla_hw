package Task_6.T2.exceptions;

public class GuestNotFoundException extends HotelManagementException {
    public GuestNotFoundException(String message) {
        super(message);
    }

    public GuestNotFoundException(String passport, String details) {
        super("Гость с паспортом " + passport + " не найден. " + details);
    }
}