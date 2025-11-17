package Task_6.T2.exceptions;

public class RoomNotAvailableException extends HotelManagementException {
    public RoomNotAvailableException(String message) {
        super(message);
    }

    public RoomNotAvailableException(int roomNumber, String status) {
        super("Номер " + roomNumber + " недоступен. Текущий статус: " + status);
    }
}