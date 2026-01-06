Voici un plan d’exécution détaillé, orienté “ingénierie produit + implémentation”, pour remettre votre projet Android en conformité avec les règles réelles du Loto Foot FDJ (gamme “Loto Sports” – point de vente), et pour sécuriser les calculs/algorithmes par des tests et des garde-fous.

Je base ce plan sur le règlement FDJ “Loto Sports” (version applicable à compter du 2 août 2024), qui définit notamment : les 4 formules Loto Foot 7/8/12/15, les cas de neutralisation quand la liste contient moins de matchs, les règles d’annulation, et surtout les tableaux de mises (donc les combinaisons doubles/triples autorisées).

1. Cadrage fonctionnel (ce que l’application doit reproduire “à l’identique FDJ”)

1.1. “Quelle variante de Loto Foot ?”
Vous devez “figer” votre référence règlementaire. Ici : Loto Foot proposé en points de vente FDJ (gamme “Loto Sports”), avec 4 formules : Loto Foot 7, Loto Foot 8, Loto Foot 12, Loto Foot 15.

1.2. Règles qui impactent directement vos algorithmes (donc à implémenter)
A. Format et nombre de matchs “nominal”
Le règlement définit bien 7/8/12/15 comme formats (nombre de lignes “grille”).

B. Cas “liste courte” : lignes neutralisées (à gérer dans vos calculs)
Exemples explicites du règlement :

* Loto Foot 7 peut exceptionnellement avoir 6 rencontres : la 7e ligne est neutralisée et ne participe pas.
* Loto Foot 8 peut exceptionnellement avoir 7 rencontres : la 8e ligne est neutralisée.
* Loto Foot 12 peut exceptionnellement avoir 11/10/9 rencontres : les dernières lignes (12, ou 12-11, ou 12-11-10) sont neutralisées.
* Loto Foot 15 peut exceptionnellement avoir 14/13/12 rencontres : les dernières lignes sont neutralisées.

C. Jeu multiple : doubles/triples autorisés (sinon “rejeté terminal”)
Le règlement dit explicitement que les bulletins dont le nombre de doubles/triples n’est pas autorisé sont rejetés, et renvoie à des tableaux.
Puis il donne les tableaux de mises (Article 7) qui listent exactement les couples (doubles, triples) possibles, via les montants 1€, 2€, 3€, …

D. Match annulé : “réputé gagnant”, et “si annulé avant prise de jeu, pas besoin de cocher / ignoré”
Règles importantes :

* En cas d’annulation dans certaines conditions, le match est “annulé et réputé gagnant”.
* Si la rencontre a été annulée avant la prise de jeu, ce n’est pas nécessaire de cocher et le terminal ignore le pronostic même si coché.

E. Résultat retenu : fin du temps réglementaire (pas prolongations/tirs au but)
Le règlement rappelle que le résultat pris en compte est celui de la fin du temps réglementaire et précise que le “temps réglementaire” n’inclut pas les prolongations / tirs au but.

1.3. Critères d’acceptation (à écrire noir sur blanc pour la recette)
Vous devez pouvoir dire “OK / KO” sur :

* L’utilisateur ne peut créer que des grilles FDJ (7/8/12/15), pas “n’importe quel nombre de matchs”.
* La mise/budget calculée correspond au nombre de combinaisons “jeux simples inclus” (2^d × 3^t) et uniquement si le couple (d,t) est autorisé par le tableau FDJ correspondant.
* Si la liste est “courte”, les lignes neutralisées n’entrent ni dans la mise, ni dans les stats (couverture, distribution).
* Si un match est marqué “annulé (avant prise de jeu)”, la ligne est ignorée et réputée gagnante.

2. Refactoring “règles FDJ” : isoler la logique dans le module core

Objectif : ne pas disperser les règles dans l’UI / ViewModel. Créer une source unique de vérité (un “rules engine” minimal).

2.1. Créer un fichier de règles dédié
Créer : `app/src/main/java/com/example/yoyo_loto/core/FdjLotoFootRules.kt`

Contenu recommandé :

* Enum de format

    * `LF7`, `LF8`, `LF12`, `LF15`
    * `nominalMatches` (7/8/12/15)
    * `allowedRealMatches` (pour gérer la neutralisation : ex LF12 -> 12,11,10,9)
    * Table de validation doubles/triples via les tableaux de mises de l’Article 7

* Validation des couples (doubles, triples)
  Représentation simple (robuste et rapide) : un tableau “maxTriplesByDoubles”, directement déduit du tableau de mises.
  Valeurs (issues des tableaux Article 7)  :

    * LF7 (Loto Sports 7 pour Loto Foot 7)
      max triples par doubles = `[3, 2, 1, 1, 0]`
    * LF8
      `[3, 2, 2, 1, 0, 0]`
    * LF12
      `[5, 4, 4, 3, 2, 2, 1, 0, 0]`
    * LF15 (Loto Sports 15 pour Loto Foot 15)
      `[6, 5, 4, 4, 3, 2, 2, 1, 1, 0]`

  Une règle simple suffit : si `d` est dans l’index du tableau et `t <= maxTriplesByDoubles[d]`, alors la combinaison est autorisée.

2.2. Introduire un résultat de validation structuré
Créer une data class de diagnostic, ex :

* `GridFdjValidationResult`

    * `isOk: Boolean`
    * `errors: List<String>` (messages affichables)
    * `format: LotoFootFormat`
    * `nominalMatches: Int`
    * `realMatches: Int`
    * `neutralizedCount: Int`
    * `doubles: Int`
    * `triples: Int`
    * `stake: BigInteger` (mise en euros)
    * `stakeReason: String` (optionnel)

But : le ViewModel appelle `validateGrid()` et l’UI affiche simplement le résultat.

3. Modèle de données : faire apparaître “format FDJ + match réel + neutralisation”

Votre modèle actuel est “matchCountInput libre” + “GridUiState.matchCount”. Ce n’est pas FDJ.

3.1. Modifier `AppUiState` / `SavedAppState` pour stocker un format FDJ
Fichiers :

* `model/UiState.kt`
* `model/SavedState.kt`
* `data/AppStateRepository.kt`

Changements conseillés :

A. Remplacer `matchCountInput: String`
Par :

* `selectedFormat: LotoFootFormat` (ou `String` sérialisable)
* `selectedRealMatchCount: Int` (optionnel, mais très utile pour la neutralisation)

B. Dans `GridUiState`, remplacer / compléter :

* `matchCount` (aujourd’hui = nombre de lignes)
  Par :
* `format: LotoFootFormat`
* `nominalMatchCount: Int` (dérivé du format, pas saisi)
* `realMatchCount: Int` (modifie les lignes neutralisées)
* `matches: List<MatchUiState>` reste à taille nominale (7/8/12/15), mais certaines lignes seront neutralisées.

Pourquoi : la neutralisation FDJ “désactive des lignes de fin”, tout en gardant une grille nominale.

3.2. Ajouter un statut par match (au minimum “actif vs neutralisé vs annulé”)
Modifier `MatchUiState` pour porter :

* `status: MatchStatus`

Créer `enum class MatchStatus { ACTIVE, NEUTRALIZED, CANCELLED_BEFORE_BET }`

Justification :

* Neutralisé : “ne participe pas au jeu” (liste courte).
* Annulé avant prise de jeu : “pas nécessaire de cocher / ignoré par terminal” + réputé gagnant.

3.3. Migration DataStore (compatibilité ascendante)
Dans `AppStateRepository.parseState` :

* Si vous ne trouvez pas `format`, déduisez-le à partir de `matchCount` existant quand possible.
* Sinon retombez sur LF7 par défaut.

C’est indispensable pour ne pas “casser” les installations existantes.

4. UI : remplacer “nombre de matchs” par un sélecteur de formule FDJ

4.1. Remplacer la création de grille
Fichier : `ui/screens/MainScreen.kt`

Remplacer “Creation de grille / champ Matchs” par :

* Un dropdown “Formule” : Loto Foot 7 / 8 / 12 / 15
* Un dropdown “Rencontres réelles” (optionnel mais recommandé) :

    * Pour LF7 : 7 ou 6
    * Pour LF8 : 8 ou 7
    * Pour LF12 : 12 / 11 / 10 / 9
    * Pour LF15 : 15 / 14 / 13 / 12

En Jetpack Compose Material3, vous pouvez utiliser `ExposedDropdownMenuBox` (composant standard pour un combo box).

4.2. Dans chaque ligne match : afficher le statut et empêcher les saisies incohérentes
Dans `SelectionRow` et `OddsRow` :

* Si `status == NEUTRALIZED` : afficher “Neutralisée” et désactiver les boutons 1/N/2 + champs cotes.
* Si `status == CANCELLED_BEFORE_BET` : idem (désactivé), mais afficher “Annulée (gagnant pour tous)” (le libellé doit refléter la règle).

4.3. Ajouter un panneau “Validation FDJ” dans la carte grille
Dans `GridCard` (MainScreen) :

* Avant “Statistiques”, afficher un bloc :

    * “Doubles: X, Triples: Y, Mise: Z €”
    * “Statut: Valide FDJ / Non valide FDJ + raisons”
* Désactiver les boutons “Calculer” et “Auto-grille” si Non valide FDJ (ou les laisser actifs mais afficher un snackbar “Le terminal FDJ rejetterait cette grille”). Le règlement indique bien le rejet par terminal en cas de double/triple non autorisé.

5. ViewModel : appliquer strictement les contraintes FDJ au moment du calcul

Fichier : `viewmodel/AppViewModel.kt`

5.1. Changer `addGrid()`
Aujourd’hui : parse un nombre libre, clamp à MAX_MATCHES.
Demain :

* Lire `selectedFormat` de l’état UI
* Construire la grille avec `nominalMatchCount = format.nominalMatches`
* Appliquer immédiatement la neutralisation selon `realMatchCount` :

    * ex LF12, real=10 -> lignes 11 et 12 en `NEUTRALIZED`
* Initialiser les `MatchUiState` neutralisés avec sélections vides et champs odds désactivés (ou valeurs par défaut, mais ignorées).

5.2. Ajouter `setRealMatchCount(gridId, value)` (si vous exposez ce contrôle par grille)
Si vous permettez de modifier le “match réel” après création :

* Recalculer les statuts `NEUTRALIZED` pour les indices >= realMatchCount
* Réinitialiser proprement les selections de ces lignes (pour éviter qu’un triple “reste” en mémoire alors qu’il est neutralisé)

5.3. Centraliser la validation FDJ : une méthode unique utilisée partout
Ajouter une fonction privée :

* `private fun validateFdjGrid(grid: GridUiState): GridFdjValidationResult`

Elle doit faire :
A. Déterminer les lignes “participantes”

* Participantes = `status == ACTIVE` et éventuellement celles annulées après validation si vous décidez de gérer ce cas plus tard.
* Neutralisées = ignorées.

B. Vérifier “au moins 1 issue” sur chaque match actif
Le terminal rejetterait une ligne vide (et le règlement impose de remplir le bon nombre de lignes).

C. Compter doubles / triples

* double si 2 issues cochées
* triple si 3 issues cochées

D. Vérifier la légalité du couple (d,t) avec la table du format
Table Article 7 = référence.

E. Calculer la mise (stake) = produit des choix (2^d × 3^t)
C’est exactement ce que vos calculs “tickets” font déjà quand chaque match a 1/2/3 issues, sauf que maintenant vous refusez les couples interdits.

5.4. Brancher la validation à `calculateGrid()` et `openAutoGrille()`
Avant de lancer les calculs :

* Si `!validation.isOk` : snackbar + stop.
* Sinon : calculer les stats.

6. Calculs statistiques : adapter les formules à la neutralisation / annulation

Actuellement, vos calculs supposent que tous les matchs participent (matchCount = nombre de lignes) et que le total des scénarios est 3^M.

Or, en “liste courte”, certaines lignes ne participent pas au jeu.

6.1. Définir `mEffective`

* `mEffective = nombre de lignes participantes (status ACTIVE)`
* Total scénarios = 3^mEffective
* Couverture = tickets / 3^mEffective
* Distribution = calculée sur mEffective matchs, pas sur le nominal

6.2. Gestion “annulé avant prise de jeu”
Si vous implémentez `CANCELLED_BEFORE_BET` (annulé avant validation) :

* Ne pas exiger de sélection
* Ne pas compter la ligne dans la mise (stake) car le terminal ignore cette ligne (règle 6.2)
* Pour les probabilités : c’est réputé gagnant -> probabilité de “couvrir” cette ligne = 1
* Pour la distribution du nombre de bons : cette ligne contribue comme succès certain (p=1) si vous la gardez comme “match compté”, ou vous pouvez aussi la sortir de `mEffective` (approche plus simple). Ce point est un choix de modélisation ; le règlement explique surtout l’aspect “gagnant/ignoré” et les règles de gain, mais ne vous impose pas une manière unique de “stats utilisateur”.

6.3. Rappel : résultat au temps réglementaire
Votre UI “Aide” doit mentionner explicitement que le résultat retenu est celui du temps réglementaire (pas prolongations). C’est une règle d’interprétation que les utilisateurs confondent souvent.

7. Contraintes “bulletin FDJ” (recommandé si vous voulez coller au réel)

Votre app permet d’ajouter des grilles sans limite. En point de vente, un bulletin a 8 grilles maximum et il y a 2 grilles par formule (2 pour LF7, 2 pour LF8, 2 pour LF12, 2 pour LF15).

Plan d’implémentation :

* Ajouter constants :

    * `MAX_GRIDS_TOTAL = 8`
    * `MAX_GRIDS_PER_FORMAT = 2`
* Dans `addGrid()` :

    * Si `grids.size == 8` => message + refuse
    * Si “grids of same format” == 2 => refuse
* Option produit : prévoir un switch “Mode FDJ strict” vs “Mode libre” (si vous voulez garder l’usage “analyse” hors contraintes FDJ). Dans un premier temps, vous pouvez mettre “strict” par défaut.

8. Mise à jour Help / écrans d’information (documentation embarquée)

Fichier : `ui/screens/HelpScreen.kt`

À faire :

* Remplacer l’aide actuelle (générique) par une aide centrée FDJ :

    * Les 4 formats 7/8/12/15
    * Lignes neutralisées si liste courte (et ce que ça implique dans l’app)
    * Double/triple autorisés uniquement selon tableau des mises (sinon rejet)
    * Résultat au temps réglementaire
    * Annulation avant prise de jeu : pas besoin de cocher, ignoré
* Ajouter un disclaimer : application non officielle FDJ, ne calcule pas les gains (les gains FDJ dépendent d’un partage, rangs, etc.), l’objectif est l’aide à la préparation.

9. Tests unitaires : verrouiller les règles FDJ et éviter les régressions

Vous avez déjà JUnit en place (ExampleUnitTest). Il faut maintenant des tests “métier”.

9.1. Où et comment

* Ajouter des tests dans `app/src/test/java/com/example/yoyo_loto/` (tests locaux JVM). Android recommande cette approche pour les tests unitaires “purs” (sans framework Android).
* Kotlin + JUnit : bonnes pratiques de structure et exécution via Gradle.

9.2. Jeux de tests indispensables (minimum)
A. Validation couples doubles/triples par format

* LF7 :

    * (d=0,t=3) => OK, stake 27
    * (d=1,t=3) => KO (54 n’existe pas dans le tableau)
* LF8 :

    * (d=2,t=2) => OK, stake 36
    * (d=2,t=3) => KO
* LF12 :

    * (d=8,t=0) => OK, stake 256
    * (d=6,t=2) => KO (si votre table le dit : d=6 max t=1)
* LF15 :

    * (d=8,t=1) => OK, stake 768
    * (d=8,t=2) => KO

B. Neutralisation

* LF12 nominal 12, real 10 :

    * Les 2 dernières lignes neutralisées ne doivent pas entrer dans la mise ni dans 3^M

C. Annulation avant prise de jeu (si implémentée)

* Une ligne `CANCELLED_BEFORE_BET` :

    * Ne doit pas exiger de sélection
    * Ne doit pas augmenter la mise
    * Doit “passer” la validation même sans choix

9.3. Stratégie d’implémentation test-friendly

* Mettre la logique de validation dans `core/` sans dépendance Android.
* Tester `validateFdjGrid()` en alimentant une structure simple (ou en construisant un `GridUiState` minimal).

10. Recette manuelle (checklist avant livraison)

Sur un build debug :

* Créer 1 grille LF7 :

    * 7 matchs actifs, 0 sélection sur M1 => l’app doit refuser “Calculer” (erreur)
    * 3 triples, 0 double => OK, budget = 27 €
    * 4 doubles + 1 triple => KO (doit refuser)
* LF12 (real=9) :

    * Vérifier que les lignes 10-11-12 sont affichées neutralisées/désactivées
    * Vérifier que couverture est sur 3^9, pas 3^12
* Vérifier que l’aide indique bien “temps réglementaire”

11. Découpage “sprints” (pragmatique)

Sprint 1 (valeur immédiate, risque faible)

* Ajouter `FdjLotoFootRules.kt`
* Remplacer le champ “Matchs” par sélection 7/8/12/15
* Bloquer combinaisons doubles/triples interdites
* Ajuster budget = mise FDJ (tickets) uniquement si valide
* Tests unitaires de validation

Sprint 2 (réalisme “liste courte”)

* Ajouter realMatchCount + neutralisation automatique
* Adapter statistiques à mEffective
* UI : marquage neutralisé

Sprint 3 (cas avancés + conformité bulletin)

* Annulation “avant prise de jeu” (statut match)
* Limites bulletin (8 grilles, 2 par format)
* Refonte Help + disclaimer

Si vous suivez ce plan, vous obtenez une application qui :

* calcule correctement les mises (budget) comme le ferait le terminal FDJ,
* refuse les grilles qui seraient rejetées (doubles/triples non autorisés),
* modélise la neutralisation des lignes (cas liste courte) conformément au règlement,
* est testée et donc maintenable.

Si vous voulez, je peux aussi vous proposer une “matrice de validation” prête à copier-coller (format JSON ou Kotlin) qui encode les combinaisons autorisées pour chaque formule à partir des tableaux Article 7, afin d’éviter toute ambiguïté future quand le règlement évolue.
