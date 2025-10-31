package Task_4.T1;

import java.util.Comparator;

public class RoomStarsComparator implements Comparator<Room> {
    @Override
    public int compare(Room room1, Room room2) {
        return Integer.compare(room1.getStars(), room2.getStars());
    }
}