# ALT × ComfyUI — Fiabilité et validation manuelle

## Ce que garantit le client

- L'envoi distant d'une photo nécessite un consentement explicite dans les réglages. La copie envoyée est recadrée/réencodée en JPEG, sans les métadonnées EXIF de l'original.
- Chaque chapitre soumet au maximum une tâche ComfyUI par tentative. Un échec de consultation de l'historique ou de téléchargement **ne soumet pas une nouvelle tâche GPU**.
- Une image n'est choisie que lorsque le serveur confirme `status.completed = true`. La présence d'une prévisualisation intermédiaire ne suffit pas.
- L'envoi et le téléchargement sont bornés en taille. Le client vérifie l'annulation de coroutine entre les blocs du transfert, et supprime les fichiers de téléchargement partiels lors d'un échec ou d'une annulation.
- Le nom d'entrée distant est dérivé d'une empreinte opaque de la photo, du recadrage et du serveur. Une nouvelle génération identique peut remplacer la même entrée au lieu de créer des doublons.

## Limites techniques de l'annulation

La vérification entre les blocs n'interrompt pas nécessairement **une lecture/écriture réseau déjà bloquée** dans `HttpURLConnection`. Sa durée reste limitée principalement par les délais de connexion et de lecture configurés ; un serveur lent ou une réponse reçue au compte-gouttes peut allonger l'arrêt. Une tâche GPU déjà acceptée côté serveur n'est pas annulée automatiquement lorsque l'utilisateur quitte la génération.

## Vérification fonctionnelle sur un vrai serveur

1. Configurer un serveur ComfyUI accessible en HTTPS et un workflow qui utilise les placeholders `__ALT_SOURCE_IMAGE__` et `__ALT_PROMPT__`. Activer explicitement le consentement d'envoi distant.
2. Importer une photo de test et générer une timeline complète. Vérifier la présence de chaque chapitre dans Reveal et sa persistance après redémarrage de l'application.
3. Relancer un chapitre avec la même photo et le même cadrage ; inspecter le répertoire `input` de ComfyUI. Vérifier que son nom n'a pas changé et qu'une entrée identique n'est pas ajoutée à chaque génération.
4. Tester un workflow produisant une image de prévisualisation avant son image finale : le chapitre affiché doit provenir d'une exécution **terminée**, pas d'une prévisualisation intermédiaire.
5. Sur un réseau limité, annuler explicitement une génération pendant l'envoi ou le téléchargement de l'image. Vérifier le retour de l'interface à un état non bloqué, l'absence de fuite de téléchargement dans le cache local et l'absence de lancement automatique d'un nouveau prompt GPU.
6. Répéter avec une coupure réseau temporaire et avec un certificat TLS invalide. La coupure peut entraîner une erreur récupérable ; l'erreur de certificat ne doit pas provoquer des tentatives automatiques.

## Vérifications automatisées existantes

Le workflow `.github/workflows/android.yml` exécute les tests JVM préexistants, les lints debug/release, la compilation APK, les tests instrumentés sur émulateur et le build release AAB. Il ne remplace **pas** les tests de bout en bout avec un serveur ComfyUI réel.

Les modifications de ce document sont des indications de validation ; elles ne constituent pas la preuve qu'un test manuel a été effectué.
