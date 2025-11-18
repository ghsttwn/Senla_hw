package Task_6.T1;

import Task_6.T1.Action;

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
