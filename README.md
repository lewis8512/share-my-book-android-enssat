# ShareMyBook

ShareMyBook est une application Android de partage de livres.

## Fonctionnalités principales

- Scanner les ISBN de ses livres pour les ajouter automatiquement à l’application, ou saisir les informations manuellement
- Prêter et emprunter des livres et garder une trace des échanges

## Installation

### Prérequis
- Android Studio
- SDK Android 24 minimum (Android 7.0)
- JDK 17

### Compilation

Le projet se compile directement depuis Android Studio, mais peut aussi être compilé directement en ligne de commande :

```bash
# Windows
.\gradlew.bat assembleDebug

# Linux/macOS
./gradlew assembleDebug
```

### Téléchargement
 
 L'APK de l'application peut être téléchargé [ici](./ShareMyBook.apk)

## Architecture de l'application

### Organisation du code

L'application suit une architecture en 3 couches (interface utilisateur, domaine, données) et s'inspire des [recommandations officielles Android](https://developer.android.com/topic/architecture/recommendations?hl=fr#layered-architecture) :

```
app/src/main/java/fr/enssat/sharemybook/lewisgillian/
├── data/           # Sources de données et repositories
│   ├── local/      # Base de donnée Room, DAOs
│   ├── remote/     # Retrofit services, API
│   └── repository/ # Repository
├── domain/         # Modèles métier
│   └── model/      # Entités métiers
└── ui/             # Interface utilisateur
    ├── screens/    # Écrans Compose
    ├── components/ # Composants
    └── viewmodel/  # ViewModels
```

Les mappers (`data/mapper/`) permettent de convertir les données entre les différentes couches.

### Architecture UI : MVVM

L'architecture **MVVM (Model-View-ViewModel)** est utilisée pour l’ensemble des écrans.

- **View (Compose)** : affiche l’état et relaie les actions utilisateur
- **ViewModel** : contient la logique de présentation et l’état
- **Repository** : gère l’accès aux données (Room pour le local, et Retrofit pour le remote)

Les états sont exposés via StateFlow, ce qui permet une mise à jour automatique de l’interface.

Nous utilisons ```AndroidViewModel``` pour accéder aux ressources de l’application (comme strings.xml), malgré la [recommandation d'Android de n’utiliser que ViewModel](https://developer.android.com/topic/architecture/recommendations?hl=fr#viewmodel).

### Injection de dépendances via le pattern Factory

L'injection est gérée via le [pattern Factory](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories?hl=fr). Chaque ViewModel a une Factory qui injecte les repositories.

### Abstraction des données via le pattern Repository 

Les Repositories abstraient les sources de données comme [recommandé par Android](https://developer.android.com/topic/architecture?hl=fr#data-layer) :
- `BookRepository` : livres (CRUD local + API OpenLibrary)
- `UserRepository` : profil et contacts
- `TransactionRepository` : communication avec le backend

## Technologies utilisées

| Composant | Technologie |
|-----------|-------------|
| Langage de programmation | Kotlin |
| Interface utilisateur | Jetpack Compose, Material 3 |
| Concurrence et gestion d’état | Coroutines, StateFlow |
| Base de données | Room |
| Communication API | Retrofit, OkHttp |

Les [Coroutines](https://developer.android.com/kotlin/coroutines?hl=fr) permettent de réaliser des tâches non bloquantes, par exemple elles permettent de rechercher un livre sur l'API ou d'enregistrer des données sans bloquer l'application (l'utilisateur peut continuer à naviguer pendant que l'opération se fait en arrière-plan).

Les [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow?hl=fr) permettent de mettre à jour automatiquement l'écran quand les données changent (par exemple, afficher que l'application est en recherche, puis afficher le livre trouvé).

## Améliorations possibles

- Utiliser Hilt pour simplifier l'injection de dépendances
- Ajouter des tests (dont tests unitaires)
- Implémenter la pagination pour les grandes bibliothèques

## Auteurs

IAI3, promotion 2026 :
- Lewis SUIRE
- Gilian LE PEVEDIC