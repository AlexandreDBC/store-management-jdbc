package catalogue;

public record ContainerCatalogueItem(int idArticle, String ref, String name, double sellPrice, int stock,
                                     double capacity, String capacityType, boolean reusable) {
    public String toString() {
        return name + " " + ref + " " + (reusable ? "réutilisable" : "à usage unique") + " (" + capacity + " " + capacityType + ")" + "\n└── " + stock + " restant à " + sellPrice + "€ l'unité";
    }
}