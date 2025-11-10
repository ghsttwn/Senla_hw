package Task_5_1.T1;

import Task_5.T1.Action;

public class MenuItem {
    private String name;
    private Task_5.T1.Action action;

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
