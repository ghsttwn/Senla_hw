package T3;

public class CarAssemblyLine implements Interfaces.IAssemblyLine {
    private Interfaces.ILineStep bodyStep;
    private Interfaces.ILineStep chassisStep;
    private Interfaces.ILineStep engineStep;

    public CarAssemblyLine(Interfaces.ILineStep bodyStep, Interfaces.ILineStep chassisStep, Interfaces.ILineStep engineStep) {
        this.bodyStep = bodyStep;
        this.chassisStep = chassisStep;
        this.engineStep = engineStep;
    }

    @Override
    public Interfaces.IProduct assembleProduct(Interfaces.IProduct product) {
        System.out.println("=== НАЧАЛО СБОРКИ АВТОМОБИЛЯ ===");

        System.out.println("\n1. Сборка кузова:");
        Interfaces.IProductPart body = bodyStep.buildProductPart();
        product.installFirstPart(body);

        System.out.println("\n2. Сборка шасси:");
        Interfaces.IProductPart chassis = chassisStep.buildProductPart();
        product.installSecondPart(chassis);

        System.out.println("\n3. Сборка двигателя:");
        Interfaces.IProductPart engine = engineStep.buildProductPart();
        product.installThirdPart(engine);

        System.out.println("\n=== СБОРКА ЗАВЕРШЕНА ===");
        return product;
    }

    public Interfaces.ILineStep getBodyStep() { return bodyStep; }
    public Interfaces.ILineStep getChassisStep() { return chassisStep; }
    public Interfaces.ILineStep getEngineStep() { return engineStep; }
}