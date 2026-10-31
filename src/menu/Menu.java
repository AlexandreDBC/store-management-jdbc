package menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private final ArrayList<MenuOption> options;

    public Menu(ArrayList<MenuOption> options) {
        this.options = new ArrayList<>(options);
    }
    public Menu(MenuOption... options) {
        this.options = new ArrayList<>(List.of(options));
    }

    public void addOption(MenuOption option) {
        options.add(option);
    }

    private void displayChoices() {
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + 1 + " - " + options.get(i));
        }
    }

    public void displayChoice(int i) {
        System.out.println(i + 1 + " - " + options.get(i));
    }

    public String selectOne(String question) {
        System.out.println(question);
        displayChoices();
        Scanner sc = new Scanner(System.in);
        int n;
        while (true) {
            try {
                String line = sc.nextLine();
                n = Integer.parseInt(line);
                if (n > 0 && n <= options.size()) break;
            } catch (NumberFormatException e) {
            }
            System.out.println("Erreur : cette option n'est pas un numéro valide");
        }
        return options.get(n - 1).getKey();
    }

    public String selectOneSansAffichage() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Sélectionner une commande à clôturer : ");
        int n;
        while (true) {
            try {
                String line = sc.nextLine();
                n = Integer.parseInt(line);
                if (n > 0 && n <= options.size()) break;
            } catch (NumberFormatException e) {
            }
            System.out.println("Erreur : cette option n'est pas un numéro valide");
        }
        return options.get(n - 1).getKey();
    }
}
