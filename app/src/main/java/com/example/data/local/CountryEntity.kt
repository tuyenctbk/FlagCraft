package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val cca3: String,
    val cca2: String,
    val name: String,
    val officialName: String,
    val capital: String,
    val region: String,
    val subregion: String,
    val population: Long,
    val flagPng: String,
    val flagSvg: String,
    val flagAlt: String,
    val currencies: String,
    val languages: String,
    val isFavorite: Boolean = false,
    val masteryLevel: Int = 0 // 0: Unstudied, 1: Learning, 2: Mastered
)
