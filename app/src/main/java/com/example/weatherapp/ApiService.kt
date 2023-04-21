package com.example.weatherapp

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/v1/forecast")
    fun getData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") current: Boolean = true,
        @Query("past_days") days: Int = 10,
        @Query("hourly") value: String = "temperature_2m,relativehumidity_2m,windspeed_10m",
    ): Call<ApiResponse>
}

data class ApiResponse(
    @SerializedName("latitude") var latitude: Double? = null,
    @SerializedName("longitude") var longitude: Double? = null,
    @SerializedName("generationtime_ms") var generationtimeMs: Double? = null,
    @SerializedName("utc_offset_seconds") var utcOffsetSeconds: Int? = null,
    @SerializedName("timezone") var timezone: String? = null,
    @SerializedName("timezone_abbreviation") var timezoneAbbreviation: String? = null,
    @SerializedName("elevation") var elevation: Int? = null,
    @SerializedName("current_weather") var currentWeather: CurrentWeather? = CurrentWeather(),
    @SerializedName("hourly_units") var hourlyUnits: HourlyUnits? = HourlyUnits(),
    @SerializedName("hourly") var hourly: Hourly? = Hourly()
)

data class CurrentWeather(
    @SerializedName("temperature") var temperature: Double? = null,
    @SerializedName("windspeed") var windspeed: Double? = null,
    @SerializedName("winddirection") var winddirection: Int? = null,
    @SerializedName("weathercode") var weathercode: Int? = null,
    @SerializedName("is_day") var isDay: Int? = null,
    @SerializedName("time") var time: String? = null
)

data class Hourly(
    @SerializedName("time") var time: ArrayList<String> = arrayListOf(),
    @SerializedName("temperature_2m") var temperature2m: ArrayList<Double> = arrayListOf(),
    @SerializedName("relativehumidity_2m") var relativehumidity2m: ArrayList<Long> = arrayListOf(),
    @SerializedName("windspeed_10m") var windspeed10m: ArrayList<Double> = arrayListOf()
)

data class HourlyUnits(
    @SerializedName("time") var time: String? = null,
    @SerializedName("temperature_2m") var temperature2m: String? = null,
    @SerializedName("relativehumidity_2m") var relativehumidity2m: String? = null,
    @SerializedName("windspeed_10m") var windspeed10m: String? = null
)
