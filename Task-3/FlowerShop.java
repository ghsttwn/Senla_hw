import java.util.ArrayList;
import java.util.List;

// Абстрактный класс цветка
abstract class Flower {
    protected String name;
    protected double price;
    protected String color;

    public Flower(String name, double price, String color) {
        this.name = name;
        this.price = price;
        this.color = color;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + color + ") - " + price + " руб.";
    }
}

// Конкретные виды цветов
class Rose extends Flower {
    public Rose(String color, double price) {
        super("Роза", price, color);
    }
}

class Tulip extends Flower {
    public Tulip(String color, double price) {
        super("Тюльпан", price, color);
    }
}

class Lily extends Flower {
    public Lily(String color, double price) {
        super("Лилия", price, color);
    }
}

class Chrysanthemum extends Flower {
    public Chrysanthemum(String color, double price) {
        super("Хризантема", price, color);
    }
}

// Класс букета
class Bouquet {
    private List<Flower> flowers;
    private String packaging;
    private double packagingPrice;

    public Bouquet() {
        this.flowers = new ArrayList<>();
        this.packaging = "Без упаковки";
        this.packagingPrice = 0;
    }

    public void addFlower(Flower flower) {
        flowers.add(flower);
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

    public void displayBouquet() {
        System.out.println("=== Ваш букет ===");
        for (Flower flower : flowers) {
            System.out.println(flower);
        }
        System.out.println("Упаковка: " + packaging + " - " + packagingPrice + " руб.");
        System.out.println("Общая стоимость: " + calculateTotalPrice() + " руб.");
    }
}

// Демонстрационный класс
class FlowerShop {
    public static void main(String[] args) {
        // Создаем цветы
        Rose redRose = new Rose("Красного цвета", 150);
        Rose whiteRose = new Rose("Белого цвета", 140);
        Tulip yellowTulip = new Tulip("Желтого цвета", 80);
        Lily pinkLily = new Lily("Розового цвета", 200);
        Chrysanthemum whiteChrys = new Chrysanthemum("Белого цвета", 120);

        // Создаем букет
        Bouquet bouquet = new Bouquet();
        bouquet.addFlower(redRose);
        bouquet.addFlower(redRose);
        bouquet.addFlower(whiteRose);
        bouquet.addFlower(yellowTulip);
        bouquet.addFlower(pinkLily);
        bouquet.addFlower(whiteChrys);

        // Добавляем упаковку
        bouquet.setPackaging("Праздничная упаковка", 50);

        // Выводим информацию о букете
        bouquet.displayBouquet();
    }
}