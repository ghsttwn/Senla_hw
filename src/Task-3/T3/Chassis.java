import java.util.Objects;

public class Chassis implements Interfaces.IProductPart {
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

    public int getWheelCount() { return wheelCount; }
    public String getSuspensionType() { return suspensionType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chassis chassis = (Chassis) o;
        return wheelCount == chassis.wheelCount &&
                Objects.equals(suspensionType, chassis.suspensionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wheelCount, suspensionType);
    }

    @Override
    public String toString() {
        return "Chassis{" +
                "wheelCount=" + wheelCount +
                ", suspensionType='" + suspensionType + '\'' +
                '}';
    }
}