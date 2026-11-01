<<<<<<< HEAD
# store-management-jdbc
Java application using JDBC to connect to a relational database, implementing a complete store management system (products, orders, deliveries, and catalog features).
=======
# Projet Base de données

## Compilation et exécution

### 1. **Prérequis**
*   Java Development Kit (JDK) version 17 ou supérieure.
*   ojdbc17.jar

### 2. **Compiler le projet**
Pour compiler l'ensemble des fichiers source :
```bash
make
```
Cette commande créera un dossier `out/` contenant tous les fichiers `.class`.

### 3. **Exécuter le projet**

```bash
make start
```
Cette commande démarrera l'application.

## Utilisation du démonstrateur

Le démonstrateur propose une interface dans le terminal.
On peut intéragir avec celle-ci en entrant les numéros correspondant aux choix désirés.

### 1. **Exemple : Affichage du catalogue des contenants** 

À l'invitation :

```
Que voulez vous faire ?
1 - Consulter le catalogue
2 - Passer une commande
3 - Consulter les alertes de péremption
4 - Clôturer une commande
5 - Quitter l'application
```
On pourra répondre `1` dans le terminal pour consulter les produits et contenants disponibles à la vente. On pourra ensuite faire afficher ce menu :

```
Que souhaitez-vous voir ?
1 - Consulter les produits
2 - Consulter les contenants
```
Pour écrire `2` pour afficher les contenants disponibles à la vente :
```
Chargement du catalogue...
Sac kraft SACK1K à usage unique (1000.0 grammes)
└── 300 restant à 0.1€ l'unité

Bouteille plastique BOT1LPL à usage unique (1.0 litres)
└── 120 restant à 0.25€ l'unité

Pot en verre POT750V réutilisable (0.75 litres)
└── 50 restant à 1.2€ l'unité
```
Le reste du fonctionnement de l'application est analogue.
>>>>>>> master
