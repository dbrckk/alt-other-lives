# ALT — Stockage local des photos : cycle de vie et nettoyage

## Où les photos sont-elles stockées ?

ALT importe la photo choisie dans un dossier privé de l'application :
`files/source-photos/`. Le fichier initial est copié vers
`.source-<uuid>.tmp`, validé (dimensions, format, orientation), puis renommé
en `source-<uuid>.<extension>`. L'extension provient du type MIME choisi via
le sélecteur Android. Le chemin exact reste interne à l'application.

Les contenus temporaires servant à ComfyUI sont stockés séparément dans
`cache/generation/crops/`. Ils sont réencodés sans métadonnées EXIF avant
un envoi distant explicitement autorisé.

## Règles de nettoyage

| Situation | Action |
| --- | --- |
| Erreur pendant l'import actif | Suppression immédiate de sa copie temporaire et de sa cible partielle |
| Annulation de l'import actif | Vérification entre les blocs de copie et avant/après le renommage final ; suppression du fichier partiel ou finalisé ; l'annulation n'est pas enregistrée comme échec de photo |
| Annulation pendant le retour vers l'interface | L'application supprime aussi la photo finalisée si le résultat I/O n'a jamais été remis à l'écran |
| Redémarrage ou nouvel import | Purge des fichiers `.source-<uuid>.tmp` de plus de 24 h, jamais des imports récents |
| Suppression d'une timeline ou création d'une nouvelle timeline | `deleteUnreferenced` ne purge que des **photos finalisées**, reconnues par `SourcePhotoFileName`, non référencées ; les imports temporaires et la photo actuellement sélectionnée mais pas encore enregistrée sont préservés |
| Suppression explicite de tout l'historique | `clearAll` efface le répertoire source privé ; cette opération est destructive et ne doit pas être lancée en parallèle d'un import utilisateur |
| Nettoyage du cache IA | Nettoyage indépendant selon l'âge des fichiers et leur propriétaire |

Les imports provisoires trop récents peuvent subsister en cas d'arrêt brutal.
Ils seront éliminés lors d'un passage de nettoyage ultérieur, s'ils sont
encore présents au-delà de 24 h.

## Vérification fonctionnelle sur appareil

1. Sélectionner une photo via le sélecteur Android ; vérifier qu'un fichier
   `source-<uuid>.<extension>` validé est accessible dans l'application.
2. Enregistrer une timeline et lancer une seconde importation, notamment à
   partir d'un fournisseur de documents lent.
3. Pendant cette copie, provoquer une suppression de timeline ou une
   maintenance des fichiers non référencés. Vérifier que la copie en cours
   n'est pas supprimée par `deleteUnreferenced`.
4. Avec des imports interrompus artificiellement datés de moins puis de plus
   de 24 h, vérifier que seuls les anciens fichiers temporaires correspondant
   exactement au motif propriétaire sont supprimés lors de la récupération.
5. Importer une nouvelle photo **sans** encore créer de timeline, puis ouvrir
   l'historique et supprimer une ancienne timeline. Revenir à l'accueil :
   la photo active doit toujours être visible et utilisable.
6. Supprimer une timeline ancienne ; vérifier que les photos finalisées qui
   n'appartiennent plus à une timeline sont purgées, et que celles référencées
   ailleurs restent disponibles après redémarrage.

Le test instrumenté `SourcePhotoCancellationInstrumentedTest` vérifie sur
émulateur qu'une annulation pendant la copie ou après le renommage ne
laisse aucun fichier d'import privé, ainsi que le nettoyage explicite d'une
photo finalisée lors d'une annulation au retour vers l'interface. Une lecture
`ContentResolver` déjà bloquée reste tributaire du fournisseur Android :
la vérification d'annulation s'effectue **entre** les lectures et écritures.

Le test instrumenté `SourcePhotoPruneInstrumentedTest` vérifie sur émulateur
Android le nettoyage de fichiers réels dans le stockage privé (fichier
référencé, finalisé orphelin, temporaire récent, temporaire expiré et fichier
étranger). Le parcours complet de **navigation simultanée** entre import et
suppression de timeline reste un contrôle manuel à effectuer : il ne peut
pas être présumé validé par ce test.
