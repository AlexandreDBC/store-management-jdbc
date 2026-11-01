package commande;

public enum ModeRecuperation {
    RETRAIT("en boutique", 1), LIVRAISON("à domicile", 2), NON_RENSEIGNE("Non renseigné", 0);

    private final String libelle;
    private final int numero;

    ModeRecuperation(String libelle, int numero) {
        this.libelle = libelle;
        this.numero = numero;
    }

    public static void affModeRecuperation() {
        System.out.println("--- Modes de récupération disponibles ---");
        for (ModeRecuperation mode : ModeRecuperation.values()) {
            if (mode != ModeRecuperation.NON_RENSEIGNE) {
                System.out.println(mode.getNumero() + " - " + mode.getLibelle());
            }
        }
    }

    public static ModeRecuperation getByNumero(int choix) {
        for (ModeRecuperation mode : ModeRecuperation.values()) {
            if (mode.numero == choix) {
                return mode;
            }
        }
        return NON_RENSEIGNE;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public int getNumero() {
        return this.numero;
    }
}