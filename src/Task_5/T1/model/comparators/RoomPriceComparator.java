package Task_5.T1.model.comparators;

import Task_5.T1.model.Room;
import java.util.Comparator;


public class RoomPriceComparator implements Comparator<Room> {
    @Override
    public int compare(Room room1, Room room2) {
        return Double.compare(room1.getPricePerNight(), room2.getPricePerNight());
    }
}
