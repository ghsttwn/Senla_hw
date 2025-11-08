package Task_5.T1.model.comparators;

import Task_5.T1.model.Service;
import java.util.Comparator;


public class ServicePriceComparator implements Comparator<Service> {
    @Override
    public int compare(Service service1, Service service2) {
        return Double.compare(service1.getPrice(), service2.getPrice());
    }
}