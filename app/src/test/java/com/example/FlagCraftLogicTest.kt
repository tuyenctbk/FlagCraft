package com.example

import com.example.data.model.Country
import com.example.data.remote.PreseededData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlagCraftLogicTest {

    @Test
    fun preseededData_containsCountriesWithValidFlagsAndNames() {
        val countries = PreseededData.defaultCountries
        assertTrue("Preseeded list should not be empty", countries.isNotEmpty())
        assertTrue("Should have at least 40 countries for quiz variety", countries.size >= 40)

        countries.forEach { country ->
            assertTrue("Country code must not be blank", country.cca3.isNotBlank())
            assertTrue("Country name must not be blank", country.name.isNotBlank())
            assertTrue("Flag URL must be valid", country.flagPng.startsWith("https://"))
            assertTrue("Capital must not be blank", country.capital.isNotBlank())
            assertTrue("Region must not be blank", country.region.isNotBlank())
        }
    }

    @Test
    fun countryDomainModel_calculatesAttributesCorrectly() {
        val sample = Country(
            code = "USA",
            cca2 = "US",
            name = "United States",
            officialName = "United States of America",
            capital = "Washington, D.C.",
            region = "Americas",
            subregion = "North America",
            population = 331893745L,
            flagPng = "https://flagcdn.com/w320/us.png",
            flagSvg = "https://flagcdn.com/us.svg",
            flagAlt = "Flag of United States",
            currencies = "USD ($)",
            languages = "English",
            isFavorite = true,
            masteryLevel = 2
        )

        assertEquals("USA", sample.code)
        assertEquals("United States", sample.name)
        assertTrue(sample.isFavorite)
        assertEquals(2, sample.masteryLevel)
    }
}
