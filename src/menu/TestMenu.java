package menu;

import java.util.ArrayList;

public class TestMenu {
    public static void main(String[] args) {
        ArrayList<MenuOption> options = new ArrayList<>();
        options.add(new MenuOption("1", "Option 1"));
        options.add(new MenuOption("2", "Option 2"));
        options.add(new MenuOption("3", "Option 3"));
        options.add(new MenuOption("4", "Option 4"));
        menu.Menu menu = new menu.Menu(options);
        String selectedKey = menu.selectOne("Choisir un produit :");
        System.out.println("Tu as sélectionné : " + selectedKey);

        ArrayList<MenuOption> options1 = new ArrayList<>();
        options1.add(new MenuOption("prec", "Pré-conditionné"));
        options1.add(new MenuOption("vrac", "En vrac"));
        menu.Menu sizeMenu = new Menu(options1);
        String selectedSize = sizeMenu.selectOne("Choisir un conditionnement :");
        System.out.println("Tu as sélectionné : " + selectedSize);

    }
}
