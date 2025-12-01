package Task_7.T2.exceptions;

public class HotelManagementException extends Exception {
    public HotelManagementException(String message) {
        super(message);
    }

    public HotelManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}