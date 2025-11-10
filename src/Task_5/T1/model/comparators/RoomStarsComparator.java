package Task_5.T1.model.comparators;
import Task_5.T1.model.Room;
import java.util.Comparator;


public class RoomStarsComparator implements Comparator<Room> {
    @Override
    public int compare(Room room1, Room room2) {
        return Integer.compare(room1.getStars(), room2.getStars());
    }
}
