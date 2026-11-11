package com.example.contactsv3.util

import com.example.contactsv3.model.Contact
import java.text.Normalizer
import java.util.Locale

/**
 * Filtrage pur (sans dépendance Android) -> testable en test unitaire.
 * - insensible à la casse (et aux accents) ;
 * - porte sur le nom, le prénom, "prénom nom" et le téléphone ;
 * - pour le téléphone, les espaces / tirets / points sont ignorés.
 */
object ContactFilter {

    fun filtrer(contacts: List<Contact>, requete: String?): List<Contact> {
        val q = normaliser(requete.orEmpty())
        if (q.isEmpty()) return contacts
        val qChiffres = chiffres(q)
        return contacts.filter { c ->
            normaliser(c.nom).contains(q) ||
                normaliser(c.prenom).contains(q) ||
                normaliser("${c.prenom} ${c.nom}").contains(q) ||
                normaliser("${c.nom} ${c.prenom}").contains(q) ||
                normaliser(c.telephone).contains(q) ||
                (qChiffres.isNotEmpty() && chiffres(c.telephone).contains(qChiffres))
        }
    }

    internal fun normaliser(s: String): String {
        val sansAccents = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
        return sansAccents.lowercase(Locale.ROOT).replace("\\s+".toRegex(), " ")
    }

    private fun chiffres(s: String): String = s.filter { it.isDigit() || it == '+' }
}
