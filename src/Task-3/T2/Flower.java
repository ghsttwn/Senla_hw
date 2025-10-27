import java.util.Objects;

public abstract class Flower {
    protected String name;
    protected double price;
    protected String color;

    public Flower(String name, double price, String color) {
        this.name = name;
        this.price = price;
        this.color = color;
    }

    public double getPrice() { return price; }
    public String getName() { return name; }
    public String getColor() { return color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flower flower = (Flower) o;
        return Double.compare(price, flower.price) == 0 &&
                Objects.equals(name, flower.name) &&
                Objects.equals(color, flower.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, color);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "Название='" + name + '\'' +
                ", цена=" + price +
                ", цвет='" + color + '\'' +
                '}';
    }
}
