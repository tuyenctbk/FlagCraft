package com.example.data.remote

import com.example.data.model.RestCountry
import retrofit2.Response
import retrofit2.http.GET

interface RestCountriesApi {
    @GET("v3.1/all?fields=name,cca2,cca3,capital,region,subregion,population,flags,continents,currencies,languages")
    suspend fun getAllCountries(): Response<List<RestCountry>>
}
