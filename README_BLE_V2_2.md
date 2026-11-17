# HeartCare AI V2.2 — Écran « Mes capteurs »

## Améliorations

- Trois fiches avec de **vraies photographies** : Ceinture cardiaque, Capteur optique, Montre intelligente.
- Badges **Connecté / Non connecté** déterminés à partir de la liaison GATT réelle, jamais simulés.
- Scan BLE de 15 secondes, liste des appareils détectés, qualité de signal RSSI.
- Connexion **manuelle à l'appareil choisi** ; la version précédente sélectionnait automatiquement le premier appareil.
- Vérification des services Bluetooth standards **0x180D** et **0x2A37** ; notification GATT obligatoire pour afficher « Connecté ».
- Mesure du pouls affichée uniquement si elle a été reçue d'un capteur connecté.
- Gestion des erreurs d'autorisation, de connexion et des appareils sans service cardiaque standard.

## Visualiser

Ouvrez `HeartCareAI/` avec Android Studio puis l'écran « Capteur » en bas de l'interface. Un aperçu Compose figure dans `ModernPreviews.kt` (`BleSensorPreview`).

## Conditions réelles

1. Exécuter sur **un vrai téléphone Android** avec BLE. L'émulateur ne permet généralement pas de scanner des périphériques BLE physiques.
2. Accorder les permissions Bluetooth lorsqu'elles sont demandées et activer le Bluetooth.
3. Porter/réveiller le capteur, rechercher, puis appuyer sur **Connecter** sur le modèle détecté.
4. Seul **un capteur** peut être connecté à la fois. « Non connecté » reste visible lorsqu'un modèle est simplement listé ou détecté.
5. Les capteurs doivent exposer le profil BLE Heart Rate standard ; d'autres produits peuvent demander une API dédiée. La compatibilité théorique n'est pas un test physique réussi.

## Photos

Les photos proviennent de Wikimedia Commons, sous licences CC BY-SA. Elles sont chargées **depuis Internet lors de la première visite** puis mises en cache pour l'ouverture suivante. Sans réseau ou si Wikimedia est indisponible, une icône de remplacement s'affiche. Le projet contient les crédits détaillés dans `CREDITS_PHOTOS_BLE.md`.

La présence d'une photo ne signifie pas que l'appareil est présent. Les images ne sont pas des données du périphérique.

## Sécurité des données

Le pouls instantané **n'est pas la variable MaxHR** du modèle Kaggle. La connexion BLE est un suivi complémentaire, pas un instrument médical ni un diagnostic.
