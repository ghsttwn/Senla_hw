import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Bouquet {
    private List<Flower> flowers;
    private String packaging;
    private double packagingPrice;

    public Bouquet() {
        this.flowers = new ArrayList<>();
        this.packaging = "Без упаковки";
        this.packagingPrice = 0;
    }

    public void addFlower(Flower flower) {
        if (flower != null) {
            flowers.add(flower);
        }
    }

    public void setPackaging(String packaging, double price) {
        this.packaging = packaging;
        this.packagingPrice = price;
    }

    public double calculateTotalPrice() {
        double total = packagingPrice;
        for (Flower flower : flowers) {
            total += flower.getPrice();
        }
        return total;
    }

    public List<Flower> getFlowers() {
        return new ArrayList<>(flowers);
    }

    public String getPackaging() { return packaging; }
    public double getPackagingPrice() { return packagingPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bouquet bouquet = (Bouquet) o;
        return Double.compare(packagingPrice, bouquet.packagingPrice) == 0 &&
                Objects.equals(flowers, bouquet.flowers) &&
                Objects.equals(packaging, bouquet.packaging);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowers, packaging, packagingPrice);
    }

    @Override
    public String toString() {
        return "Букет{" +
                "Цветы=" + flowers +
                ", упаковка='" + packaging + '\'' +
                ", цена упаковки=" + packagingPrice +
                ", сумма к оплате=" + calculateTotalPrice() +
                '}';
    }

    public void displayBouquet() {
        System.out.println("=== Ваш букет ===");
        for (Flower flower : flowers) {
            System.out.println(flower);
        }
        System.out.println("Упаковка: " + packaging + " - " + packagingPrice + " руб.");
        System.out.println("Общая стоимость: " + calculateTotalPrice() + " руб.");
    }
}