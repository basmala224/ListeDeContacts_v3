package com.example.contactsv3

import com.example.contactsv3.util.CoordResult
import com.example.contactsv3.util.CoordinateValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinateValidatorTest {

    @Test fun deuxChampsVides_positionAbsente() {
        assertEquals(CoordResult.Absente, CoordinateValidator.valider("", "  "))
    }

    @Test fun valeursValides() {
        assertEquals(CoordResult.Valide(34.74, 10.76), CoordinateValidator.valider("34.74", "10.76"))
    }

    @Test fun virguleDecimaleAcceptee() {
        assertEquals(CoordResult.Valide(34.74, 10.76), CoordinateValidator.valider("34,74", "10,76"))
    }

    @Test fun bornesIncluses() {
        assertTrue(CoordinateValidator.valider("-90", "180") is CoordResult.Valide)
        assertTrue(CoordinateValidator.valider("90", "-180") is CoordResult.Valide)
    }

    @Test fun horsLimites() {
        val r = CoordinateValidator.valider("91", "-181") as CoordResult.Erreur
        assertEquals(CoordinateValidator.ERR_LAT_HORS_LIMITES, r.champLatitude)
        assertEquals(CoordinateValidator.ERR_LNG_HORS_LIMITES, r.champLongitude)
    }

    @Test fun nonNumerique() {
        val r = CoordinateValidator.valider("abc", "10") as CoordResult.Erreur
        assertEquals(CoordinateValidator.ERR_NON_NUMERIQUE, r.champLatitude)
        assertEquals(null, r.champLongitude)
    }

    @Test fun nanRefuse() {
        assertTrue(CoordinateValidator.valider("NaN", "10") is CoordResult.Erreur)
    }

    @Test fun uneSeuleCoordonnee() {
        val r = CoordinateValidator.valider("36.8", "") as CoordResult.Erreur
        assertEquals(CoordinateValidator.ERR_MANQUANTE, r.champLongitude)
    }
}
