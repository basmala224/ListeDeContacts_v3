package com.example.contactsv3

import com.example.contactsv3.data.ContactRepository
import com.example.contactsv3.util.ContactFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactFilterTest {

    private val contacts = ContactRepository.contactsDemo()

    @Test fun requeteVide_renvoieTout() {
        assertEquals(contacts.size, ContactFilter.filtrer(contacts, "").size)
        assertEquals(contacts.size, ContactFilter.filtrer(contacts, "   ").size)
        assertEquals(contacts.size, ContactFilter.filtrer(contacts, null).size)
    }

    @Test fun filtreParNom_insensibleALaCasse() {
        val r = ContactFilter.filtrer(contacts, "TRABELSI")
        assertEquals(listOf(2L), r.map { it.id })
    }

    @Test fun filtreParPrenom() {
        val r = ContactFilter.filtrer(contacts, "leila")
        assertEquals(listOf(4L), r.map { it.id })
    }

    @Test fun filtreParPrenomEtNom() {
        assertEquals(listOf(5L), ContactFilter.filtrer(contacts, "marie dup").map { it.id })
    }

    @Test fun filtreParTelephone_ignoreLesEspaces() {
        assertEquals(listOf(3L), ContactFilter.filtrer(contacts, "98555").map { it.id })
        assertEquals(listOf(3L), ContactFilter.filtrer(contacts, "98 555 111").map { it.id })
    }

    @Test fun aucunResultat() {
        assertTrue(ContactFilter.filtrer(contacts, "zzzz").isEmpty())
    }

    @Test fun lIdDuContactFiltreEstConserve() {
        // Après filtrage, la position 0 correspond à l'id 2 et non à l'id 1 :
        // c'est pourquoi on navigue avec l'id du contact, jamais avec la position.
        val r = ContactFilter.filtrer(contacts, "sarra")
        assertEquals(2L, r[0].id)
    }
}
