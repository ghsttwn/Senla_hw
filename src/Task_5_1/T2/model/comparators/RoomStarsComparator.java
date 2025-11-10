package Task_5_1.T2.model.comparators;
import Task_5.T2.model.Room;

import java.util.Comparator;


public class RoomStarsComparator implements Comparator<Room> {
    @Override
    public int compare(Room room1, Room room2) {
        return Integer.compare(room1.getStars(), room2.getStars());
    }
}
