package T8.T1.exceptions;

public class RoomNotAvailableException extends HotelManagementException {
    public RoomNotAvailableException(String message) {
        super(message);
    }

    public RoomNotAvailableException(int roomNumber, String status) {
        super("Номер " + roomNumber + " недоступен. Текущий статус: " + status);
    }
}
