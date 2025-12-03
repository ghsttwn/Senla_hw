package Task_7.T1;

public class MenuItem {
    private String name;
    private Action action;

    public MenuItem(String name, Action action) {
        this.name = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void execute() {
        action.execute();
    }

    @Override
    public String toString() {
        return name;
    }
}
