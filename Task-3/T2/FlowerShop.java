package T2;

import java.util.ArrayList;
import java.util.List;
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