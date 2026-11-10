package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries ORDER BY name ASC")
    fun getAllCountries(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries")
    suspend fun getAllCountriesSync(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE region = :region ORDER BY name ASC")
    fun getCountriesByRegion(region: String): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteCountries(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE cca3 = :code LIMIT 1")
    suspend fun getCountryByCode(code: String): CountryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Query("UPDATE countries SET isFavorite = :isFavorite WHERE cca3 = :code")
    suspend fun updateFavorite(code: String, isFavorite: Boolean)

    @Query("UPDATE countries SET masteryLevel = :masteryLevel WHERE cca3 = :code")
    suspend fun updateMasteryLevel(code: String, masteryLevel: Int)

    @Query("SELECT COUNT(*) FROM countries")
    suspend fun getCount(): Int
}
