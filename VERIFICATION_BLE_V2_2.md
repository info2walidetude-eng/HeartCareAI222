# HeartCare AI V2.2 — Vérification du module BLE

Date : 17 novembre 2026.

## Validé dans cet environnement

- Catalogue compilé avec Kotlin JVM local : **réussi**.
- 10 assertions Kotlin : **réussies** (catalogue, reconnaissance des noms, décodage 8/16 bits et trames invalides).
- 2 tests Python du module Machine Learning : **réussis**.
- 6 fichiers XML Android analysés : **valides**.
- Vérification du lien `MainActivity` → `SensorScreen` → `HeartRateBleManager.connect(address)` : **réussie**.
- Aucune seconde définition de `SensorScreen` : **vérifié**.
- Syntaxe de l'UI analysée par le parseur Kotlin : pas d'erreur de type `expecting` ; les références Compose ne sont pas disponibles pour une compilation intégrale dans cet environnement.

## Non vérifié

- **Compilation complète APK Android** : SDK et dépendances Gradle indisponibles dans cet environnement.
- **Détection et connexion réelles** aux capteurs Polar H10/OH1/Wahoo TICKR : appareil Bluetooth physique non disponible.
- **Téléchargement automatique des photographies sur Android** : nécessite une connexion Internet réelle. Les URL correspondent à des photos Wikimedia Commons, avec repli visuel en cas d'échec.

## Test manuel recommandé

1. Ouvrir le dossier `HeartCareAI/` dans Android Studio et synchroniser Gradle.
2. Construire `app` (Build → Make Project) et résoudre le cas échéant les erreurs Android Studio.
3. Installer sur un téléphone Android BLE, activer le Bluetooth et accorder les permissions.
4. Vérifier que les 3 modèles sont « Non connecté » avant recherche.
5. Mettre un Polar H10/OH1 ou Wahoo TICKR en fonctionnement, puis lancer « Rechercher les appareils ».
6. Vérifier le signal RSSI et le nom relevé ; appuyer sur « Connecter ».
7. Vérifier « Connecté » **uniquement** après activation des notifications GATT du service standard 0x180D et de la caractéristique 0x2A37.
8. Vérifier la réception d'un pouls en bpm ; utiliser « Déconnecter ».
9. Tester l'absence d'appareil et le refus des permissions, sans afficher de faux état connecté.
10. Tester les photos avec et sans Internet pour confirmer le chargement et le repli.

Ne pas utiliser comme dispositif médical ou outil diagnostique.


Mise à jour V2.3 : remplacement du catalogue par trois catégories d'appareils (montre intelligente, capteur optique, ceinture cardiaque) avec photos illustratives et statut Connecté/Non connecté.
