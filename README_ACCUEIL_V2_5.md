# HeartCare AI V2.5 — Accueil enrichi

## Nouveautés dans Accueil

- **Bilan d'aujourd'hui** : état calculé depuis l'horodatage des évaluations de l'utilisateur, selon le fuseau horaire du téléphone. Affiche « Pas encore effectué » si aucun bilan du jour n'existe, puis « Effectué » après enregistrement. Bouton « Faire mon bilan » ou « Voir mes bilans ».
- **Courbe d'évolution** : choix entre tension systolique au repos (`RestingBP`, mmHg) et fréquence cardiaque maximale à l'effort (`MaxHR`, bpm). Affiche uniquement les **7 derniers bilans réellement enregistrés**. Aucune donnée fictive n'est ajoutée.
- **Conseils pédagogiques** : des messages généraux et contextualisés par le dernier bilan enregistré (`RestingBP`, `FastingBS`, `Cholesterol`, `ExerciseAngina`). Les messages invitent à vérifier les mesures et à consulter si besoin, sans promettre d'éviter une maladie ou établir un diagnostic.
- **État vide** : si aucun bilan n'est disponible, affichage d'une invitation à remplir le premier bilan et de conseils généraux.

## Fichiers principaux modifiés

- `app/src/main/java/dz/univ/alger1/heartcareai/ui/Screens.kt` — accueil remanié
- `app/src/main/java/dz/univ/alger1/heartcareai/ui/HomeDashboardCharts.kt` — nouveau graphique et cartes quotidiennes / conseils
- `app/src/main/java/dz/univ/alger1/heartcareai/data/HomeInsights.kt` — logique Kotlin pure testable sans Android
- `app/src/test/java/dz/univ/alger1/heartcareai/data/HomeInsightsTest.kt` — tests locaux
- `app/src/test/java/dz/univ/alger1/heartcareai/ble/BleDeviceCatalogTest.kt` — adaptation des tests BLE aux images offline V2.4

## Ouverture dans Android Studio

1. Décompresser le ZIP, ouvrir `HeartCareAI` (dossier avec `settings.gradle.kts`).
2. Installer Android SDK 36, configurer le Gradle JDK 17.
3. Lancer **Sync Project with Gradle Files**.
4. Sélectionner un émulateur ou téléphone Android et **Run ▶**.
5. Inscrire un compte, enregistrer des bilans, retourner à Accueil : la courbe et les conseils utilisent les données réellement enregistrées.

Si Gradle signale une erreur, recopier le premier message rouge de **Build Output**.

## Limites importantes

- `RestingBP` est la pression artérielle **systolique uniquement** : il ne s'agit pas d'une valeur systolique/diastolique complète.
- `MaxHR` est la fréquence cardiaque **maximale atteinte à l'effort** ; **ne pas confondre** avec le pouls BLE en direct.
- Un seul bilan ne permet pas de conclure à une amélioration ou une dégradation de l'état de santé.
- Les conseils ne sont ni un diagnostic médical, ni une prescription, ni une garantie de prévention des maladies chroniques.
- Les bilans de cette version restent stockés localement **sans chiffrement médical complet**. Ne pas enregistrer de données réelles de patients ou diffuser comme application clinique.
- Le modèle IA est facultatif : il requiert toujours la préparation des actifs depuis `ml/heart.csv` (non inclus).
- Le projet a été vérifié hors Android Studio ; **la compilation Android complète et l'exécution sur appareil ne sont pas certifiées** ici.

## Références éducatives

- American Heart Association — Life's Essential 8 : https://www.heart.org/en/healthy-living/healthy-lifestyle/lifes-essential-8
- American Heart Association — Mesure de tension à domicile : https://www.heart.org/en/health-topics/high-blood-pressure/understanding-blood-pressure-readings/monitoring-your-blood-pressure-at-home
