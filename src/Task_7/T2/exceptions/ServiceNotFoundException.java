package Task_7.T2.exceptions;

public class ServiceNotFoundException extends HotelManagementException {

    public ServiceNotFoundException(String serviceName) {
        super("Услуга '" + serviceName + "' не найдена");
    }

    public ServiceNotFoundException(String serviceName, String details) {
        super("Услуга '" + serviceName + "' не найдена. " + details);
    }
}