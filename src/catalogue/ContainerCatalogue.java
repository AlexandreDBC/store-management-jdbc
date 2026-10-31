package catalogue;

import menu.Menu;
import menu.MenuOption;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class ContainerCatalogue extends HashSet<ContainerCatalogueItem> {
    public void addItemFromResultSet(ResultSet resultSet) throws SQLException {
        this.add(new ContainerCatalogueItem(resultSet.getInt("idarticle"), resultSet.getString("refcontenant"), resultSet.getString("typecontenant"), resultSet.getDouble("prixcontenant"), resultSet.getInt("stockcontenant"), resultSet.getDouble("capacitecontenant"), resultSet.getString("typecapacite"), resultSet.getBoolean("reutilisable")));
    }

    public ContainerCatalogueItem containerFromId(String containerId) {
        for (ContainerCatalogueItem item : this) {
            if (String.valueOf(item.idArticle()).equals(containerId)) return item;
        }
        return null;
    }

    public Menu toMenu() {
        Menu menu = new Menu();
        this.forEach(container -> menu.addOption(new MenuOption(String.valueOf(container.idArticle()), container.name() + " à " + container.sellPrice() + "€")));
        return menu;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ContainerCatalogueItem item : this) sb.append(item.toString()).append("\n\n");
        return sb.toString();
    }
}
