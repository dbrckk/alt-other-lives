# ALT Other Lives — Privacy Policy / Politique de confidentialité

_Last updated / Dernière mise à jour: 7 October 2026_

## English

ALT Other Lives is designed to keep personal content on the device by default.

### Data stored on the device

When you select a source photo, ALT copies it into the app's private storage so it can be framed, reused in a timeline, and restored after app restarts. Generated images, timeline history, AI configuration, and the local diagnostics journal are also stored locally.

The diagnostics journal is intentionally limited to technical event codes, exception types, internal ALT code locations, timestamps, and the app version. It does not store photos, prompts, server URLs, URIs, file paths, workflow contents, or raw exception messages.

### Remote AI generation

ALT currently supports a user-configured ComfyUI endpoint. Remote AI generation is disabled unless you configure an endpoint, provide a compatible workflow, and explicitly consent to uploading the selected source photo.

When remote generation is used, ALT sends the framed source image and the workflow request required for generation to the ComfyUI server you configured. That server is operated by you or by a third party you chose. Its operator may process or retain data according to its own policies.

The current ALT app does not operate a managed AI backend and does not receive this remote generation traffic by default.

### Analytics, advertising, and automatic telemetry

The current ALT app does not include advertising SDKs, analytics SDKs, or automatic remote crash-reporting SDKs. Technical diagnostics stay on the device unless you explicitly copy and share them.

### Network security

ALT requires HTTPS for configured remote ComfyUI endpoints and disables cleartext network traffic at the Android application level.

### Retention and deletion

Local photos, generated media, settings, history, and diagnostics remain on the device until you delete them through the app where an applicable control exists, clear the app's data, or uninstall the app.

Data sent to a configured ComfyUI server is governed by that server's retention and deletion behavior.

### Your choices

You can use ALT without remote AI generation. You can clear AI settings, history, and local diagnostics from the app. You can also remove all app data using Android system settings or by uninstalling ALT.

### Privacy questions

For privacy-related questions, use the repository's public issue tracker without posting personal data:
https://github.com/dbrckk/alt-other-lives/issues

---

## Français

ALT Other Lives est conçu pour conserver les contenus personnels sur l'appareil par défaut.

### Données stockées sur l'appareil

Lorsque vous sélectionnez une photo source, ALT la copie dans le stockage privé de l'application afin de permettre le cadrage, sa réutilisation dans une timeline et sa restauration après redémarrage. Les images générées, l'historique, la configuration IA et le journal de diagnostics local sont également stockés localement.

Le journal de diagnostics est volontairement limité à des codes d'événement techniques, des types d'exception, des emplacements internes du code ALT, des horodatages et la version de l'application. Il ne stocke pas les photos, prompts, URL de serveur, URI, chemins de fichiers, contenus de workflow ni messages d'exception bruts.

### Génération IA distante

ALT prend actuellement en charge un endpoint ComfyUI configuré par l'utilisateur. La génération IA distante reste désactivée tant que vous n'avez pas configuré un endpoint, fourni un workflow compatible et explicitement accepté l'envoi de la photo source sélectionnée.

Lorsque la génération distante est utilisée, ALT envoie l'image cadrée et la requête de workflow nécessaire à la génération vers le serveur ComfyUI que vous avez configuré. Ce serveur est exploité par vous ou par un tiers que vous avez choisi. Son opérateur peut traiter ou conserver les données selon ses propres règles.

La version actuelle d'ALT n'exploite pas de backend IA géré par ALT et ne reçoit pas par défaut le trafic de génération distante.

### Analytique, publicité et télémétrie automatique

La version actuelle d'ALT n'intègre pas de SDK publicitaire, de SDK analytique ni de SDK de remontée automatique des crashs. Les diagnostics techniques restent sur l'appareil sauf si vous choisissez explicitement de les copier et de les partager.

### Sécurité réseau

ALT impose HTTPS pour les endpoints ComfyUI distants configurés et bloque le trafic réseau en clair au niveau de l'application Android.

### Conservation et suppression

Les photos locales, médias générés, réglages, historique et diagnostics restent sur l'appareil jusqu'à leur suppression via les contrôles disponibles dans l'application, l'effacement des données de l'application ou la désinstallation.

Les données envoyées à un serveur ComfyUI configuré dépendent des règles de conservation et de suppression de ce serveur.

### Vos choix

Vous pouvez utiliser ALT sans génération IA distante. Vous pouvez effacer les réglages IA, l'historique et les diagnostics locaux depuis l'application. Vous pouvez également supprimer toutes les données d'ALT via les réglages Android ou en désinstallant l'application.

### Questions relatives à la confidentialité

Pour toute question liée à la confidentialité, utilisez le gestionnaire public d'issues du dépôt sans y publier de données personnelles :
https://github.com/dbrckk/alt-other-lives/issues
