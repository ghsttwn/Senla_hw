class AssemblyLineTest {
    public static void main(String[] args) {
        // Создаем шаги сборки
        Interfaces.ILineStep bodyStep = new BodyAssemblyLine();
        Interfaces.ILineStep chassisStep = new ChassisAssemblyLine();
        Interfaces.ILineStep engineStep = new EngineAssemblyLine();

        // Создаем сборочную линию
        Interfaces.IAssemblyLine assemblyLine = new CarAssemblyLine(bodyStep, chassisStep, engineStep);

        // Создаем продукт (автомобиль)
        Interfaces.IProduct car = new Car();

        // Запускаем сборку
        Interfaces.IProduct assembledCar = assemblyLine.assembleProduct(car);

        // Выводим информацию о собранном автомобиле
        System.out.println("\n" + assembledCar.getInfo());
    }
}