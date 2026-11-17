# HeartCare AI v2.2 — Capteurs BLE modernisés

Nouveau dans cette version : interface « Mes capteurs » avec trois modèles réels (Polar H10, Polar OH1 et Wahoo TICKR), photos Creative Commons depuis Wikimedia, états Connecté/Non connecté, recherche de proximité et connexion manuelle au périphérique choisi. Voir [README_BLE_V2_2.md](README_BLE_V2_2.md) et [CREDITS_PHOTOS_BLE.md](CREDITS_PHOTOS_BLE.md).

**Attention :** les photos nécessitent une connexion Internet au premier affichage ; sans Internet, une icône de remplacement apparaît. Le Bluetooth physique nécessite un vrai téléphone. La compilation complète Android n'a pas été vérifiée ici.

---

# Important — Version V2.1 du 17 novembre 2026

Cette archive ajoute les champs **âge / sexe / type de douleur thoracique lors de la création du compte**, les enregistre dans **Mon profil**, et les reprend automatiquement dans le formulaire d'évaluation. L'écran de connexion a été modernisé et les mentions universitaires retirées de l'interface. Voir `MODIFICATIONS_V2_1.md` et `OUVRIR_DANS_ANDROID_STUDIO.txt`.

---

# HeartCare AI 2.0 — Interface moderne et projet Android Studio

**Application Android pour un projet universitaire Master 2 ISI (2026–2027).**

Technologies : **Kotlin, Jetpack Compose, SQLite local, Bluetooth Low Energy (BLE), Machine Learning hors ligne (optionnel après entraînement)**.

- **Réalisateurs** : Bouzid Youcef, Atek Yanis, Cherifi Walid Zakaria.
- **Enseignante** : Benelmir Rima.
- **Université** : Université d’Alger 1 – Benyoucef Benkheda, Faculté des Sciences.

## Version modernisée

La version 2.0 ajoute un accueil interactif, une saisie en trois étapes, un historique avec recherche/filtres, un écran de résultat repensé et un guide pédagogique. Voir **`NOUVEAUTES_VERSION_MODERNE.md`**.

## Démarrage dans Android Studio (Windows, macOS, Linux)

1. **Décompresser** le ZIP téléchargé.
2. Ouvrir **Android Studio → File → Open**, puis sélectionner le dossier **`HeartCareAI`** (celui qui contient `settings.gradle.kts`). Ne pas choisir le fichier ZIP ni le dossier `app`.
3. Laisser Android Studio terminer **Gradle Sync**. Installer **Android SDK Platform 36** si demandé via **Tools → SDK Manager** et utiliser le JDK intégré (version 17 ou plus récente). **Une connexion Internet est nécessaire au premier lancement** pour Gradle, les plugins et les dépendances.
4. Ouvrir **Tools → Device Manager → Create Device**, créer un téléphone virtuel (API 35 ou 36), puis sélectionner `app` et cliquer sur **▶ Run**.
5. Sur l'écran de connexion, choisir **S'inscrire** et créer un compte local avec mot de passe de 8 caractères minimum.

Le ZIP **inclut les fichiers `gradlew`, `gradlew.bat`, `gradle-wrapper.jar` et `gradle-wrapper.properties`**. Il ne faut donc pas installer Gradle ou générer un wrapper à la main. Un bootstrap Java léger, inclus, installe automatiquement la **distribution officielle de Gradle 8.13**, après vérification de son **SHA-256 officiel**. Ce bootstrap est **différent du JAR du Gradle Wrapper officiel**, celui-ci n'ayant pas pu être obtenu dans l'environnement de production de l'archive. En pratique Android Studio peut utiliser directement la distribution définie dans `gradle-wrapper.properties`. Si vous souhaitez à terme remplacer le bootstrap par le wrapper Gradle officiel, exécutez `gradle wrapper --gradle-version 8.13` depuis une installation Gradle standard.

### Commandes utiles

Dans un terminal PowerShell **dans le dossier `HeartCareAI`** :

```powershell
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
```

Sur macOS/Linux :

```bash
./gradlew --version
./gradlew :app:assembleDebug
```

Si la compilation réussit, l'APK se trouvera dans `app/build/outputs/apk/debug/app-debug.apk`.

La dépendance Material Icons Extended doit être téléchargée une seule fois lors du premier Gradle Sync.

**Versions verrouillées :** Android Gradle Plugin `8.11.1`, Gradle `8.13`, Kotlin `2.2.21`, SDK compilation `36`, SDK minimum `26` (Android 8.0). Ce couple Kotlin/AGP/Gradle est dans les plages de compatibilité publiées.

## Fonctionnalités incluses et utilisables dès le premier lancement

- **Inscription, connexion, déconnexion**, changement du nom et suppression du compte.
- **Formulaire guidé** des 11 variables du dataset Kaggle avec validation.
- **Historique et gestion CRUD** : créer, ouvrir, modifier, supprimer des bilans.
- **Statistiques d'accueil**, page de détails et page « À propos » avec les 2 logos de l'université et de la faculté.
- **Connexion facultative à un capteur BLE de fréquence cardiaque** (service standard `0x180D`, caractéristique `0x2A37`). Le capteur doit être testé sur un vrai téléphone.
- **Prédiction ML hors ligne si un modèle entraîné est installé**. Sans modèle, l'app sauvegarde les bilans et affiche clairement « Modèle ML non disponible » sans fabriquer de diagnostic.

## Ajouter la prédiction à partir du dataset Kaggle

**La compilation et le lancement Android ne nécessitent PAS Python ni heart.csv.** Ces outils ne servent que pour activer la classification ML.

1. Télécharger **`heart.csv`** depuis le [Heart Failure Prediction Dataset](https://www.kaggle.com/datasets/fedesoriano/heart-failure-prediction).
2. Placer le CSV dans `HeartCareAI/ml/heart.csv`.
3. Installer Python 3.10+ puis exécuter, à partir du dossier racine du projet :

   ```powershell
   py -m pip install -r ml/requirements.txt
   py ml/train_export.py --csv ml/heart.csv
   ```

4. Le script génère **`app/src/main/assets/heart_model.json`**. Revenir dans Android Studio et cliquer sur **Build → Rebuild Project**, puis relancer `app`.

Le modèle est une **régression logistique scikit-learn**, exportée pour inférence sur Android. Le split test est 80/20 avec graine fixe ; les métriques affichées par Python sont des métriques expérimentales, non une validation clinique. Il est possible de tenter `py ml/train_export.py --download` à la place du téléchargement manuel ; cette option utilise un miroir public et nécessite Internet. Le modèle réel **n'est pas pré-entraîné dans ce ZIP** car le CSV Kaggle n'a pas été fourni.

## Collecte de données manuelle ou appareil intelligent

- **Saisie manuelle** des 11 variables du modèle : âge, sexe, douleur thoracique, pression au repos, cholestérol, glycémie à jeun, ECG, `MaxHR`, angine à l'effort, `Oldpeak`, pente ST.
- **Capteur BLE** : acquisition du **pouls actuel en bpm**, à condition d'avoir un capteur standard compatible. Il peut être enregistré comme **valeur additionnelle**, mais n'entre PAS dans les variables du modèle et ne remplace PAS `MaxHR` (fréquence maximale lors d'un effort).
- Les montres intelligentes sans service standard BLE Heart Rate peuvent nécessiter Health Connect ou une API propre au constructeur : **non intégrés** à ce projet.

## Structure des fichiers

```text
HeartCareAI/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradlew                       # lanceur macOS / Linux
├── gradlew.bat                   # lanceur Windows
├── gradle/wrapper/
│   ├── gradle-wrapper.jar        # bootstrap SHA-256 Gradle 8.13 fourni
│   └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/dz/univ/alger1/heartcareai/
│       │   ├── MainActivity.kt
│       │   ├── ble/HeartRateBleManager.kt
│       │   ├── data/LocalStore.kt
│       │   ├── data/Models.kt
│       │   ├── ml/OnDevicePredictor.kt
│       │   └── ui/{Screens.kt, Theme.kt}
│       └── res/                  # couleurs, icônes, logos universitaires
└── ml/
    ├── train_export.py
    ├── requirements.txt
    └── tests/test_train_export.py
```

## Limites et tests

- Le **code Python d'export ML** a été testé sur des données **synthétiques** ; cela **ne valide pas la qualité clinique du modèle**.
- Les fichiers Java du lanceur Gradle sont compilés et inclus. **Aucune compilation Android ni lancement sur téléphone n'a pu être effectué dans cet environnement**, car le SDK Android, Gradle et l'accès aux dépôts de dépendances y sont absents. Les premiers téléchargements et la compilation devront se dérouler dans Android Studio.
- Les comptes et les bilans sont stockés **localement dans une base SQLite non chiffrée**. Ne pas utiliser de données médicales personnelles réelles ou déployer auprès de patients sans mécanismes professionnels de confidentialité, de sécurité et de validation.
- La cible **`HeartDisease`** du CSV est la présence/absence enregistrée d'une **maladie cardiaque**, **pas** la survenue future ni un diagnostic spécifique d'insuffisance cardiaque.
- L'application est un **prototype pédagogique**, pas un dispositif médical.

## Si Android Studio affiche une erreur

Dans **View → Tool Windows → Build**, ouvrez **Build Output** et envoyez les **10–20 premières lignes d'erreur** pour diagnostiquer les dépendances, SDK ou compilation Kotlin.
