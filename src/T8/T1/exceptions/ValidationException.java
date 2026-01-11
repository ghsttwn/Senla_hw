package T8.T1.exceptions;

public class ValidationException extends HotelManagementException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String entity, String field, String problem) {
        super("Ошибка валидации " + entity + ": поле '" + field + "' - " + problem);
    }
}