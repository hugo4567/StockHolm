# StockHolm

Application Java desktop Swing avec base SQLite locale pour suivre des produits et leurs pieces detachees. idéale pour gerer de l'achat / revente de produits sur vinted avec réparations.

## Fonctionnalites

- Ajout, modification et suppression de produits
- Suivi des pieces detachees liees a chaque produit
- Tableau de bord avec graphiques des statuts et des couts
- Base SQLite creee automatiquement a cote de l'executable

## Lancement

```bash
gradle test
gradle run
gradle build
gradle packageWindows
```

La base est enregistree dans le meme dossier que l'application lancee, sous le nom `stockholm.db`.

`gradle packageWindows` genere une application Windows autonome dans
`build/jpackage/StockHolm/`. Le fichier executable est
`build/jpackage/StockHolm/StockHolm.exe`, avec son runtime Java inclus.

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
