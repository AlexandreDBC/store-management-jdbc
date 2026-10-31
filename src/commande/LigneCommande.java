package commande;

public class LigneCommande {
    private final int idProduit;
    private final double quantite;
    private final String displayName;
    private final boolean isProduct;
    private double unitaryPrice = 0;
    private final double poids;
    private final int delaiDispo;

    public LigneCommande(int idProduit, String displayName, double quantite, boolean isProduct, double price, double poids, int delaiDispo) {
        this.idProduit = idProduit;
        this.displayName = displayName;
        this.quantite = quantite;
        this.isProduct = isProduct;
        this.unitaryPrice = price;
        this.poids = poids;
        this.delaiDispo = delaiDispo;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public double getQuantite() {
        return quantite;
    }

    public double getPoids() {
        return poids;
    }

    public int getDelaiDispo() {
        return delaiDispo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean getIsProduct() {
        return isProduct;
    }

    public void updateUnitaryPrice(double newPrice) {
        this.unitaryPrice = newPrice;
    }

    public double getUnitaryPrice() {
        return unitaryPrice;
    }

    // Important pour l'affichage
    @Override
    public String toString() {
        return this.displayName + " (x" + quantite + ") à " + unitaryPrice + "€";
    }
}