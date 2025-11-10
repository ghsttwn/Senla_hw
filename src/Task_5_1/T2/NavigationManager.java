package Task_5_1.T2;

import Task_5.T2.Menu;
import Task_5.T2.MenuItem;

import java.util.Stack;

public class NavigationManager {
    private static NavigationManager instance;
    private Stack<Task_5.T2.Menu> menuStack;

    private NavigationManager() {
        this.menuStack = new Stack<>();
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void pushMenu(Task_5.T2.Menu menu) {
        menuStack.push(menu);
    }

    public void popMenu() {
        if (!menuStack.isEmpty()) {
            menuStack.pop();
        }
    }

    public Task_5.T2.Menu getCurrentMenu() {
        return menuStack.isEmpty() ? null : menuStack.peek();
    }

    public void printCurrentMenu() {
        Task_5.T2.Menu currentMenu = getCurrentMenu();
        if (currentMenu != null) {
            currentMenu.printMenu();
        }
    }

    public void navigateTo(Task_5.T2.Menu menu, boolean showMenu) {
        pushMenu(menu);
        if (showMenu) {
            printCurrentMenu();
        }
    }

    public void navigateBack() {
        popMenu();
        if (!menuStack.isEmpty()) {
            printCurrentMenu();
        }
    }

    public void executeMenuItem(int index) {
        Menu currentMenu = getCurrentMenu();
        if (currentMenu == null) return;

        if (index == 0) {
            navigateBack();
            return;
        }

        if (index > 0 && index <= currentMenu.getMenuItems().size()) {
            MenuItem selectedItem = currentMenu.getMenuItems().get(index - 1);
            System.out.println("\n>>> " + selectedItem.getName());
            selectedItem.execute();

            // После выполнения действия снова показываем текущее меню
            if (!menuStack.isEmpty()) {
                System.out.println();
                printCurrentMenu();
            }
        } else {
            System.out.println("✗ Неверный выбор!");
        }
    }
}