package com.example.data.repository

import android.util.Log
import com.example.data.local.CountryDao
import com.example.data.local.CountryEntity
import com.example.data.local.QuizHistoryDao
import com.example.data.local.QuizHistoryEntity
import com.example.data.model.Country
import com.example.data.remote.NetworkClient
import com.example.data.remote.PreseededData
import com.example.data.remote.RestCountriesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CountryRepository(
    private val countryDao: CountryDao,
    private val quizHistoryDao: QuizHistoryDao,
    private val api: RestCountriesApi = com.example.data.remote.NetworkClient.countryApiService
) {
    val allCountries: Flow<List<Country>> = countryDao.getAllCountries().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val favoriteCountries: Flow<List<Country>> = countryDao.getFavoriteCountries().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val quizHistory: Flow<List<QuizHistoryEntity>> = quizHistoryDao.getAllHistory()

    fun getHighScore(mode: String): Flow<Int?> = quizHistoryDao.getHighScoreForMode(mode)

    suspend fun syncCountries() = withContext(Dispatchers.IO) {
        try {
            // Check if DB has initial items, seed preseeded items first if empty
            val count = countryDao.getCount()
            if (count == 0) {
                countryDao.insertCountries(PreseededData.defaultCountries)
            }

            // Fetch live API data
            val response = api.getAllCountries()
            if (response.isSuccessful && response.body() != null) {
                val remoteCountries = response.body()!!
                // Fetch current local records to preserve favorites & mastery
                val existingMap = countryDao.getAllCountriesSync().associateBy { it.cca3 }.toMutableMap()

                remoteCountries.forEach { rest ->
                    val cca3 = rest.cca3 ?: return@forEach
                    val name = rest.name?.common ?: return@forEach
                    val officialName = rest.name.official ?: name
                    val capital = rest.capital?.firstOrNull() ?: "N/A"
                    val region = rest.region ?: "Other"
                    val subregion = rest.subregion ?: region
                    val population = rest.population ?: 0L
                    val flagPng = rest.flags?.png ?: "https://flagcdn.com/w320/${rest.cca2?.lowercase() ?: "un"}.png"
                    val flagSvg = rest.flags?.svg ?: ""
                    val flagAlt = rest.flags?.alt ?: "Flag of $name"
                    val currencies = rest.currencies?.entries?.joinToString(", ") { "${it.key} (${it.value.name ?: ""})" } ?: "N/A"
                    val languages = rest.languages?.values?.joinToString(", ") ?: "N/A"

                    val existing = existingMap[cca3]
                    val isFav = existing?.isFavorite ?: false
                    val mastery = existing?.masteryLevel ?: 0

                    val entity = CountryEntity(
                        cca3 = cca3,
                        cca2 = rest.cca2 ?: "",
                        name = name,
                        officialName = officialName,
                        capital = capital,
                        region = region,
                        subregion = subregion,
                        population = population,
                        flagPng = flagPng,
                        flagSvg = flagSvg,
                        flagAlt = flagAlt,
                        currencies = currencies,
                        languages = languages,
                        isFavorite = isFav,
                        masteryLevel = mastery
                    )
                    existingMap[cca3] = entity
                }

                if (existingMap.isNotEmpty()) {
                    countryDao.insertCountries(existingMap.values.toList())
                }
            }
        } catch (e: Exception) {
            Log.e("CountryRepository", "Failed to sync REST countries API: ${e.message}")
            // Fallback: if database is empty, seed preseeded data
            if (countryDao.getCount() == 0) {
                countryDao.insertCountries(PreseededData.defaultCountries)
            }
        }
    }

    suspend fun toggleFavorite(code: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        countryDao.updateFavorite(code, !currentFavorite)
    }

    suspend fun updateMasteryLevel(code: String, level: Int) = withContext(Dispatchers.IO) {
        countryDao.updateMasteryLevel(code, level)
    }

    suspend fun saveQuizHistory(
        mode: String,
        score: Int,
        total: Int,
        correctCount: Int? = null
    ) = withContext(Dispatchers.IO) {
        val effectiveCorrect = correctCount ?: if (score > total) (score / 100) else score
        val percentage = if (total > 0) ((effectiveCorrect * 100) / total).coerceIn(0, 100) else 0
        quizHistoryDao.insertHistory(
            QuizHistoryEntity(
                mode = mode,
                score = score,
                totalQuestions = total,
                accuracyPercentage = percentage
            )
        )
    }

    private fun CountryEntity.toDomainModel(): Country {
        return Country(
            code = cca3,
            cca2 = cca2,
            name = name,
            officialName = officialName,
            capital = capital,
            region = region,
            subregion = subregion,
            population = population,
            flagPng = flagPng,
            flagSvg = flagSvg,
            flagAlt = flagAlt,
            currencies = currencies,
            languages = languages,
            isFavorite = isFavorite,
            masteryLevel = masteryLevel
        )
    }
}
