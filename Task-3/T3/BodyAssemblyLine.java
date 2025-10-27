package T3;

import java.util.Random;

public class BodyAssemblyLine implements Interfaces.ILineStep {
    private Random random = new Random();

    @Override
    public Interfaces.IProductPart buildProductPart() {
        String[] types = {"Седан", "Хэтчбек", "Универсал", "Купе"};
        String[] materials = {"Сталь", "Алюминий", "Карбон"};

        String type = types[random.nextInt(types.length)];
        String material = materials[random.nextInt(materials.length)];

        CarBody body = new CarBody(type, material);
        System.out.println("Создан кузов: " + body.getName());
        return body;
    }
}