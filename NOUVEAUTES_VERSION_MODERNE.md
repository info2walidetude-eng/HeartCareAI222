# HeartCare AI — Version moderne 2.0

Cette archive est une évolution du projet précédent : **même applicationId et même base SQLite** pour conserver l'accès aux données de démonstration lors d'une mise à jour, à condition de ne pas désinstaller l'application et de conserver la même signature de build.

## Nouveautés

1. **Identité visuelle** : typographie modernisée, couleurs corail / menthe / anthracite, écrans homogènes, pictogrammes Material Rounded, cartes à coins arrondis.
2. **Accueil enrichi** : tableau de bord, nombre de bilans, fréquence cardiaque BLE, état du modèle ML, accès rapides et derniers bilans.
3. **Évaluation guidée en 3 étapes** : profil, mesures au repos, effort/ECG ; progression, contrôles de saisie, bouton précédent.
4. **Historique amélioré** : recherche par date, âge, type de douleur ou source, filtres « tous/avec score/sans score », tri chronologique, modification et suppression confirmée.
5. **Résultat** : classe et score du modèle avec barre de visualisation et avertissements de non-validité clinique.
6. **Capteur BLE** : grand affichage du pouls, état de connexion, boutons de recherche et de déconnexion, rappel des compatibilités.
7. **Guide santé pédagogique** : explication des indicateurs et du rôle du Machine Learning, sans recommandation personnalisée.
8. **Profil et page À propos** : design renouvelé, noms des réalisateurs, encadrement et deux logos de l'université/faculté conservés.

## Conditions de compilation

- Ouvrez **uniquement le dossier `HeartCareAI`** (celui qui contient `settings.gradle.kts`) dans Android Studio.
- Gradle 8.13, Android Gradle Plugin 8.11.1, Kotlin 2.2.21 et SDK Android 36 sont configurés.
- Le **Gradle Wrapper de bootstrap** et ses fichiers sont inclus, comme dans la version précédente.
- Une connexion Internet est nécessaire à la première synchronisation, notamment pour l'ajout de la dépendance `androidx.compose.material:material-icons-extended`.
- Démarrez un émulateur, sélectionnez `app`, puis `Run ▶`.
- La compilation Android et l'exécution sur téléphone ne sont **pas vérifiées dans notre environnement**, dépourvu du SDK Android et sans accès réseau aux dépôts Gradle. En cas de message d'erreur, joindre le *Build Output*.

## Machine Learning

`heart_model.json` n'est pas pré-entraîné dans cette archive. En son absence, le projet permet inscription, collecte, sauvegarde, recherche, modification et suppression de bilans, sans fabriquer de score.

À partir de `heart.csv` du dataset Kaggle :

```powershell
py -m pip install -r ml/requirements.txt
py ml/train_export.py --csv ml/heart.csv
```

Recompilez ensuite l'application dans Android Studio pour embarquer le modèle généré dans les assets.

**Limites médicales** : le dataset prédit la variable `HeartDisease` (0/1) et non spécifiquement l'insuffisance cardiaque. L'application est pédagogique, sans certification ou validation clinique. La base locale SQLite n'est pas chiffrée : utilisez exclusivement des données fictives.
