import java.util.Objects;

public class CarBody implements Interfaces.IProductPart {
    private String type;
    private String material;

    public CarBody(String type, String material) {
        this.type = type;
        this.material = material;
    }

    @Override
    public String getName() {
        return "Кузов (" + type + ", " + material + ")";
    }

    public String getType() { return type; }
    public String getMaterial() { return material; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarBody carBody = (CarBody) o;
        return Objects.equals(type, carBody.type) &&
                Objects.equals(material, carBody.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, material);
    }

    @Override
    public String toString() {
        return "CarBody{" +
                "type='" + type + '\'' +
                ", material='" + material + '\'' +
                '}';
    }
}