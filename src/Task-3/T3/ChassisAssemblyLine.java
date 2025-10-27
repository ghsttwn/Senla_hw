import java.util.Random;

public class ChassisAssemblyLine implements Interfaces.ILineStep {
    private Random random = new Random();

    @Override
    public Interfaces.IProductPart buildProductPart() {
        int wheelCount = 4;
        String[] suspensions = {"Независимая", "МакФерсон", "Многорычажная"};

        String suspension = suspensions[random.nextInt(suspensions.length)];

        Chassis chassis = new Chassis(wheelCount, suspension);
        System.out.println("Создано шасси: " + chassis.getName());
        return chassis;
    }
}