package com.example.contactsv3.ui.liste

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsv3.databinding.ItemContactBinding
import com.example.contactsv3.model.Contact
import com.example.contactsv3.util.ImageUtils

/**
 * Adapter de la RecyclerView. ListAdapter + DiffUtil calcule les différences
 * entre deux listes (filtrage, modification) et anime uniquement les lignes concernées.
 *
 * Le clic transmet le CONTACT de la ligne (donc son id), jamais la position :
 * aucune erreur d'indice possible après filtrage.
 */
class ContactAdapter(
    private val onClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(DIFF) {

    /** Le ViewHolder garde les références vers les vues d'une ligne (pas de findViewById répété). */
    inner class ContactViewHolder(private val b: ItemContactBinding) : RecyclerView.ViewHolder(b.root) {
        private var courant: Contact? = null

        init {
            b.root.setOnClickListener { courant?.let(onClick) }
        }

        fun bind(c: Contact) {
            courant = c
            b.tvNom.text = c.nom
            b.tvPrenom.text = c.prenom
            b.tvTelephone.text = c.telephone
            b.ivGps.alpha = if (c.aUnePosition) 1f else 0.2f
            b.ivGps.contentDescription = b.root.context.getString(
                if (c.aUnePosition) com.example.contactsv3.R.string.cd_position_ok
                else com.example.contactsv3.R.string.cd_position_absente
            )
            ImageUtils.afficher(b.ivPhoto, c.photoUri, 160)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder =
        ContactViewHolder(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(a: Contact, b: Contact) = a.id == b.id
            override fun areContentsTheSame(a: Contact, b: Contact) = a == b
        }
    }
}
