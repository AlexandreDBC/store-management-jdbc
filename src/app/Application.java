package app;

import bdd.JDBC;
import catalogue.*;
import commande.Commande;
import commande.LigneCommande;
import commande.ModePaiement;
import commande.ModeRecuperation;
import service.livraisonChecker;
import menu.Menu;
import menu.MenuOption;
import utils.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

public class Application {
    private final Scanner sc;
    JDBC jdbc = new JDBC();
    private int client;

    public Application() throws SQLException, InterruptedException {
        this.sc = new Scanner(System.in);
        home();
        jdbc.close();
    }

    public void home() throws SQLException, InterruptedException {
        connection();
        Menu mainMenu = new Menu(new MenuOption("catalogue", "Consulter le catalogue"), new MenuOption("order", "Passer une commande"), new MenuOption("warnings", "Consulter les alertes de péremption"), new MenuOption("close_order", "Clôturer une commande"), new MenuOption("exit", "Quitter l'application"));
        while (true) {
            switch (mainMenu.selectOne("Que voulez vous faire ?")) {
                case "catalogue":
                    showCatalogue();
                    break;
                case "order":
                    passerCommande();
                    break;
                case "warnings":
                    consulterAlertePeremption();
                    break;
                case "close_order":
                    closeOrder();
                    break;
                case "exit":
                    System.exit(0);
                default:
                    throw new Error("Choix impossible");
            }
        }
    }

    private void showCatalogue() throws SQLException {
        Menu catalogueTypeMenu = new Menu(new MenuOption("products", "Consulter les produits"), new MenuOption("container", "Consulter les contenants"));
        switch (catalogueTypeMenu.selectOne("Que souhaitez-vous voir ?")) {
            case "products":
                showProductCatalogue();
                break;
            case "container":
                showContainerCatalogue();
                break;
            default:
                throw new Error("Choix impossible");
        }
    }

    private void showContainerCatalogue() throws SQLException {
        System.out.println("Chargement du catalogue...");
        ResultSet resultSet = jdbc.ask("SELECT * FROM contenant");
        ContainerCatalogue catalogueItems = new ContainerCatalogue();
        while (resultSet.next()) {
            catalogueItems.addItemFromResultSet(resultSet);
        }
        System.out.println(catalogueItems);
    }

    private void showProductCatalogue() throws SQLException {
        // TODO: Gérer qu'un produit peut avoir plusieurs saisonnalités
        System.out.println("Chargement du catalogue...");
        ResultSet resultSet = jdbc.ask("SELECT * FROM PRODUIT p, VARIANTEPRODUIT v, SAISONNALITÉS s WHERE p.IDPRODUIT = v.IDPRODUIT AND s.IDPRODUIT = p.IDPRODUIT");
        ProductCatalogue catalogueItems = new ProductCatalogue();
        while (resultSet.next()) {
            catalogueItems.addItemFromResultSet(resultSet);
        }
        System.out.println(catalogueItems);
    }

    private void closeOrder() throws SQLException {
        try {
            this.jdbc.connection.setAutoCommit(false);
            System.out.println("--- Liste de vos commandes en cours ---");
            ResultSet commandesResultSet = jdbc.ask("SELECT * FROM commande WHERE idclient = " + client + " AND  statutcommande IN ('prête','en livraison')");
            ResultSet lignesResultSet;

            int i = 0;
            Menu menuCommandes = new Menu();
            if (!(commandesResultSet.next())) {
                System.out.println("Aucune commande à cloturer");
                return;
            }
            do {
                String displayName = commandesResultSet.getString("datecommande");
                menuCommandes.addOption(new MenuOption(commandesResultSet.getString("idCommande"), displayName));
                menuCommandes.displayChoice(i);
                System.out.println("Statut de la commande : " + commandesResultSet.getString("statutcommande"));
                lignesResultSet = jdbc.ask("SELECT * FROM lignecommande WHERE idcommande  = " + commandesResultSet.getString("idcommande"));
                while (lignesResultSet.next()) {
                    System.out.println(lignesResultSet.getString("idarticle") + " - quantité commande : " + lignesResultSet.getString("quantiteCommande") + " prix unitaire : " + lignesResultSet.getString("prixunitaire"));
                }
                i++;
            } while (commandesResultSet.next());

            int idCmd = Integer.parseInt(menuCommandes.selectOneSansAffichage());
            ResultSet setCmdCloture = jdbc.ask("SELECT * FROM commande WHERE idCommande =" + idCmd);
            setCmdCloture.next();
            jdbc.ask("UPDATE commande.Commande " + "SET statutCommande = 'récupérée/livrée'" + "WHERE idCommande = " + idCmd);
            if (setCmdCloture.getString("modedepayement").equals(ModePaiement.BOUTIQUE.getLibelle())) {
                jdbc.ask("UPDATE commande.Commande " + "SET payementCommande = 1 " + "WHERE idCommande = " + idCmd);
            }
            jdbc.ask("UPDATE commande.Commande " + "SET dateRecuperation = SYSDATE " + "WHERE idCommande = " + idCmd);
            // Pour un retour à la ligne
            System.out.println();
        } catch (Exception e) {
            jdbc.connection.rollback(); // annulation en cas d’erreur
        } finally {
            jdbc.connection.setAutoCommit(true);
        }
    }

    // Méthode qui gère les alertes de péremption et qui fait la réduction automatiquement selon la date entré par l'utilisateur
    private void consulterAlertePeremption() throws SQLException {
        try {
            jdbc.connection.setAutoCommit(false);
            String dateEnString;
            System.out.print("Tapez l'année à simuler : ");
            dateEnString = this.sc.nextLine();
            if (Integer.parseInt(dateEnString) <= 0 || Integer.parseInt(dateEnString) > 9999) {
                System.out.println("Année incohérente");
                throw new Exception("Année incohérente : " + dateEnString);
            }
            System.out.print("Tapez le mois à simuler : ");
            String mois = this.sc.nextLine();
            dateEnString += "-" + mois;
            if (Integer.parseInt(mois) < 0 || Integer.parseInt(mois) > 12) {
                System.out.println("Mois incohérent");
                throw new Exception("Mois incohérent : " + mois);
            }
            System.out.print("Tapez le jour à simuler : ");
            String jour = this.sc.nextLine();
            dateEnString += "-" + jour;
            if (Integer.parseInt(jour) < 0 || Integer.parseInt(jour) > 31) {
                System.out.println("Jour incohérent");
                throw new Exception("Jour incohérent : " + jour);
            }

            System.out.println("\n--- Liste des alertes de péremption ---");
            ResultSet result = jdbc.ask("SELECT vp.idArticle, p.nomProduit, vp.prixVente AS prix_actuel, vp.prixVente * 0.7 AS nouveau_prix_estime, l.dateLimiteLot, l.quantitelot, l.typeDateLimiteLot " + "FROM Lot l, VarianteProduit vp, Produit p " + "WHERE l.idArticle = vp.idArticle AND vp.idProduit = p.idProduit " + " AND l.quantitelot > 0" + " AND l.dateLimiteLot >= TO_DATE('" + dateEnString + "', 'YYYY-MM-DD HH24:MI:SS') " + " AND l.dateLimiteLot <= TO_DATE('" + dateEnString + "', 'YYYY-MM-DD HH24:MI:SS') + 7" + " ORDER BY l.dateLimiteLot ASC");


            if (!(result.next())) {
                System.out.println("Aucune alerte de péremption à cette date : " + dateEnString);
                return;
            }
            System.out.printf("%-10s %-30s %-12s %-15s %-20s %-10s %-15s%n", "IDARTICLE", "NOM_PRODUIT", "PRIX_ACTUEL", "NOUVEAU_PRIX", "DATE_LIMITE", "QUANTITE", "TYPE_DATE");
            do {
                String idArticle = result.getString("idArticle");
                String nomProduit = result.getString("nomProduit");
                double prixActuel = result.getDouble("prix_actuel");
                double nouveauPrix = result.getDouble("nouveau_prix_estime");
                java.sql.Date dateLimite = result.getDate("dateLimiteLot");
                int quantite = result.getInt("quantiteLot");
                String typeDate = result.getString("typeDateLimiteLot");

                System.out.printf("%-10s %-30s %-12.2f %-15.2f %-20s %-10d %-15s%n", idArticle, nomProduit, prixActuel, nouveauPrix, dateLimite, quantite, typeDate);
            } while (result.next());
            jdbc.ask("UPDATE VarianteProduit" + " SET prixVente = prixVente * 0.7" + " WHERE idArticle IN (" + "    SELECT DISTINCT l.idArticle" + "    FROM Lot l" + "    WHERE l.quantiteLot > 0" + "        AND l.dateLimiteLot >= TO_DATE('" + dateEnString + "', 'YYYY-MM-DD HH24:MI:SS') " + "        AND l.dateLimiteLot <= TO_DATE('" + dateEnString + "', 'YYYY-MM-DD HH24:MI:SS') + 7" + ")");
        } catch (Exception e) {
            jdbc.connection.rollback(); //   annulation en cas d’erreur
            System.out.println("Entrez une date cohérente");
        } finally {
            jdbc.connection.setAutoCommit(true);
        }
    }

    private void passerCommande() throws SQLException, InterruptedException {
        Commande commandeEnCours = new Commande();
        System.out.println("Chargement du catalogue...");
        // ============ CATALOGUE PRODUITS ============  
        ResultSet productResultSet = jdbc.ask("SELECT * FROM PRODUIT p, VARIANTEPRODUIT v, SAISONNALITÉS s WHERE p.IDPRODUIT = v.IDPRODUIT AND s.IDPRODUIT = p.IDPRODUIT");
        ProductCatalogue productCatalogueItems = new ProductCatalogue();
        // Parsing
        while (productResultSet.next()) {
            productCatalogueItems.addItemFromResultSet(productResultSet);
        }
        // Construction du menu des produits
        Menu productCatalogueMenu = productCatalogueItems.toMenu();
        productCatalogueMenu.addOption(new MenuOption("containers", "Ajouter des contenants."));
        productCatalogueMenu.addOption(new MenuOption("stop", "Arrêter la prise de commande."));
        // ============ CATALOGUE CONTENANT ============  
        ResultSet containerResultSet = jdbc.ask("SELECT * FROM CONTENANT");
        ContainerCatalogue containerCatalogueItems = new ContainerCatalogue();
        // Parsing
        while (containerResultSet.next()) {
            containerCatalogueItems.addItemFromResultSet(containerResultSet);
        }
        // Construction du menu des contenants
        Menu containerCatalogueMenu = containerCatalogueItems.toMenu();
        containerCatalogueMenu.addOption(new MenuOption("products", "Ajouter des produits."));
        containerCatalogueMenu.addOption(new MenuOption("stop", "Arrêter la prise de commande."));

        int toDo = 1;
        while (toDo != 0) {
            toDo = switch (toDo) {
                case 1 -> choisirProduit(commandeEnCours, productCatalogueMenu, productCatalogueItems);
                case 2 -> choisirContenant(commandeEnCours, containerCatalogueMenu, containerCatalogueItems);
                default -> toDo;
            };
        }
        if (commandeEnCours.getLignesCommande().isEmpty()) {
            System.out.println("Aucune produit dans la commande");
            System.out.println("Annulation de la commande");
            Utils.sleep(2);
            return;
        }
        choisirModePaiement(commandeEnCours);
        choisirModeRecuperation(commandeEnCours);

        Menu validateOrder = new Menu(new MenuOption("yes", "Valider la création de la commande"), new MenuOption("no", "Annuler"));

        while (true) {
            try {
                jdbc.connection.setAutoCommit(false); // début de la transaction
                System.out.println("--- Récapitulatif de la commande ---");
                System.out.println(commandeEnCours);
                String choice = validateOrder.selectOne("Voulez vous confirmer la création de la commande ?");
                if (choice.equals("no")) {
                    break;
                }
                HashMap<String, Double> quantities = new HashMap<>();
                HashMap<String, Double> prices = new HashMap<>();
                // lecture des stocks des produits
                if (commandeEnCours.getLignesCommande().values().stream().anyMatch(LigneCommande::getIsProduct)) {
                    ResultSet productQuantities = jdbc.ask("SELECT l.IDARTICLE, l.QUANTITELOT, v.PRIXVENTE FROM LOT l, VARIANTEPRODUIT v WHERE v.IDARTICLE IN (" + commandeEnCours.getIdsToString(true) + ") AND v.IDARTICLE = l.IDARTICLE");
                    while (productQuantities.next()) {
                        String productId = productQuantities.getString("IDARTICLE");
                        double quantity = productQuantities.getDouble("QUANTITELOT");
                        double price = productQuantities.getDouble("PRIXVENTE");
                        quantities.put(productId, quantity);
                        prices.put(productId, price);
                    }
                }
                // lecture des stocks des contenants
                if (commandeEnCours.getLignesCommande().values().stream().anyMatch(lc -> !lc.getIsProduct())) {
                    ResultSet containerQuantities = jdbc.ask("SELECT IDARTICLE, STOCKCONTENANT, PRIXCONTENANT FROM Contenant WHERE IDARTICLE IN (" + commandeEnCours.getIdsToString(false) + ")");
                    while (containerQuantities.next()) {
                        String containerId = containerQuantities.getString("IDARTICLE");
                        double quantity = containerQuantities.getDouble("STOCKCONTENANT");
                        double price = containerQuantities.getDouble("PRIXCONTENANT");
                        quantities.put(containerId, quantity);
                        prices.put(containerId, price);
                    }
                }
                // Vérification des quantités
                for (String key : quantities.keySet()) {
                    Double currentPrice = prices.get(key);
                    Double quantity = quantities.get(key);
                    LigneCommande line = commandeEnCours.getLignesCommande().get(Integer.valueOf(key));
                    if (currentPrice != line.getUnitaryPrice()) {
                        line.updateUnitaryPrice(currentPrice);
                        System.out.println("/!\\ ATTENTION : L'article " + "\"" + line.getDisplayName() + "\" a changé de prix.");
                    }
                    // TODO : au lieu d'annuler on enlève line dans commandeEnCours
                    if (quantity < line.getQuantite()) {
                        // L'article n'est plus disponible
                        System.out.println("/!\\ ATTENTION : L'article " + "\"" + line.getDisplayName() + "\" n'est plus disponible dans les quantités souhaitées.\nAnnulation de la commande.");
                        throw new Exception("Quantité insuffisante pour : " + key);
                    }
                }
                // enregistrement de la commande
                // INSERT INTO COMMANDE
                LocalDateTime aujourdHui = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String dateAjdFormatted = aujourdHui.format(formatter);

                ResultSet lastIdResultSet = jdbc.ask("SELECT IDCOMMANDE FROM commande.Commande ORDER BY IDCOMMANDE DESC FETCH FIRST 1 ROW ONLY");
                lastIdResultSet.next();
                int idCommande = Integer.parseInt(lastIdResultSet.getString("IDCOMMANDE")) + 1;
                int statutModePayement = (commandeEnCours.getModePaiement().getLibelle().equals("en ligne") ? 1 : 0);
                jdbc.ask("INSERT INTO commande.Commande (idCommande, DATECOMMANDE, STATUTCOMMANDE, MODEDERECUPERATION, MODEDEPAYEMENT, IDCLIENT,PAYEMENTCOMMANDE) " + "VALUES (" + idCommande + ", " + "TO_DATE('" + dateAjdFormatted + "', 'DD-MM-YYYY HH24:MI:SS'), " + "'en préparation', " + "'" + commandeEnCours.getModeRecuperation().getLibelle() + "', " + "'" + commandeEnCours.getModePaiement().getLibelle() + "', " + this.client + ", " + statutModePayement + ")");

                double totalAllProduit = 0;
                double poidsCommande = 0;
                int delaiDisponibilite = 0;
                // insert dans les lignes de commande
                for (String key : quantities.keySet()) {
                    LigneCommande line = commandeEnCours.getLignesCommande().get(Integer.valueOf(key));
                    jdbc.ask("INSERT INTO commande.LigneCommande ( idCommande, numLigneCommande, quantiteCommande, prixUnitaire, idArticle) " + "VALUES (" + idCommande + ", " + Integer.valueOf(key) + ", " + line.getQuantite() + ", " + line.getUnitaryPrice() + "," + line.getIdProduit() + ")");

                    // j'en profite pour récup le prix total
                    totalAllProduit += line.getQuantite() * line.getUnitaryPrice();
                    poidsCommande += line.getPoids();
                    if (line.getDelaiDispo() > delaiDisponibilite) delaiDisponibilite = line.getDelaiDispo();
                }

                System.out.println("Mode de paiement : " + commandeEnCours.getModePaiement().getLibelle() + "\nMode de récupération : " + commandeEnCours.getModeRecuperation().getLibelle());

                System.out.println("Voici la somme de tous vos produits : " + totalAllProduit);

                if (commandeEnCours.getModeRecuperation().getLibelle().equals("à domicile")) {
                    double fraisLivraison = 0;

                    ResultSet resultSet = jdbc.ask("SELECT * FROM adresse WHERE idclient =" + this.client);

                    resultSet.next();

                    String pays = resultSet.getString("pays");
                    String adresse = resultSet.getString("adresse");

                    livraisonChecker livraisonChecker = new livraisonChecker();
                    fraisLivraison = livraisonChecker.getTax("france", poidsCommande);
                    String dateLivraison;
                    dateLivraison = livraisonChecker.getDateLivraison(pays, delaiDisponibilite);

                    jdbc.ask("INSERT INTO COMMANDELIVRAISON (idcommande,fraislivraison,datelivraisonestimee,poidscommande,payslivraison,adresse,idclient) " + "VALUES (" + idCommande + "," + fraisLivraison + ",TO_DATE('" + dateLivraison + "', 'YYYY-MM-DD')," + poidsCommande + ",'" + pays + "','" + adresse + "'," + this.client + ") ");

                    System.out.println("Voici les frais de livraison : " + fraisLivraison);
                    System.out.println("Voici la date de livraison estimé : " + dateLivraison);

                    double prixTotal = fraisLivraison + totalAllProduit;
                    System.out.println("Prix total : " + prixTotal);
                }
                // commit
                jdbc.connection.commit();
                break;
            } catch (Exception e) {
                jdbc.connection.rollback(); // annulation en cas d’erreur
            } finally {
                jdbc.connection.setAutoCommit(true);
            }
        }
    }

    public int choisirProduit(Commande commande, Menu menuCatalogue, ProductCatalogue catalogueItems) throws SQLException, InterruptedException {
        String selectedProductId = menuCatalogue.selectOne("--- Liste des produits ---");
        if (selectedProductId.equals("stop")) {
            return 0;
        } else if (selectedProductId.equals("containers")) {
            return 2;
        }
        CatalogueProductItem product = catalogueItems.productItemFromId(selectedProductId);
        // Vérification de la disponibilité du produit
        // TODO: Permettre plusieurs disponibilités
        Integer intDay = LocalDate.now().getDayOfYear();
        if (!Utils.isYearDayOfTheYearBetweenDays(intDay, product.seasonalityStart(), product.seasonalityEnd())) {
            System.out.println("Ce produit n'est pas disponible actuellement.");
            Utils.sleep(2);
            return 1;
        }
        // Choix des variantes associées au produit
        Menu varianteMenu = catalogueItems.toProductVariantesMenu(selectedProductId);
        String selectedVarianteId = varianteMenu.selectOne("Choisir une des variantes disponibles :");
        CatalogueVarianteProduct variante = null;
        for (CatalogueVarianteProduct v : product.variantes()) {
            if (selectedVarianteId.equals(String.valueOf(v.articleId()))) variante = v;
        }
        // Choix de la quantité
        ResultSet articleQuantityResultSet = jdbc.ask("SELECT SUM(l.QUANTITELOT) as QUANTITELOT FROM LOT l WHERE l.IDARTICLE = " + selectedVarianteId);
        articleQuantityResultSet.next();
        Integer maxQuantity = articleQuantityResultSet.getInt("QUANTITELOT");
        System.out.println("Combien souhaitez vous de " + product.name() + " ?");
        System.out.println(variante.isPreCond() ? "Indiquez un nombre d'unités pour cet article pré-conditionné." : "Indiquez un poids en kg pour cet article en vrac.");

        Number selectedQuantity;

        while (true) {
            // TODO: A verifier que ca marche: le choix de 0.5 kg (nombre non entier)
            try {
                System.out.println("Merci de choisir un nombre entre 0 et " + maxQuantity + ".");
                if (variante.isPreCond()) {
                    selectedQuantity = Integer.parseInt(sc.nextLine());
                } else {
                    selectedQuantity = Double.parseDouble(sc.nextLine());
                }
                if (selectedQuantity.doubleValue() <= maxQuantity && selectedQuantity.doubleValue() >= 0) break;
            } catch (NumberFormatException e) {
                System.out.println("Veuillez rentrer un nombre valide.");
            }
        }
        if (selectedQuantity.doubleValue() > 0) {
            ResultSet delaiDisponResultSet = jdbc.ask("SELECT p.delaiDisponibilite " + "FROM Produit p " + "JOIN VarianteProduit vp " + "    ON p.idProduit = vp.idProduit " + "WHERE vp.idarticle = " + selectedVarianteId);
            delaiDisponResultSet.next();
            int delaiDisponibilite = delaiDisponResultSet.getInt("delaiDisponibilite");
            if (variante.isPreCond()) {
                // récup le poids de la variante
                ResultSet variantePoids = jdbc.ask("SELECT poids FROM varianteproduit WHERE idarticle = " + selectedVarianteId);
                variantePoids.next();
                double poids = variantePoids.getDouble("poids") * selectedQuantity.doubleValue();
                commande.addArticle(Integer.parseInt(selectedVarianteId), product.name(), selectedQuantity.intValue(), true, variante.sellPrice(), poids, delaiDisponibilite);
            } else {
                commande.addArticle(Integer.parseInt(selectedVarianteId), product.name(), selectedQuantity.doubleValue(), true, variante.sellPrice(), selectedQuantity.doubleValue(), delaiDisponibilite);
            }
            System.out.println("Article \"" + product.name() + "\" ajouté à la commande !");
        } else {
            System.out.println("Annulation");
        }
        Utils.sleep(1);
        return 1;
    }

    public int choisirContenant(Commande commande, Menu menuCatalogue, ContainerCatalogue catalogueItems) throws SQLException, InterruptedException {
        String containerId = menuCatalogue.selectOne("--- Liste des contenants ---");
        if (containerId.equals("stop")) {
            return 0;
        } else if (containerId.equals("products")) {
            return 1;
        }
        ContainerCatalogueItem container = catalogueItems.containerFromId(containerId);
        // Choix de la quantité
        Integer maxQuantity = container.stock();
        System.out.println("Combien souhaitez vous de " + container.name() + " ?");
        int selectedQuantity;
        while (true) {
            try {
                System.out.println("Merci de choisir un nombre entre 0 et " + maxQuantity + ".");
                selectedQuantity = Integer.parseInt(sc.nextLine());
                if (selectedQuantity <= maxQuantity && selectedQuantity >= 0) break;
            } catch (NumberFormatException e) {
                System.out.println("Veuillez rentrer un nombre valide.");
            }
        }
        if (selectedQuantity > 0) {
            commande.addArticle(Integer.parseInt(containerId), container.name(), selectedQuantity, false, container.sellPrice(), 0.0, 0);
            System.out.println("Article \"" + container.name() + "\" ajouté à la commande !");
        } else {
            System.out.println("Annulation");
        }
        Utils.sleep(1);
        return 2;
    }

    public void choisirModePaiement(Commande commande) {
        int selectedChoice;
        ModePaiement.displayPaymentMethods();
        while (true) {
            try {
                System.out.println("Sélectionnez un mode de paiement : ");
                selectedChoice = Integer.parseInt(this.sc.nextLine());
                ModePaiement modePaiement = ModePaiement.getByNumero(selectedChoice);
                if (selectedChoice <= 0 || modePaiement == ModePaiement.NON_RENSEIGNE)
                    System.out.println("Choix incorrect, veuillez réessayer.");
                else {
                    commande.setModePaiement(modePaiement);
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez rentrer un nombre.");
            }
        }
    }

    public void choisirModeRecuperation(Commande commande) {
        int selectedChoice;
        ModeRecuperation.affModeRecuperation();
        while (true) {
            try {
                System.out.println("Choisissez un mode de récupération :");
                selectedChoice = Integer.parseInt(this.sc.nextLine());
                ModeRecuperation modeRecuperation = ModeRecuperation.getByNumero(selectedChoice);
                if (selectedChoice <= 0 || modeRecuperation == ModeRecuperation.NON_RENSEIGNE)
                    System.out.println("Choix incorrect, veuillez réessayer");
                else {
                    commande.setModeRecuperation(modeRecuperation);
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez rentrer un nombre");
            }
        }
    }

    public void connection() throws SQLException {
        System.out.println("Veuillez taper votre e-mail (ex: jean.dupont@mail.fr) : ");
        String mail;
        ResultSet resultSet;
        while (true) {
            mail = this.sc.nextLine();
            resultSet = jdbc.ask("SELECT idclient FROM client WHERE emailclient = '" + mail + "'");
            if (!(resultSet.next())) System.out.println("Email non connu en base de donnée, merci de réessayer");
            else {
                System.out.println("\nVous êtes connecté avec l'email : " + mail + "\n");
                this.client = resultSet.getInt("idclient");
                break;
            }
        }
    }
}