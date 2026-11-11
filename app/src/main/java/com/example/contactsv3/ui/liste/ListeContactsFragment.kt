package com.example.contactsv3.ui.liste

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactsv3.MainActivity
import com.example.contactsv3.R
import com.example.contactsv3.data.ContactsViewModel
import com.example.contactsv3.databinding.FragmentListeContactsBinding
import com.example.contactsv3.ui.detail.DetailContactFragment

/** Écran 1 : liste des contacts + recherche instantanée. */
class ListeContactsFragment : Fragment() {

    private var _binding: FragmentListeContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListeContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.titre_liste)

        adapter = ContactAdapter { contact ->
            // On transmet l'IDENTIFIANT du contact (Bundle), pas la position de la ligne.
            (requireActivity() as MainActivity).naviguerVers(
                DetailContactFragment.newInstance(contact.id), DetailContactFragment.TAG
            )
        }
        binding.recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerContacts.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        binding.recyclerContacts.adapter = adapter

        // Le filtre courant est conservé dans le ViewModel (rotation, retour du détail).
        val requeteCourante = viewModel.requete.value.orEmpty()
        if (binding.etRecherche.text?.toString() != requeteCourante) {
            binding.etRecherche.setText(requeteCourante)
        }
        // Filtrage au fil de la saisie, sans bouton de validation.
        binding.etRecherche.doAfterTextChanged { viewModel.setRequete(it?.toString().orEmpty()) }

        viewModel.contactsFiltres.observe(viewLifecycleOwner) { liste ->
            adapter.submitList(liste)
            val vide = liste.isEmpty()
            binding.tvAucunResultat.isVisible = vide
            binding.recyclerContacts.isVisible = !vide
            if (vide) {
                binding.tvAucunResultat.text = getString(
                    R.string.aucun_resultat, viewModel.requete.value.orEmpty()
                )
            }
            binding.tvCompteur.text = resources.getQuantityString(
                R.plurals.nb_contacts, liste.size, liste.size
            )
        }
    }

    override fun onDestroyView() {
        binding.recyclerContacts.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "liste"
    }
}
