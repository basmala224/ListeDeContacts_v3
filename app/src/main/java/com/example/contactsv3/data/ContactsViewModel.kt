package com.example.contactsv3.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.contactsv3.model.Contact
import com.example.contactsv3.util.ContactFilter

/**
 * ViewModel PARTAGÉ (portée : MainActivity, obtenu via activityViewModels()).
 * - Source unique de vérité pour la liste des contacts ;
 * - survit à la rotation (pas de rechargement, pas de perte du filtre) ;
 * - toute modification enregistrée dans le Détail est immédiatement observée
 *   par la liste (LiveData) : pas besoin de renvoyer manuellement un résultat.
 */
class ContactsViewModel : ViewModel() {

    private val _contacts = MutableLiveData(ContactRepository.contactsDemo())
    val contacts: LiveData<List<Contact>> = _contacts

    private val _requete = MutableLiveData("")
    val requete: LiveData<String> = _requete

    /** Liste affichée = liste complète filtrée par la requête courante. */
    val contactsFiltres: LiveData<List<Contact>> = MediatorLiveData<List<Contact>>().apply {
        fun maj() { value = ContactFilter.filtrer(_contacts.value.orEmpty(), _requete.value) }
        addSource(_contacts) { maj() }
        addSource(_requete) { maj() }
    }

    fun setRequete(texte: String) {
        if (texte != _requete.value) _requete.value = texte
    }

    fun getContact(id: Long): Contact? = _contacts.value?.firstOrNull { it.id == id }

    fun mettreAJour(contact: Contact) {
        _contacts.value = _contacts.value.orEmpty().map { if (it.id == contact.id) contact else it }
    }
}
