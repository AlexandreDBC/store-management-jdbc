package catalogue;

import menu.Menu;
import menu.MenuOption;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class ProductCatalogue extends HashMap<String, HashMap<String, CatalogueProductItem>> {
    public void addItemFromResultSet(ResultSet resultSet) throws SQLException {
        String productCategory = resultSet.getString("categorie");
        if (!this.containsKey(productCategory)) this.put(productCategory, new HashMap<>());
        String productId = resultSet.getString("idproduit");
        if (!this.get(productCategory).containsKey(productId)) {
            this.get(productCategory).put(productId, new CatalogueProductItem(resultSet.getString("nomproduit"), resultSet.getString("description"), resultSet.getInt("delaidisponibilite"), resultSet.getInt("jourdebutdispo"), resultSet.getInt("jourfindispo"), new HashSet<>()));
        }
        this.get(productCategory).get(productId).getVariantes().add(new CatalogueVarianteProduct(resultSet.getInt("idarticle"), resultSet.getDouble("prixvente"), Objects.equals(resultSet.getString("typeproduit"), "Pré-conditionné"), resultSet.getDouble("poids")));
    }

    public Menu toMenu() {
        Menu menu = new Menu();
        this.forEach((categorie, items) -> {
            items.forEach((id, product) -> menu.addOption(new MenuOption(id, product.toShortString())));
        });
        return menu;
    }

    public Menu toProductVariantesMenu(String productId) {
        Menu menu = new Menu();
        this.forEach((categorie, items) -> {
            items.forEach((id, product) -> {
                if (productId.equals(id)) {
                    product.variantes().forEach(variante -> menu.addOption(new MenuOption(String.valueOf(variante.articleId()), variante.toString())));
                }
            });
        });
        return menu;
    }

    public CatalogueProductItem productItemFromId(String productId) {
        for (HashMap<String, CatalogueProductItem> category : this.values()) {
            if (category.containsKey(productId)) return category.get(productId);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.forEach((k, v) -> {
            sb.append("\n-------------------------------------\n");
            sb.append(k.toUpperCase() + "\n");
            sb.append("-------------------------------------\n");
            v.forEach((k2, v2) -> sb.append(v2).append("\n"));
        });
        return sb.toString();
    }
}
