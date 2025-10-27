import java.util.Objects;

public class Car implements Interfaces.IProduct {
    private CarBody body;
    private Chassis chassis;
    private Engine engine;
    private boolean isAssembled = false;

    @Override
    public void installFirstPart(Interfaces.IProductPart part) {
        if (part instanceof CarBody) {
            this.body = (CarBody) part;
            System.out.println("Установлен кузов: " + part.getName());
        }
    }

    @Override
    public void installSecondPart(Interfaces.IProductPart part) {
        if (part instanceof Chassis) {
            this.chassis = (Chassis) part;
            System.out.println("Установлено шасси: " + part.getName());
        }
    }

    @Override
    public void installThirdPart(Interfaces.IProductPart part) {
        if (part instanceof Engine) {
            this.engine = (Engine) part;
            System.out.println("Установлен двигатель: " + part.getName());
            this.isAssembled = true;
        }
    }

    @Override
    public String getInfo() {
        if (!isAssembled) {
            return "Автомобиль не собран";
        }
        return "Собранный автомобиль:\n" +
                " - " + body.getName() + "\n" +
                " - " + chassis.getName() + "\n" +
                " - " + engine.getName();
    }

    @Override
    public boolean isAssembled() {
        return isAssembled;
    }

    public CarBody getBody() { return body; }
    public Chassis getChassis() { return chassis; }
    public Engine getEngine() { return engine; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return isAssembled == car.isAssembled &&
                Objects.equals(body, car.body) &&
                Objects.equals(chassis, car.chassis) &&
                Objects.equals(engine, car.engine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, chassis, engine, isAssembled);
    }

    @Override
    public String toString() {
        return "Car{" +
                "body=" + body +
                ", chassis=" + chassis +
                ", engine=" + engine +
                ", isAssembled=" + isAssembled +
                '}';
    }
}
