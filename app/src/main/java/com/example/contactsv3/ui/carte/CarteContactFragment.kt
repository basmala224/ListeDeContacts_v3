package com.example.contactsv3.ui.carte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.contactsv3.BuildConfig
import com.example.contactsv3.R
import com.example.contactsv3.databinding.FragmentCarteContactBinding
import com.example.contactsv3.util.CoordinateValidator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

/**
 * Écran 3 : position GPS du contact sur Google Maps.
 * Aucune géolocalisation de l'appareil -> aucune permission de localisation.
 * La SupportMapFragment n'est ajoutée (fragment enfant) que si la position est
 * valide ET qu'une clé API est configurée ; sinon un message explicite s'affiche.
 */
class CarteContactFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarteContactBinding? = null
    private val binding get() = _binding!!

    private var position: LatLng? = null
    private var titre: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCarteContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        titre = args.getString(ARG_TITRE).orEmpty()
        requireActivity().title = getString(R.string.titre_carte, titre)
        binding.btnRetourDetail.setOnClickListener { parentFragmentManager.popBackStack() }

        val etat = args.getString(ARG_ETAT, ETAT_ABSENTE)
        val lat = if (args.containsKey(ARG_LAT)) args.getDouble(ARG_LAT) else null
        val lng = if (args.containsKey(ARG_LNG)) args.getDouble(ARG_LNG) else null

        val message: String? = when {
            etat == ETAT_ABSENTE -> getString(R.string.carte_position_absente, titre)
            etat == ETAT_INVALIDE || !CoordinateValidator.estValide(lat, lng) ->
                getString(R.string.carte_position_invalide)
            !BuildConfig.MAPS_KEY_CONFIGURED -> getString(R.string.carte_cle_absente)
            else -> null
        }

        if (message != null) {
            binding.tvMessage.text = message
            binding.tvMessage.isVisible = true
            binding.mapContainer.isVisible = false
            binding.tvCoordonnees.isVisible = false
            return
        }

        position = LatLng(lat!!, lng!!)
        binding.tvMessage.isVisible = false
        binding.mapContainer.isVisible = true
        binding.tvCoordonnees.isVisible = true
        binding.tvCoordonnees.text = getString(
            R.string.coordonnees_affichees,
            String.format(Locale.US, "%.5f", lat), String.format(Locale.US, "%.5f", lng)
        )

        // Rotation : on réutilise la carte restaurée par le childFragmentManager (pas de doublon).
        val mapFragment = childFragmentManager.findFragmentByTag(MAP_TAG) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.map_container, it, MAP_TAG)
                }
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        val pos = position ?: return
        map.uiSettings.isZoomControlsEnabled = true
        map.clear()
        map.addMarker(MarkerOptions().position(pos).title(titre.ifBlank { getString(R.string.contact) }))
            ?.showInfoWindow()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, ZOOM))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "carte"
        const val ARG_ID = "contact_id"
        const val ARG_LAT = "latitude"
        const val ARG_LNG = "longitude"
        const val ARG_TITRE = "titre"
        const val ARG_ETAT = "etat"
        const val ETAT_VALIDE = "valide"
        const val ETAT_ABSENTE = "absente"
        const val ETAT_INVALIDE = "invalide"
        private const val MAP_TAG = "google_map"
        private const val ZOOM = 14f
    }
}
