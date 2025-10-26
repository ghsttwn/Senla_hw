import java.util.Random;

// Интерфейсы из диаграммы
interface IProductPart {
    String getName();
}

interface ILineStep {
    IProductPart buildProductPart();
}

interface IProduct {
    void installFirstPart(IProductPart part);
    void installSecondPart(IProductPart part);
    void installThirdPart(IProductPart part);
    String getInfo();
}

interface IAssemblyLine {
    IProduct assembleProduct(IProduct product);
}

// Реализация частей автомобиля
class CarBody implements IProductPart {
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
}

class Chassis implements IProductPart {
    private int wheelCount;
    private String suspensionType;

    public Chassis(int wheelCount, String suspensionType) {
        this.wheelCount = wheelCount;
        this.suspensionType = suspensionType;
    }

    @Override
    public String getName() {
        return "Шасси (" + wheelCount + " колес, " + suspensionType + " подвеска)";
    }
}

class Engine implements IProductPart {
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
}

// Шаги сборки
class BodyAssemblyLine implements ILineStep {
    private Random random = new Random();

    @Override
    public IProductPart buildProductPart() {
        String[] types = {"Седан", "Хэтчбек", "Универсал", "Купе"};
        String[] materials = {"Сталь", "Алюминий", "Карбон"};

        String type = types[random.nextInt(types.length)];
        String material = materials[random.nextInt(materials.length)];

        CarBody body = new CarBody(type, material);
        System.out.println("Создан кузов: " + body.getName());
        return body;
    }
}

class ChassisAssemblyLine implements ILineStep {
    private Random random = new Random();

    @Override
    public IProductPart buildProductPart() {
        int wheelCount = 4; // У автомобилей обычно 4 колеса
        String[] suspensions = {"Независимая", "МакФерсон", "Многорычажная"};

        String suspension = suspensions[random.nextInt(suspensions.length)];

        Chassis chassis = new Chassis(wheelCount, suspension);
        System.out.println("Создано шасси: " + chassis.getName());
        return chassis;
    }
}

class EngineAssemblyLine implements ILineStep {
    private Random random = new Random();

    @Override
    public IProductPart buildProductPart() {
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

// Продукт - автомобиль
class Car implements IProduct {
    private CarBody body;
    private Chassis chassis;
    private Engine engine;
    private boolean isAssembled = false;

    @Override
    public void installFirstPart(IProductPart part) {
        if (part instanceof CarBody) {
            this.body = (CarBody) part;
            System.out.println("Установлен кузов: " + part.getName());
        }
    }

    @Override
    public void installSecondPart(IProductPart part) {
        if (part instanceof Chassis) {
            this.chassis = (Chassis) part;
            System.out.println("Установлено шасси: " + part.getName());
        }
    }

    @Override
    public void installThirdPart(IProductPart part) {
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

    public boolean isAssembled() {
        return isAssembled;
    }
}

// Сборочная линия
class CarAssemblyLine implements IAssemblyLine {
    private ILineStep bodyStep;
    private ILineStep chassisStep;
    private ILineStep engineStep;

    public CarAssemblyLine(ILineStep bodyStep, ILineStep chassisStep, ILineStep engineStep) {
        this.bodyStep = bodyStep;
        this.chassisStep = chassisStep;
        this.engineStep = engineStep;
    }

    @Override
    public IProduct assembleProduct(IProduct product) {
        System.out.println("=== НАЧАЛО СБОРКИ АВТОМОБИЛЯ ===");

        // Сборка и установка кузова
        System.out.println("\n1. Сборка кузова:");
        IProductPart body = bodyStep.buildProductPart();
        product.installFirstPart(body);

        // Сборка и установка шасси
        System.out.println("\n2. Сборка шасси:");
        IProductPart chassis = chassisStep.buildProductPart();
        product.installSecondPart(chassis);

        // Сборка и установка двигателя
        System.out.println("\n3. Сборка двигателя:");
        IProductPart engine = engineStep.buildProductPart();
        product.installThirdPart(engine);

        System.out.println("\n=== СБОРКА ЗАВЕРШЕНА ===");
        return product;
    }
}

// Тестовый класс
class AssemblyLineTest {
    public static void main(String[] args) {
        // Создаем шаги сборки
        ILineStep bodyStep = new BodyAssemblyLine();
        ILineStep chassisStep = new ChassisAssemblyLine();
        ILineStep engineStep = new EngineAssemblyLine();

        // Создаем сборочную линию
        IAssemblyLine assemblyLine = new CarAssemblyLine(bodyStep, chassisStep, engineStep);

        // Создаем продукт (автомобиль)
        IProduct car = new Car();

        // Запускаем сборку
        IProduct assembledCar = assemblyLine.assembleProduct(car);

        // Выводим информацию о собранном автомобиле
        System.out.println("\n" + assembledCar.getInfo());
    }
}