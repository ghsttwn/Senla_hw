package Task_6.T2.exceptions;

public class RoomNotFoundException extends HotelManagementException {
    public RoomNotFoundException(String message) {
        super(message);
    }

    public RoomNotFoundException(int roomNumber) {
        super("Номер " + roomNumber + " не найден");
    }
}