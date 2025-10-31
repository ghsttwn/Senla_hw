package Task_4.T1;

import java.util.Comparator;
import java.util.Map;

public class GuestCheckOutDateComparator implements Comparator<Map.Entry<Guest, Room>> {
    @Override
    public int compare(Map.Entry<Guest, Room> entry1, Map.Entry<Guest, Room> entry2) {
        return entry1.getValue().getCheckOutDate().compareTo(entry2.getValue().getCheckOutDate());
    }
}
