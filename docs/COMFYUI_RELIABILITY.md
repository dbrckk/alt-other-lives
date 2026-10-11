# ALT × ComfyUI — Fiabilité et validation manuelle

## Ce que garantit le client

- L'envoi distant d'une photo nécessite un consentement explicite dans les réglages. La copie envoyée est recadrée/réencodée en JPEG, sans les métadonnées EXIF de l'original.
- Chaque chapitre soumet au maximum une tâche ComfyUI par tentative. Un échec de consultation de l'historique ou de téléchargement **ne soumet pas une nouvelle tâche GPU**.
- Une image n'est choisie que lorsque le serveur confirme `status.completed = true`. La présence d'une prévisualisation intermédiaire ne suffit pas.
- L'envoi et le téléchargement sont bornés en taille. Le client vérifie l'annulation de coroutine entre les blocs du transfert, et supprime les fichiers de téléchargement partiels lors d'un échec ou d'une annulation.
- Le nom d'entrée distant est dérivé d'une empreinte opaque de la photo, du recadrage et du serveur. Une nouvelle génération identique peut remplacer la même entrée au lieu de créer des doublons.

## Limites techniques de l'annulation

La vérification entre les blocs n'interrompt pas nécessairement **une lecture/écriture réseau déjà bloquée** dans `HttpURLConnection`. Sa durée reste limitée principalement par les délais de connexion et de lecture configurés ; un serveur lent ou une réponse reçue au compte-gouttes peut allonger l'arrêt. Une tâche GPU déjà acceptée côté serveur n'est pas annulée automatiquement lorsque l'utilisateur quitte la génération.

## Annulation pendant les réponses HTTP et le contrôle qualité

Les réponses texte de ComfyUI (`/system_stats`, `/upload/image`,
`/prompt`, `/history/{id}`, ainsi que les corps d'erreur HTTP)
sont lues avec une limite de taille et vérifient maintenant l'annulation
**avant et après chaque bloc reçu**. Une annulation pendant un corps
d'erreur ne doit pas être convertie en erreur HTTP réessayable.
La lecture d'une image téléchargée et son inspection JPEG/PNG contrôlent
aussi l'annulation à la fin du décodage et de l'analyse visuelle ;
le fichier de cache est supprimé si cette analyse a été interrompue.

Ces points de contrôle ne peuvent pas interrompre immédiatement
`HttpURLConnection` bloqué à l'intérieur d'une lecture réseau.
Le délai dépend toujours du `readTimeout` de chaque requête.
La limite globale de polling ne constitue pas un délai réseau strict.

### Vérification manuelle conseillée

1. Avec un serveur ComfyUI de test en HTTPS qui renvoie une réponse
   `/history/{id}` volumineuse ou fractionnée lentement, annuler
   la génération et vérifier qu'elle ne reprend pas au bloc suivant.
2. Annuler pendant une réponse d'erreur HTTP fragmentée :
   l'interface ne doit pas annoncer un échec réessayable après l'annulation.
3. Annuler juste après réception d'une grande image mais avant la fin
   de l'inspection qualité, puis vérifier que le téléchargement temporaire
   n'est pas conservé dans `cache/generation`.

## Vérification fonctionnelle sur un vrai serveur

1. Configurer un serveur ComfyUI accessible en HTTPS et un workflow qui utilise les placeholders `__ALT_SOURCE_IMAGE__` et `__ALT_PROMPT__`. Activer explicitement le consentement d'envoi distant.
2. Importer une photo de test et générer une timeline complète. Vérifier la présence de chaque chapitre dans Reveal et sa persistance après redémarrage de l'application.
3. Relancer un chapitre avec la même photo et le même cadrage ; inspecter le répertoire `input` de ComfyUI. Vérifier que son nom n'a pas changé et qu'une entrée identique n'est pas ajoutée à chaque génération.
4. Tester un workflow produisant une image de prévisualisation avant son image finale : le chapitre affiché doit provenir d'une exécution **terminée**, pas d'une prévisualisation intermédiaire.
5. Sur un réseau limité, annuler explicitement une génération pendant l'envoi ou le téléchargement de l'image. Vérifier le retour de l'interface à un état non bloqué, l'absence de fuite de téléchargement dans le cache local et l'absence de lancement automatique d'un nouveau prompt GPU.
6. Répéter avec une coupure réseau temporaire et avec un certificat TLS invalide. La coupure peut entraîner une erreur récupérable ; l'erreur de certificat ne doit pas provoquer des tentatives automatiques.


## Annulation pendant la préparation JPEG

Avant l'envoi vers ComfyUI, ALT décode, oriente, recadre puis réencode
la photo en JPEG pour ne pas transférer les métadonnées EXIF de l'original.
La préparation est faite sur le dispatcher IO et vérifie désormais
l'annulation entre l'inspection des dimensions, le décodage, l'orientation,
le recadrage et la compression. Si l'annulation intervient pendant une
opération de décodage ou de compression synchrone, ALT vérifie son état
immédiatement après et supprime la copie JPEG avant toute tentative d'envoi.
Les bitmaps décodés sont libérés en cas d'annulation après décodage.

Une opération système déjà bloquée dans un fournisseur de documents Android
ne peut pas toujours être interrompue immédiatement ; tester la latence
sur des appareils et fournisseurs réels.

## Annulation pendant la persistance privée

Après téléchargement, ALT copie les images générées dans son stockage privé.
Cette copie vérifie désormais l'annulation **entre les blocs transférés** et
**avant de remplacer un chapitre existant**. Une copie partielle interrompue
est supprimée. Lors d'une régénération complète, l'annulation est contrôlée
pendant la préparation des images et avant le début de la transaction :
dès le déplacement des sauvegardes et le début du commit atomique, ALT laisse
cette courte phase s'achever pour préserver l'intégrité de l'ancienne ou
de la nouvelle version de la timeline.

Ces points de contrôle ne garantissent pas l'interruption immédiate d'un
`ContentProvider` bloqué en lecture, ni une annulation après que le commit
atomique a commencé. Le résultat doit être confirmé sur appareil avec un
flux lent et, pour l'ensemble du parcours, un vrai serveur ComfyUI.

### Vérification manuelle ciblée

1. Avec plusieurs scènes déjà générées, relancer un chapitre sur une
   image volontairement volumineuse et annuler pendant la persistance locale.
   Vérifier que le chapitre précédent reste lisible et qu'aucun fichier
   `.scene-*.tmp` ne subsiste après récupération.
2. Lancer une régénération complète avec cinq scènes, annuler pendant la
   préparation de la dernière scène et rouvrir la timeline. Les scènes
   précédentes et l'ancienne seed doivent rester intactes.
3. Répéter avec une interruption proche du commit final et redémarrer
   l'application pour vérifier que les scènes et la seed restent cohérentes.

## Intégrité des scènes lors d'une annulation sur émulateur

Le test instrumenté Android
`GeneratedScenePersistenceCancellationInstrumentedTest` utilise
le vrai `ContentResolver`, le vrai `FileProvider` de l'application
et son dossier privé `files/generated/`. Il couvre deux opérations :

- Annulation au cours de la copie d'un nouveau chapitre : les octets
  de l'ancien chapitre sont préservés et le fichier `.scene-*.tmp`
  de préparation est supprimé.
- Annulation après préparation des cinq images d'une régénération, juste
  avant le commit atomique : les cinq anciennes images et la seed
  d'origine restent inchangées, sans dossier `.batch-*` résiduel.

Le point d'annulation est injecté de façon déterministe après l'écriture
de données dans de **vrais fichiers Android** ; cela vérifie le rollback
et la conservation des médias sans simuler le système de stockage.
Ce test ne remplace pas une annulation au milieu d'une lecture bloquante
d'un fournisseur tiers, ni une vérification avec un serveur ComfyUI réel.

## Annulation pendant la préparation des images vidéo

Avant de produire un MP4, ALT prépare jusqu'à cinq images de chapitres
et les cartes d'introduction et de conclusion au format 9:16. La préparation
est exécutée hors du thread UI et vérifie maintenant l'annulation entre
les chapitres, après les décodages et avant/après l'encodage JPEG synchrone.

Une annulation à ce stade ne démarre pas le transcodeur vidéo et supprime
les JPEG temporaires de cet export, y compris ceux déjà finalisés. Le test
d'intégration Android `TimelineSceneRendererCancellationInstrumentedTest`
force une annulation après deux vrais JPEG de chapitres, puis contrôle
que le dossier privé ne contient pas de nouveaux fichiers d'export.

L'encodeur JPEG en cours ne peut pas être interrompu instantanément,
mais son résultat est éliminé au point de contrôle suivant. Vérifier
également sur un appareil physique l'annulation pendant la préparation
et pendant l'encodage MP4, puis le partage du fichier final.

## Vérifications automatisées existantes

Le workflow `.github/workflows/android.yml` exécute les tests JVM préexistants, les lints debug/release, la compilation APK, les tests instrumentés sur émulateur et le build release AAB. Il ne remplace **pas** les tests de bout en bout avec un serveur ComfyUI réel.

Les modifications de ce document sont des indications de validation ; elles ne constituent pas la preuve qu'un test manuel a été effectué.
