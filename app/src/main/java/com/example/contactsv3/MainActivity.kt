package com.example.contactsv3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.contactsv3.databinding.ActivityMainBinding
import com.example.contactsv3.ui.liste.ListeContactsFragment

/**
 * Unique activité : elle héberge les fragments dans un FragmentContainerView
 * et centralise la navigation (replace + addToBackStack).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Rotation : le FragmentManager restaure lui-même les fragments.
        // On n'ajoute le fragment initial QUE au premier lancement -> pas de doublons.
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container, ListeContactsFragment(), ListeContactsFragment.TAG)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener { majBoutonRetour() }
        majBoutonRetour()
    }

    /** Ouvre un écran en l'empilant dans la back stack (bouton Retour = écran précédent). */
    fun naviguerVers(fragment: Fragment, tag: String) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            replace(R.id.fragment_container, fragment, tag)
            addToBackStack(tag)
        }
    }

    private fun majBoutonRetour() {
        supportActionBar?.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
