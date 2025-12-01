package Task_7.T1.model.comparators;

import Task_7.T1.model.Guest;
import Task_7.T1.model.Room;

import java.util.Comparator;
import java.util.Map;

public class GuestCheckOutDateComparator implements Comparator<Map.Entry<Guest, Room>> {
    @Override
    public int compare(Map.Entry<Guest, Room> entry1, Map.Entry<Guest, Room> entry2) {
        return entry1.getValue().getCheckOutDate().compareTo(entry2.getValue().getCheckOutDate());
    }
}
