# Système de Bibliothèque Multi-Agents

Ce projet implémente un système de gestion de bibliothèque basé sur une architecture multi-agents utilisant le framework JADE (Java Agent DEvelopment Framework).

## Architecture du système

### Vue d'ensemble
Le système simule une bibliothèque avec différents types d'utilisateurs qui interagissent avec une bibliothécaire pour rechercher et emprunter des livres. Un agent observateur supervise toutes les interactions et les affiche dans une interface graphique.

### Diagramme d'architecture
```
+----------------------------------+
|         MainContainer            |
|   +-------------------------+    |
|   |    Agent Observateur    |    |
|   +-------------------------+    |
+----------------------------------+
               |
       +-------+--------+
       |                |
+-------------+  +---------------+
| Bibliothèque |  | Utilisateurs |
|  Container   |  |   Container  |
+-------------+  +---------------+
       |                |
+-------------+  +---------------+
|   Agent     |  |   Agents      |
|Bibliothécaire|  | Utilisateurs |
+-------------+  +---------------+
```

### Types d'agents et architectures

1. **Bibliothécaire** (Agent réactif avec état)
    - Architecture: Réactive avec mémoire
    - Objectif: Gérer les livres de la bibliothèque et répondre aux demandes des utilisateurs
    - Comportements: Recherche de livres, gestion des emprunts

2. **Utilisateur simple** (Agent réactif simple)
    - Architecture: Réactive simple
    - Objectif: Vérifier si un livre est disponible dans la bibliothèque
    - Comportement: Demande d'information et réponse simple

3. **Utilisateur emprunteur** (Agent à but)
    - Architecture: BDI simplifiée (Beliefs-Desires-Intentions)
    - Objectif: Emprunter un livre spécifique pour une période donnée
    - Comportement: Demande d'emprunt, négociation de durée, confirmation

4. **Utilisateur avec liste** (Agent à utilité)
    - Architecture: Agent à utilité
    - Objectif: Obtenir le maximum de livres d'une liste en fonction de l'utilité perçue
    - Comportement: Évaluation de l'utilité des livres disponibles, décision d'emprunt

5. **Observateur** (Agent réactif complexe)
    - Architecture: Réactive avec état
    - Objectif: Observer et enregistrer les interactions entre la bibliothécaire et les utilisateurs
    - Comportement: Collection et affichage des informations

## Protocoles de communication

### Protocoles utilisés
1. **Protocole de recherche simple**
    - Utilisateur → Bibliothécaire: REQUEST (demande de disponibilité)
    - Bibliothécaire → Utilisateur: INFORM (recherche en cours)
    - Bibliothécaire → Utilisateur: INFORM/FAILURE (résultat)
    - Utilisateur → Bibliothécaire: INFORM (remerciement)

2. **Protocole d'emprunt**
    - Utilisateur → Bibliothécaire: CFP (demande d'emprunt)
    - Bibliothécaire → Utilisateur: INFORM (recherche en cours)
    - Bibliothécaire → Utilisateur: PROPOSE/REFUSE (disponibilité)
    - Utilisateur → Bibliothécaire: ACCEPT_PROPOSAL (acceptation)
    - Bibliothécaire → Utilisateur: INFORM/FAILURE (confirmation)
    - Utilisateur → Bibliothécaire: INFORM (remerciement)

3. **Protocole de recherche de liste**
    - Utilisateur → Bibliothécaire: CONFIRM (envoi de liste)
    - Bibliothécaire → Utilisateur: INFORM (recherche en cours)
    - Bibliothécaire → Utilisateur: INFORM (résultats)
    - Utilisateur → Bibliothécaire: INFORM (réflexion)
    - Utilisateur → Bibliothécaire: AGREE/INFORM (décision)
    - Bibliothécaire → Utilisateur: INFORM (confirmation)
    - Utilisateur → Bibliothécaire: INFORM (remerciement)

### Diagramme d'interaction (séquence)
```
+-----------+      +---------------+       +------------+
| User      |      | Bibliothécaire|       | Observateur|
+-----------+      +---------------+       +------------+
     |                     |                      |
     |  Requête / Demande  |                      |
     |-------------------->|                      |
     |                     |     Notification     |
     |                     |--------------------->|
     |   Recherche en      |                      |
     |     cours           |                      |
     |<--------------------|                      |
     |                     |     Notification     |
     |                     |--------------------->|
     |                     |                      |
     |  Résultat recherche |                      |
     |<--------------------|                      |
     |                     |     Notification     |
     |                     |--------------------->|
     |                     |                      |
     |    Décision         |                      |
     |-------------------->|                      |
     |                     |     Notification     |
     |                     |--------------------->|
     |                     |                      |
     |    Confirmation     |                      |
     |<--------------------|                      |
     |                     |     Notification     |
     |                     |--------------------->|
     |                     |                      |
     |    Remerciement     |                      |
     |-------------------->|                      |
     |                     |     Notification     |
     |                     |--------------------->|
     |                     |                      |
```

## Modèle de données

* **Livre** - Représente un livre dans la bibliothèque avec titre, auteur, quantité disponible et durée d'emprunt maximale.
* **ListeLivres** - Collection de titres de livres demandés par un utilisateur.
* **ResultatRecherche** - Résultat d'une recherche contenant les livres trouvés et non trouvés.

## Interface graphique

L'interface graphique est composée de trois sections principales :
1. **Section Bibliothécaire** - Affiche les actions et messages de la bibliothécaire
2. **Section Utilisateurs** - Affiche les actions et messages des différents utilisateurs
3. **Journal des Transactions** - Enregistre toutes les interactions et transactions

## Exécution du système

### Prérequis
- Java 8 ou supérieur
- Framework JADE (inclus dans les dépendances)

### Lancement
1. Exécutez la classe `Main` pour démarrer l'ensemble du système
2. Le système lancera séquentiellement :
    - Le conteneur principal avec l'observateur
    - Le conteneur de la bibliothèque avec l'agent bibliothécaire
    - Le conteneur des utilisateurs avec les différents types d'utilisateurs

### Fonctionnement
Le système démarre une simulation où différents types d'utilisateurs interagissent tour à tour avec la bibliothécaire :
1. Un utilisateur simple recherche un livre uniquement pour vérifier sa disponibilité
2. Un utilisateur emprunteur souhaite emprunter un livre spécifique
3. Un utilisateur avec liste recherche plusieurs livres avec un seuil d'utilité satisfaisant
4. Un utilisateur avec liste recherche plusieurs livres avec un seuil d'utilité élevé
5. Un utilisateur simple recherche un livre non disponible

## Lancement depuis l'interface GUI de JADE

Pour démarrer le système depuis l'interface graphique de JADE :

1. Lancez le Main Container JADE :
```
java -cp jade.jar jade.Boot -gui
```

2. Cliquez sur "Start New Agent" dans l'interface RMA
3. Créez les agents dans l'ordre suivant :
    - Agent "observateur" de la classe "gui.ObservateurAgent"
    - Agent "bibliothecaire" de la classe "agents.Bibliothecaire"
    - Agents utilisateurs de l'une des classes suivantes :
        - "agents.UserSimple" avec l'argument du titre du livre
        - "agents.UserEmprunteur" avec les arguments du titre et du nombre d'exemplaires
        - "agents.UserListe" avec l'argument de la liste des titres

## Extensions possibles

- Ajout d'un système de réservation pour les livres non disponibles
- Implémentation d'un système de recommandation basé sur les préférences des utilisateurs
- Gestion des retours et des pénalités pour les retards
- Interface web pour interagir avec le système

## Conclusion

Ce système démontre l'utilisation de différentes architectures d'agents pour modéliser un environnement de bibliothèque. Les interactions entre agents sont basées sur des protocoles standard de FIPA, et le système affiche toutes les communications dans une interface graphique ergonomique pour suivre le déroulement de la simulation.
