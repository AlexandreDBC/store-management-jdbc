package commande;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Commande {
    private final HashMap<Integer, LigneCommande> lignesCommande;
    private ModePaiement modePaiement;
    private ModeRecuperation modeRecuperation;

    public Commande() {
        this.lignesCommande = new HashMap<>();
        this.modePaiement = ModePaiement.NON_RENSEIGNE;
        this.modeRecuperation = ModeRecuperation.NON_RENSEIGNE;
    }

    public void addArticle(int idArticle, String displayName, double qty, boolean isProduct, double price, double poids, int delaiDisponibilite) {
        if (this.lignesCommande.containsKey(idArticle)) {
            double previousQuantity = this.lignesCommande.get(idArticle).getQuantite();
            this.lignesCommande.put(idArticle, new LigneCommande(idArticle, displayName, qty + previousQuantity, isProduct, price, poids, delaiDisponibilite));
        }
        this.lignesCommande.put(idArticle, new LigneCommande(idArticle, displayName, qty, isProduct, price, poids, delaiDisponibilite));
    }

    public String getIdsToString(boolean productsOnly) {
        return lignesCommande.values().stream().filter(lc -> lc.getIsProduct() == productsOnly).map(lc -> "'" + lc.getIdProduit() + "'").collect(Collectors.joining(","));
    }

    public HashMap<Integer, LigneCommande> getLignesCommande() {
        return this.lignesCommande;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public ModeRecuperation getModeRecuperation() {
        return modeRecuperation;
    }

    public void setModeRecuperation(ModeRecuperation modeRecuperation) {
        this.modeRecuperation = modeRecuperation;
    }

    public double getTotalPrice() {
        return this.lignesCommande.values().stream().mapToDouble(lc ->
                lc.getQuantite() * lc.getUnitaryPrice()).sum();
    }

    public String toString() {
        String s = "Liste de produits : \n";
        for (Map.Entry<Integer, LigneCommande> entry : this.lignesCommande.entrySet()) {
            s += entry.getKey() + " - " + entry.getValue().toString() + " \n";
        }
        s += "\nMode de paiement : " + this.modePaiement.getLibelle() + "\n";
        s += "Mode de récuperation : " + this.modeRecuperation.getLibelle() + "\n";

        s += "\nSous total : " + this.getTotalPrice() + "\n";
        return s;
    }
}
