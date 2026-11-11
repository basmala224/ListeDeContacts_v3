package com.example.contactsv3.model

/**
 * Modèle d'un contact. L'identifiant [id] est stable : c'est lui (et non la
 * position dans la liste) qui est transmis entre fragments.
 */
data class Contact(
    val id: Long,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val nomComplet: String get() = "$prenom $nom".trim()
    val aUnePosition: Boolean get() = latitude != null && longitude != null
}
