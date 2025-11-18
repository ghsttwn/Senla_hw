package Task_6.T2;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private String title;
    private List<MenuItem> menuItems;

    public Menu(String title) {
        this.title = title;
        this.menuItems = new ArrayList<>();
    }

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public String getTitle() {
        return title;
    }

    public void printMenu() {
        System.out.println("\n=== " + title + " ===");
        for (int i = 0; i < menuItems.size(); i++) {
            System.out.println((i + 1) + ". " + menuItems.get(i).getName());
        }
        if (title.equals("ГОСТИНИЦА - ГЛАВНОЕ МЕНЮ")) {
            System.out.println("0. Выход");
        } else {
            System.out.println("0. Назад");
        }
    }
}