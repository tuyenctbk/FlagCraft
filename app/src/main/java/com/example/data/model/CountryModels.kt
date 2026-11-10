package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Raw REST Countries API v3.1 Response Models
 */
@JsonClass(generateAdapter = true)
data class RestCountry(
    @Json(name = "cca2") val cca2: String? = null,
    @Json(name = "cca3") val cca3: String? = null,
    @Json(name = "name") val name: RestCountryName? = null,
    @Json(name = "capital") val capital: List<String>? = null,
    @Json(name = "region") val region: String? = null,
    @Json(name = "subregion") val subregion: String? = null,
    @Json(name = "population") val population: Long? = null,
    @Json(name = "flags") val flags: RestCountryFlags? = null,
    @Json(name = "continents") val continents: List<String>? = null,
    @Json(name = "currencies") val currencies: Map<String, RestCurrency>? = null,
    @Json(name = "languages") val languages: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class RestCountryName(
    @Json(name = "common") val common: String? = null,
    @Json(name = "official") val official: String? = null
)

@JsonClass(generateAdapter = true)
data class RestCountryFlags(
    @Json(name = "png") val png: String? = null,
    @Json(name = "svg") val svg: String? = null,
    @Json(name = "alt") val alt: String? = null
)

@JsonClass(generateAdapter = true)
data class RestCurrency(
    @Json(name = "name") val name: String? = null,
    @Json(name = "symbol") val symbol: String? = null
)

/**
 * Domain & Moshi-serializable Country Model
 * Defines essential properties: ISO code, common name, official name, flag URL, capital, region, population.
 */
@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "code") val code: String = "", // ISO 3166-1 alpha-3 code (e.g. "USA")
    @Json(name = "cca2") val cca2: String = "", // ISO 3166-1 alpha-2 code (e.g. "US")
    @Json(name = "name") val name: String = "", // Common country name
    @Json(name = "officialName") val officialName: String = "",
    @Json(name = "capital") val capital: String = "",
    @Json(name = "region") val region: String = "",
    @Json(name = "subregion") val subregion: String = "",
    @Json(name = "population") val population: Long = 0L,
    @Json(name = "flagPng") val flagPng: String = "", // Flag image URL
    @Json(name = "flagSvg") val flagSvg: String = "",
    @Json(name = "flagAlt") val flagAlt: String = "",
    @Json(name = "currencies") val currencies: String = "",
    @Json(name = "languages") val languages: String = "",
    @Json(name = "isFavorite") val isFavorite: Boolean = false,
    @Json(name = "masteryLevel") val masteryLevel: Int = 0 // 0 = Unstudied, 1 = Learning, 2 = Mastered
)
