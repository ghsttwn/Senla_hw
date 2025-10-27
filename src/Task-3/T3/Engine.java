import java.util.Objects;

public class Engine implements Interfaces.IProductPart {
    private double volume;
    private int horsepower;
    private String fuelType;

    public Engine(double volume, int horsepower, String fuelType) {
        this.volume = volume;
        this.horsepower = horsepower;
        this.fuelType = fuelType;
    }

    @Override
    public String getName() {
        return "Двигатель (" + volume + "L, " + horsepower + " л.с., " + fuelType + ")";
    }

    public double getVolume() { return volume; }
    public int getHorsepower() { return horsepower; }
    public String getFuelType() { return fuelType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Engine engine = (Engine) o;
        return Double.compare(volume, engine.volume) == 0 &&
                horsepower == engine.horsepower &&
                Objects.equals(fuelType, engine.fuelType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(volume, horsepower, fuelType);
    }

    @Override
    public String toString() {
        return "Engine{" +
                "volume=" + volume +
                ", horsepower=" + horsepower +
                ", fuelType='" + fuelType + '\'' +
                '}';
    }
}