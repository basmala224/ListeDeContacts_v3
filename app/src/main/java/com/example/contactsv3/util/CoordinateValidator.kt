package com.example.contactsv3.util

/** Résultat de la validation des coordonnées saisies. */
sealed class CoordResult {
    /** Aucune position (les deux champs vides). */
    data object Absente : CoordResult()
    data class Valide(val latitude: Double, val longitude: Double) : CoordResult()
    data class Erreur(val champLatitude: String?, val champLongitude: String?) : CoordResult()
}

/** Validation pure, testable : latitude [-90 ; 90], longitude [-180 ; 180]. */
object CoordinateValidator {

    const val ERR_NON_NUMERIQUE = "Valeur non numérique"
    const val ERR_LAT_HORS_LIMITES = "La latitude doit être comprise entre -90 et 90"
    const val ERR_LNG_HORS_LIMITES = "La longitude doit être comprise entre -180 et 180"
    const val ERR_MANQUANTE = "Renseigner les deux coordonnées (ou aucune)"

    fun valider(latTexte: String?, lngTexte: String?): CoordResult {
        val latS = latTexte?.trim().orEmpty()
        val lngS = lngTexte?.trim().orEmpty()
        if (latS.isEmpty() && lngS.isEmpty()) return CoordResult.Absente

        var errLat: String? = null
        var errLng: String? = null
        val lat = parse(latS)
        val lng = parse(lngS)

        when {
            latS.isEmpty() -> errLat = ERR_MANQUANTE
            lat == null -> errLat = ERR_NON_NUMERIQUE
            lat < -90.0 || lat > 90.0 -> errLat = ERR_LAT_HORS_LIMITES
        }
        when {
            lngS.isEmpty() -> errLng = ERR_MANQUANTE
            lng == null -> errLng = ERR_NON_NUMERIQUE
            lng < -180.0 || lng > 180.0 -> errLng = ERR_LNG_HORS_LIMITES
        }
        return if (errLat == null && errLng == null) CoordResult.Valide(lat!!, lng!!)
        else CoordResult.Erreur(errLat, errLng)
    }

    /** Accepte la virgule décimale française ; refuse NaN / Infinity. */
    fun parse(s: String): Double? {
        val d = s.replace(',', '.').toDoubleOrNull() ?: return null
        return if (d.isNaN() || d.isInfinite()) null else d
    }

    fun estValide(lat: Double?, lng: Double?): Boolean =
        lat != null && lng != null && !lat.isNaN() && !lng.isNaN() &&
            lat in -90.0..90.0 && lng in -180.0..180.0
}
