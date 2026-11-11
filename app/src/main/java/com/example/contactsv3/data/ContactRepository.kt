package com.example.contactsv3.data

import com.example.contactsv3.model.Contact

/** Données de démonstration en mémoire (le stockage permanent n'est pas exigé). */
object ContactRepository {
    fun contactsDemo(): List<Contact> = listOf(
        Contact(1, "Ben Ali", "Amine", "+216 20 123 456", "amine.benali@example.com",
            latitude = 36.8065, longitude = 10.1815),          // Tunis
        Contact(2, "Trabelsi", "Sarra", "+216 22 987 654", "sarra.trabelsi@example.com",
            latitude = 34.7406, longitude = 10.7603),          // Sfax
        Contact(3, "Gharbi", "Youssef", "+216 98 555 111", "youssef.gharbi@example.com",
            latitude = 35.8256, longitude = 10.6360),          // Sousse
        Contact(4, "Mansour", "Leila", "+216 55 444 333", "leila.mansour@example.com"), // pas de position
        Contact(5, "Dupont", "Marie", "+33 6 12 34 56 78", "marie.dupont@example.com",
            latitude = 48.8584, longitude = 2.2945),           // Paris
        Contact(6, "Hammami", "Karim", "+216 29 777 888", "karim.hammami@example.com",
            latitude = 33.8869, longitude = 9.5375),           // Gafsa / centre
        Contact(7, "Jlassi", "Nour", "+216 21 333 222", "nour.jlassi@example.com")  // pas de position
    )
}
