package Task_6.T1;

import Task_6.T1.Menu;
import Task_6.T1.MenuItem;

import java.util.Stack;

public class NavigationManager {
    private static NavigationManager instance;
    private Stack<Menu> menuStack;

    private NavigationManager() {
        this.menuStack = new Stack<>();
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void pushMenu(Menu menu) {
        menuStack.push(menu);
    }

    public void popMenu() {
        if (!menuStack.isEmpty()) {
            menuStack.pop();
        }
    }

    public Menu getCurrentMenu() {
        return menuStack.isEmpty() ? null : menuStack.peek();
    }

    public void printCurrentMenu() {
        Menu currentMenu = getCurrentMenu();
        if (currentMenu != null) {
            currentMenu.printMenu();
        }
    }

    public void navigateTo(Menu menu, boolean showMenu) {
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