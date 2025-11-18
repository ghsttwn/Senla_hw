package Task_6.T2.model.comparators;

import Task_6.T2.model.Room;

import java.util.Comparator;


public class RoomCapacityComparator implements Comparator<Room> {
    @Override
    public int compare(Room room1, Room room2) {
        return Integer.compare(room1.getCapacity(), room2.getCapacity());
    }
}
