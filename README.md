# StockHolm

Application Java desktop JavaFX avec base SQLite locale pour suivre des produits et leurs pieces detachees. L'interface adopte un style sombre moderne inspire des outils de gestion et de collaboration.

## Fonctionnalites

- Ajout, modification et suppression de produits
- Suivi des pieces detachees liees a chaque produit
- Stock de batteries avec tension, capacite, format, notes et etat de chaque cellule
- Tableau de bord avec menu de graphiques : statuts, couts, cellules saines, gains purs,
  gains nets avec couts, objets rentables, ROI et KPI globaux
- Roulette flottante avec bille animee, tirage aleatoire et bouton d'ouverture/fermeture
- Interface JavaFX moderne avec navigation laterale, cartes KPI et graphiques integres
- Base SQLite creee automatiquement a cote de l'executable

## Lancement

```bash
gradle test
gradle run
gradle build
gradle packageWindows
gradle packageWindowsZip
gradle packageWindowsSingleExe
```

La base est enregistree dans le meme dossier que l'application lancee, sous le nom `stockholm.db`.

Lors d'un premier lancement avec `gradle run`, un jeu de données de débogage est
créé automatiquement : produits dans plusieurs statuts, pièces détachées,
batteries et cellules avec des états variés. Le jeu de données n'est créé que
si la base est vide et ne remplace jamais les données existantes. Pour repartir
de cet état, fermez l'application puis supprimez la base locale
`%LOCALAPPDATA%/StockHolm/stockholm.db` avant de relancer `gradle run`.

`gradle packageWindows` genere une application Windows autonome dans
`build/jpackage/StockHolm/`. Le fichier executable est
`build/jpackage/StockHolm/StockHolm.exe`, avec son runtime Java inclus.

Il faut conserver tout le dossier `StockHolm`, et pas seulement le fichier
`.exe` : les dossiers `app` et `runtime` sont necessaires. Pour transferer
l'application vers un autre PC, la commande recommandee est :

```bash
gradle packageWindowsZip
```

Elle genere `build/distributions/StockHolm-1.0.0-windows.zip`. Il suffit de
decompresser cette archive, puis de lancer `StockHolm/StockHolm.exe`.

Pour obtenir un seul fichier portable auto-extractible, executer :

```bash
gradle packageWindowsSingleExe
```

Le fichier genere est `build/distributions/StockHolm-1.0.0-portable.exe`.
Il embarque l'application et Java, extrait temporairement les fichiers puis
lance StockHolm automatiquement. La base utilisateur est conservee dans
`%LOCALAPPDATA%/StockHolm/stockholm.db`.

## Champs produit

- Nom
- Plateforme d'achat
- Prix d'achat
- Frais de port
- Date
- Diagnostic initial
- Temps passe
- Prix total des pieces achetees
- Statut
- Canal de revente
- Prix de vente
- Frais de vente
