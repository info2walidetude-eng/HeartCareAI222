# Contrôles effectués

## Effectués dans l'environnement de préparation
- Compilabilité du mini-lanceur Java : `javac --release 11` et création du JAR.
- Existence des scripts `gradlew` et `gradlew.bat` et de la configuration Gradle.
- Vérification structurée des XML Android et de la cohérence des chemins de ressources.
- Tests du script Python sur données synthétiques (`unittest`).
- Intégrité ZIP.

## Non effectués
- Compilation Gradle réelle `assembleDebug` (pas de SDK Android ni accès aux dépôts dans cet environnement).
- Exécution sur émulateur ou téléphone Android.
- Tests BLE avec un appareil réel.
- Entraînement réel sur les 918 lignes de Kaggle (fichier heart.csv non fourni).

Le statut « vérifié » ne signifie donc pas APK compilé ou lancement confirmé.
