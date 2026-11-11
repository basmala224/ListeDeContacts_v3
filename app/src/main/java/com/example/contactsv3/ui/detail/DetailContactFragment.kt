package com.example.contactsv3.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.contactsv3.MainActivity
import com.example.contactsv3.R
import com.example.contactsv3.data.ContactsViewModel
import com.example.contactsv3.databinding.FragmentDetailContactBinding
import com.example.contactsv3.model.Contact
import com.example.contactsv3.ui.carte.CarteContactFragment
import com.example.contactsv3.util.CoordResult
import com.example.contactsv3.util.CoordinateValidator
import com.example.contactsv3.util.ImageUtils
import java.io.File
import java.util.Locale

/**
 * Écran 2 : consultation / modification d'un contact.
 * Reçoit uniquement l'identifiant du contact dans ses arguments (Bundle).
 * Les modifications restent un brouillon tant que "Enregistrer" n'est pas pressé.
 */
class DetailContactFragment : Fragment() {

    private var _binding: FragmentDetailContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels()

    private val contactId: Long by lazy { requireArguments().getLong(ARG_ID, -1L) }

    /** Photo choisie mais pas encore enregistrée (brouillon). */
    private var photoBrouillon: String? = null
    /** Fichier en cours de capture par la caméra. */
    private var photoCameraEnCours: String? = null
    private var champsRemplis = false

    // ---- Galerie : OpenDocument permet de conserver l'accès à l'URI (permission persistante)
    private val choisirImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            toast(R.string.msg_galerie_annulee)
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) { /* accès limité à la session : acceptable */ }
        photoBrouillon = uri.toString()
        afficherPhoto()
    }

    // ---- Caméra : l'image est écrite dans un fichier partagé via FileProvider
    private val prendrePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val chemin = photoCameraEnCours
        photoCameraEnCours = null
        if (ok && chemin != null) {
            photoBrouillon = chemin
            afficherPhoto()
        } else {
            toast(R.string.msg_camera_annulee)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            photoBrouillon = savedInstanceState.getString(STATE_PHOTO)
            photoCameraEnCours = savedInstanceState.getString(STATE_CAMERA)
            champsRemplis = true // les EditText restaurent eux-mêmes leur texte
        } else {
            photoBrouillon = viewModel.getContact(contactId)?.photoUri
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contact = viewModel.getContact(contactId)
        if (contact == null) {
            toast(R.string.msg_contact_introuvable)
            parentFragmentManager.popBackStack()
            return
        }
        requireActivity().title = contact.nomComplet

        if (!champsRemplis) {
            remplirChamps(contact)
            champsRemplis = true
        }
        afficherPhoto()
        afficherResumePosition(contact)

        binding.btnAppeler.setOnClickListener { appeler() }
        binding.btnEmail.setOnClickListener { envoyerEmail() }
        binding.btnGalerie.setOnClickListener { ouvrirGalerie() }
        binding.btnCamera.setOnClickListener { ouvrirCamera() }
        binding.btnCarte.setOnClickListener { voirSurLaCarte() }
        binding.btnEnregistrer.setOnClickListener { enregistrer(contact) }
        binding.btnAnnuler.setOnClickListener {
            toast(R.string.msg_modifs_annulees)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PHOTO, photoBrouillon)
        outState.putString(STATE_CAMERA, photoCameraEnCours)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // ------------------------------------------------------------------ affichage

    private fun remplirChamps(c: Contact) = with(binding) {
        etNom.setText(c.nom)
        etPrenom.setText(c.prenom)
        etTelephone.setText(c.telephone)
        etEmail.setText(c.email)
        etLatitude.setText(c.latitude?.let { formater(it) }.orEmpty())
        etLongitude.setText(c.longitude?.let { formater(it) }.orEmpty())
    }

    private fun afficherResumePosition(c: Contact) {
        binding.tvPosition.text = if (c.aUnePosition) {
            getString(R.string.position_enregistree, formater(c.latitude!!), formater(c.longitude!!))
        } else {
            getString(R.string.position_absente)
        }
    }

    private fun afficherPhoto() {
        _binding?.let { ImageUtils.afficher(it.ivPhoto, photoBrouillon, 480) }
    }

    private fun formater(d: Double) = String.format(Locale.US, "%.6f", d).trimEnd('0').trimEnd('.')

    // ------------------------------------------------------------------ actions V2

    private fun appeler() {
        val tel = binding.etTelephone.text?.toString()?.trim().orEmpty()
        if (tel.isEmpty()) {
            binding.tilTelephone.error = getString(R.string.err_obligatoire)
            return
        }
        // ACTION_DIAL : ouvre le composeur, aucune permission CALL_PHONE nécessaire.
        lancer(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(tel))), R.string.err_pas_de_composeur)
    }

    private fun envoyerEmail() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_email)
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        }
        lancer(intent, R.string.err_pas_de_messagerie)
    }

    private fun ouvrirGalerie() {
        try {
            choisirImage.launch(arrayOf("image/*"))
        } catch (_: ActivityNotFoundException) {
            toast(R.string.err_pas_de_galerie)
        }
    }

    private fun ouvrirCamera() {
        try {
            val dossier = File(requireContext().filesDir, "photos").apply { mkdirs() }
            val fichier = File(dossier, "contact_${contactId}_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", fichier
            )
            photoCameraEnCours = uri.toString()
            prendrePhoto.launch(uri)
        } catch (_: ActivityNotFoundException) {
            photoCameraEnCours = null
            toast(R.string.err_pas_de_camera)
        } catch (_: Exception) {
            photoCameraEnCours = null
            toast(R.string.err_camera)
        }
    }

    private fun lancer(intent: Intent, erreur: Int) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            toast(erreur)
        }
    }

    // ------------------------------------------------------------------ carte

    private fun voirSurLaCarte() {
        val args = bundleOf(CarteContactFragment.ARG_ID to contactId)
        when (val r = validerCoordonnees()) {
            is CoordResult.Valide -> {
                args.putString(CarteContactFragment.ARG_ETAT, CarteContactFragment.ETAT_VALIDE)
                args.putDouble(CarteContactFragment.ARG_LAT, r.latitude)
                args.putDouble(CarteContactFragment.ARG_LNG, r.longitude)
            }
            CoordResult.Absente ->
                args.putString(CarteContactFragment.ARG_ETAT, CarteContactFragment.ETAT_ABSENTE)
            is CoordResult.Erreur ->
                args.putString(CarteContactFragment.ARG_ETAT, CarteContactFragment.ETAT_INVALIDE)
        }
        args.putString(CarteContactFragment.ARG_TITRE, nomCompletSaisi())
        (requireActivity() as MainActivity).naviguerVers(
            CarteContactFragment().apply { arguments = args }, CarteContactFragment.TAG
        )
    }

    private fun nomCompletSaisi(): String {
        val p = binding.etPrenom.text?.toString()?.trim().orEmpty()
        val n = binding.etNom.text?.toString()?.trim().orEmpty()
        return "$p $n".trim()
    }

    // ------------------------------------------------------------------ validation / enregistrement

    private fun validerCoordonnees(): CoordResult {
        val r = CoordinateValidator.valider(
            binding.etLatitude.text?.toString(), binding.etLongitude.text?.toString()
        )
        binding.tilLatitude.error = (r as? CoordResult.Erreur)?.champLatitude
        binding.tilLongitude.error = (r as? CoordResult.Erreur)?.champLongitude
        return r
    }

    private fun enregistrer(ancien: Contact) {
        val nom = binding.etNom.text?.toString()?.trim().orEmpty()
        val prenom = binding.etPrenom.text?.toString()?.trim().orEmpty()
        val tel = binding.etTelephone.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()

        var ok = true
        binding.tilNom.error = if (nom.isEmpty()) getString(R.string.err_obligatoire).also { ok = false } else null
        binding.tilPrenom.error = if (prenom.isEmpty()) getString(R.string.err_obligatoire).also { ok = false } else null
        binding.tilTelephone.error = when {
            tel.isEmpty() -> getString(R.string.err_obligatoire).also { ok = false }
            !tel.matches(Regex("^[+]?[0-9 .()-]{6,20}$")) -> getString(R.string.err_telephone).also { ok = false }
            else -> null
        }
        binding.tilEmail.error = when {
            email.isEmpty() -> getString(R.string.err_obligatoire).also { ok = false }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.err_email).also { ok = false }
            else -> null
        }
        val coord = validerCoordonnees()
        if (coord is CoordResult.Erreur) ok = false

        if (!ok) {
            toast(R.string.msg_corriger_erreurs)
            return
        }

        val (lat, lng) = when (coord) {
            is CoordResult.Valide -> coord.latitude to coord.longitude
            else -> null to null
        }
        viewModel.mettreAJour(
            ancien.copy(
                nom = nom, prenom = prenom, telephone = tel, email = email,
                photoUri = photoBrouillon, latitude = lat, longitude = lng
            )
        )
        toast(R.string.msg_enregistre)
        parentFragmentManager.popBackStack() // retour à la liste, déjà actualisée via LiveData
    }

    private fun toast(res: Int) = Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show()

    companion object {
        const val TAG = "detail"
        private const val ARG_ID = "contact_id"
        private const val STATE_PHOTO = "photo_brouillon"
        private const val STATE_CAMERA = "photo_camera"

        fun newInstance(id: Long) = DetailContactFragment().apply {
            arguments = bundleOf(ARG_ID to id)
        }
    }
}
