# Vérifications HeartCare AI — Accueil V2.5

Tests réalisés dans l'environnement disponible :

- ✅ Compilation Kotlin **sans Android** de `HomeInsights.kt`.
- ✅ 7 assertions de logique : début/fin de journée, ordre des points, zéro bilan et conseils adaptés.
- ✅ 7 assertions du catalogue BLE et du décodeur de fréquence cardiaque.
- ✅ Les nouveaux fichiers sont inclus dans le projet et les tests JUnit sont fournis.
- ✅ Version mise à jour (`versionCode=5`, `versionName=2.5`).
- ⏳ Compilation Jetpack Compose/Android complète **non disponible** (SDK Android absent).
- ⏳ Exécution de l'interface sur émulateur/téléphone **non vérifiée**.

La compilation Kotlin des fonctions métier ne garantit pas l'absence d'erreurs de compilation Android. Un contrôle Gradle et un lancement sur appareil restent nécessaires.
