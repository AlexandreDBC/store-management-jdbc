package commande;

public enum ModePaiement {
    LIGNE("en ligne", 1), BOUTIQUE("en boutique", 2), NON_RENSEIGNE("Non renseigné", 0);

    private final String libelle;
    private final int numero;

    ModePaiement(String libelle, int numero) {
        this.libelle = libelle;
        this.numero = numero;
    }

    public static void displayPaymentMethods() {
        System.out.println("--- Modes de paiements disponibles ---");
        for (ModePaiement mode : ModePaiement.values()) {
            if (mode != ModePaiement.NON_RENSEIGNE) {
                System.out.println(mode.getNumero() + " - " + mode.getLibelle());
            }
        }
    }

    public static ModePaiement getByNumero(int choix) {
        for (ModePaiement mode : ModePaiement.values()) {
            if (mode.numero == choix) {
                return mode;
            }
        }
        return NON_RENSEIGNE;
    }

    public String getLibelle() {
        return libelle;
    }

    public int getNumero() {
        return numero;
    }
}