# Compte rendu — TP Android Contacts V3

**Étudiante :** Aouida Basmala  
**Dépôt GitHub :** _à compléter_

> Exporter ce fichier en PDF (Android Studio, VS Code « Markdown PDF », ou Word) après avoir
> inséré les captures réelles dans `docs/captures/`.

## 1. Architecture des fragments

L'application ne contient qu'une activité, `MainActivity`, dont le layout est un
`FragmentContainerView`. Trois fragments y sont placés dynamiquement :

| Fragment | Rôle |
|---|---|
| `ListeContactsFragment` | champ de recherche + `RecyclerView` des contacts |
| `DetailContactFragment` | consultation / modification, Appeler, E-mail, Galerie, Caméra, coordonnées GPS |
| `CarteContactFragment` | carte Google Maps centrée sur le contact, avec marqueur |

La navigation passe par `MainActivity.naviguerVers()` :
`supportFragmentManager.commit { replace(...); addToBackStack(tag) }`.
Le bouton Retour dépile donc Carte → Détail → Liste.

**Rotation** : le fragment initial n'est ajouté que si `savedInstanceState == null` ; ensuite le
`FragmentManager` restaure lui-même la pile, ce qui évite les fragments en double. La carte est un
fragment enfant retrouvé par son tag avant d'en créer une nouvelle.

## 2. Choix de communication entre fragments

- **Liste → Détail / Détail → Carte** : `Bundle` (`arguments`) contenant l'identifiant du contact
  (et, pour la carte, les coordonnées actuellement saisies et validées).
- **Détail → Liste (actualisation)** : **ViewModel partagé** `ContactsViewModel`, obtenu par
  `activityViewModels()` dans les deux fragments.

Justification : la liste est un état partagé et durable. Avec un ViewModel, le Détail modifie la
source unique de vérité (`mettreAJour`) et la liste, qui observe une `LiveData`, se met à jour
automatiquement — même après une rotation. La Fragment Result API conviendrait pour un résultat
ponctuel, mais obligerait à recopier les données dans la liste et ne survivrait pas aussi
simplement aux recréations.

## 3. RecyclerView

- `ContactAdapter` hérite de `ListAdapter<Contact, ContactViewHolder>` avec un `DiffUtil.ItemCallback`
  (`areItemsTheSame` compare les id, `areContentsTheSame` compare les objets).
- `ContactViewHolder` garde les vues d'une ligne (ViewBinding `item_contact.xml`) : photo, nom,
  prénom, téléphone, icône indiquant si une position GPS existe.
- Les photos sont décodées en taille réduite (`inSampleSize`) pour éviter les dépassements mémoire.

## 4. Stratégie de filtrage

- Le texte saisi est envoyé au ViewModel à chaque frappe (`doAfterTextChanged`) — pas de bouton.
- `contactsFiltres` est une `MediatorLiveData` recalculée quand la liste ou la requête change.
- `ContactFilter.filtrer()` : comparaison en minuscules et sans accents sur nom, prénom,
  « prénom nom », « nom prénom » et téléphone ; pour le téléphone, espaces et séparateurs sont ignorés.
- Requête vide → toute la liste ; aucun résultat → message « Aucun contact ne correspond à … ».
- Le clic transmet l'objet `Contact` de la ligne (donc son id) : aucune erreur d'indice.

## 5. Intégration de Google Maps

- Dépendance `com.google.android.gms:play-services-maps`.
- Modèle étendu : `latitude: Double?`, `longitude: Double?`.
- Saisie dans le Détail avec validation (`CoordinateValidator`) : −90 ≤ lat ≤ 90,
  −180 ≤ lng ≤ 180, valeurs numériques, virgule acceptée, les deux champs ou aucun.
- `CarteContactFragment` ajoute un `SupportMapFragment`, puis dans `onMapReady` :
  marqueur titré au nom du contact + `CameraUpdateFactory.newLatLngZoom(position, 14f)`.
- Position absente / invalide / clé manquante → message explicite au lieu de la carte.
- Aucune permission de localisation : on affiche uniquement des coordonnées enregistrées.
- Clé API : `local.properties` (ignoré par Git) → `manifestPlaceholders` → `<meta-data>` du manifeste.

## 6. Tests réalisés

| # | Test | Résultat|
|---|---|---|---|---|
| 1 | Filtrer « trab », « SARRA », « 98555 » | contact correspondant | 
| 2 | Filtre « zzz » | message aucun résultat | 
| 3 | Effacer le filtre (croix) | toute la liste |
| 4 | Clic sur un contact d'une liste filtrée | bonne fiche |
| 5 | Modifier nom, tél., e-mail, photo, coordonnées → Enregistrer | liste actualisée | 
| 6 | Modifier puis Annuler | anciennes valeurs conservées |
| 7 | Champ obligatoire vide / e-mail invalide | erreur sous le champ | 
| 8 | Appeler / E-mail | composeur / messagerie préremplis | 
| 9 | Galerie / Caméra (+ annulation) | photo mise à jour / message d'annulation |
| 10 | Voir sur la carte (Sarra – Sfax) | carte centrée + marqueur | 
| 11 | Contact sans position (Leila) | message « aucune position » | 
| 12 | Latitude « abc », « 95 », longitude « 200 » | erreurs de validation | 
| 13 | Retour Carte → Détail → Liste | navigation correcte, saisie conservée | 
| 14 | Rotation sur chaque écran | pas de doublon, filtre et saisie conservés | 





## 7. Questions de compréhension

**1. Activity vs Fragment.** Une Activity est un composant système déclaré dans le manifeste,
point d'entrée avec sa propre fenêtre. Un Fragment est une portion d'interface réutilisable, avec
son propre cycle de vie (et celui de sa vue), qui doit toujours être hébergée par une Activity et
peut être ajoutée, remplacée ou retirée dynamiquement.

**2. FragmentContainerView et addToBackStack().** `FragmentContainerView` est le conteneur conçu
pour les fragments (ordre d'animation correct, gestion des insets, compatible avec le
`FragmentManager`). `addToBackStack()` enregistre la transaction dans la pile de retour : le bouton
Retour annule le `replace()` et réaffiche l'écran précédent au lieu de quitter l'application.

**3. Adapter et ViewHolder.** L'Adapter fait le lien entre les données et la RecyclerView : il crée
les vues (`onCreateViewHolder`), les remplit (`onBindViewHolder`) et indique le nombre d'éléments.
Le ViewHolder conserve les références vers les vues d'une ligne pour les recycler sans refaire de
`findViewById`, ce qui rend le défilement fluide.

**4. Éviter une erreur d'indice après filtrage.** Après filtrage, la position *n* de la liste
affichée ne correspond plus à la position *n* de la liste complète. On lit donc l'élément depuis la
liste réellement affichée par l'Adapter (`getItem(position)`) et on transmet l'objet ou son id,
jamais la position brute.

**5. Pourquoi transmettre l'identifiant.** L'id est stable : il ne change pas quand la liste est
filtrée, triée ou modifiée, ni après une rotation. Une position n'a de sens que dans une liste
donnée à un instant donné, et peut désigner un autre contact.

**6. Ne pas exposer la clé Google Maps.** La placer dans `local.properties` (ignoré par Git) et
l'injecter au build (`manifestPlaceholders` ou Secrets Gradle Plugin) ; vérifier le `.gitignore`
avant tout commit ; restreindre la clé dans Google Cloud (package + SHA-1, API Maps SDK uniquement)
pour qu'elle soit inutilisable ailleurs même si l'APK est décompilé ; si une clé a été poussée par
erreur, la révoquer et en générer une nouvelle.
