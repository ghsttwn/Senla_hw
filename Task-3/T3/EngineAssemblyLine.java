package T3;

import java.util.Random;

public class EngineAssemblyLine implements Interfaces.ILineStep {
    private Random random = new Random();

    @Override
    public Interfaces.IProductPart buildProductPart() {
        double[] volumes = {1.6, 2.0, 2.5, 3.0};
        int[] powers = {120, 150, 200, 250, 300};
        String[] fuels = {"Бензин", "Дизель", "Электричество"};

        double volume = volumes[random.nextInt(volumes.length)];
        int power = powers[random.nextInt(powers.length)];
        String fuel = fuels[random.nextInt(fuels.length)];

        Engine engine = new Engine(volume, power, fuel);
        System.out.println("Создан двигатель: " + engine.getName());
        return engine;
    }
}
