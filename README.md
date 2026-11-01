# 📦 store-management-jdbc

[![Academic Project](https://img.shields.io/badge/Type-Academic%20Project-blue?style=for-the-badge)](https://github.com/AlexandreDBC)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

> *A database design and Java JDBC application developed as part of academic coursework to manage a store catalog, orders, and deliveries.*

Java application using JDBC to connect to a relational database, implementing a complete store management system (products, orders, deliveries, and catalog features).

## 🚀 Compilation and Execution

### 1. Prerequisites
* Java Development Kit (JDK) version 17 or higher.
* `ojdbc17.jar` (Oracle JDBC Driver).

### 2. Compilation
To compile all source files, run the following command at the root of the project:
```bash
make
```
*This command will create an `out/` directory containing all the compiled `.class` files.*

### 3. Execution
To start the application:
```bash
make start
```

## 💻 Usage & Demonstration

The application features an interactive terminal-based UI. You can navigate through the menus by entering the number corresponding to your desired action.

### Example: Browsing the Container Catalog

At the main menu prompt:

```text
Que voulez vous faire ?
1 - Consulter le catalogue
2 - Passer une commande
3 - Consulter les alertes de péremption
4 - Clôturer une commande
5 - Quitter l'application
```

Type `1` in the terminal to browse available products and containers. The following sub-menu will appear:

```text
Que souhaitez-vous voir ?
1 - Consulter les produits
2 - Consulter les contenants
```

Type `2` to display the containers currently available for sale. The application will fetch the data from the database and display:

```text
Chargement du catalogue...
Sac kraft SACK1K à usage unique (1000.0 grammes)
└── 300 restant à 0.1€ l'unité

Bouteille plastique BOT1LPL à usage unique (1.0 litres)
└── 120 restant à 0.25€ l'unité

Pot en verre POT750V réutilisable (0.75 litres)
└── 50 restant à 1.2€ l'unité
```

*The rest of the application (ordering, expiration alerts, etc.) operates using this same intuitive logic.*
