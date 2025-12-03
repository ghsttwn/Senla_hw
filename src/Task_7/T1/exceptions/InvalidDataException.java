package Task_7.T1.exceptions;

public class InvalidDataException extends HotelManagementException {
    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String field, String value) {
        super("Неверное значение '" + value + "' для поля '" + field + "'");
    }

    public InvalidDataException(String field, String value, String validRange) {
        super("Неверное значение '" + value + "' для поля '" + field + "'. " + validRange);
    }
}