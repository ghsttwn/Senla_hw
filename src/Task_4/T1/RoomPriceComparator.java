package Task_4.T1;

import java.util.Comparator;

public class RoomPriceComparator implements Comparator<Room> {
    @Override
    public int compare(Room room1, Room room2) {
        return Double.compare(room1.getPricePerNight(), room2.getPricePerNight());
    }
}