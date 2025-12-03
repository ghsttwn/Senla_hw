package Task_7.T1.exceptions;


public class ImportExportException extends HotelManagementException {
    public ImportExportException(String message) {
        super(message);
    }

    public ImportExportException(String operation, String filename, Throwable cause) {
        super("Ошибка при " + operation + " файла '" + filename + "': " + cause.getMessage(), cause);
    }
}