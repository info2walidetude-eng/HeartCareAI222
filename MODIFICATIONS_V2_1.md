# HeartCare AI V2.1 — Connexion et profil enregistré

Projet modifié à partir de l'archive **HeartCareAI_AndroidStudio_V2_Moderne.zip** envoyée dans la conversation.

## Fonctionnalités demandées

1. **Connexion modernisée** : nouveau bandeau HeartCare AI avec ligne ECG, formulaire plus lisible, option afficher/masquer le mot de passe ; suppression des mentions « Master 2 ISI », « Université d'Alger 1 » et « 2026–2027 » des écrans publics (connexion et À propos).
2. **Inscription enrichie** : nom, e-mail, mot de passe, **âge (1–120 ans), sexe (M/F), douleur thoracique (TA / ATA / NAP / ASY)**. Les trois nouveaux champs sont obligatoires.
3. **Profil sauvegardé** : les informations sont enregistrées dans la table SQLite `users` et affichées sur l'écran **Mon profil**.
4. **Évaluation simplifiée** : l'âge, le sexe et la douleur thoracique sont automatiquement repris du compte et affichés en lecture seule. L'utilisateur ne doit plus les saisir à chaque bilan.
5. **Migration des comptes déjà créés** : la base passe de la version SQLite 1 à 2 sans effacer les comptes ou les évaluations. Un ancien compte doit compléter son profil dans **Mon profil** avant de lancer une nouvelle analyse.
6. **Contrôles de cohérence** : la sauvegarde des bilans refuse des valeurs d'âge/sexe/douleur différentes du profil associé au compte.

## Fichiers modifiés

- `app/src/main/java/dz/univ/alger1/heartcareai/ui/Screens.kt`
- `app/src/main/java/dz/univ/alger1/heartcareai/MainActivity.kt`
- `app/src/main/java/dz/univ/alger1/heartcareai/data/LocalStore.kt`
- `app/src/main/java/dz/univ/alger1/heartcareai/data/Models.kt`
- `app/src/main/java/dz/univ/alger1/heartcareai/data/MedicalProfile.kt` (nouveau)
- `app/src/main/java/dz/univ/alger1/heartcareai/ui/ModernPreviews.kt`
- `app/src/test/java/dz/univ/alger1/heartcareai/data/MedicalProfileTest.kt` (nouveau)
- `app/build.gradle.kts` (version 2.1 / code 3)

## Important : limites de l'application

Le projet V2 conserve un stockage **SQLite non chiffré**, sur l'appareil uniquement : il **n'est pas adapté à des données médicales réelles ou à une diffusion publique** sans audit de sécurité et corrections complémentaires. Ce projet reste un **prototype éducatif** ; la classification IA n'est pas un diagnostic médical. L'âge et les symptômes peuvent changer : la valeur fixée lors de l'inscription ne reflète pas nécessairement l'état de santé à une date ultérieure.

## Vérification

Les tests effectués sur les champs du profil et la migration SQLite sont détaillés dans `VERIFICATION_V2_1.md`. La compilation complète et l'exécution Android restent à tester dans Android Studio (SDK Android indisponible dans l'environnement de génération).
