package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar


class MainActivity : AppCompatActivity(), LocationListener,
    ActivityResultCallback<Map<String, Boolean>> {

    // App Layouts
    lateinit var layoutApp: LinearLayout
    lateinit var layoutTitleData: LinearLayout
    lateinit var layoutMainData: LinearLayout

    // API Loading Status
    lateinit var outAppStatus: TextView
    lateinit var progressBar: CircularProgressIndicator

    // Weather title data
    lateinit var outWeatherTemperature: TextView
    lateinit var outWeatherStatus: TextView
    // Weather wind data
    lateinit var outWeatherWindSpeed: TextView
    // Weather humidity data
    lateinit var outWeatherHumidity: TextView

    lateinit var locationManager: LocationManager
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), this)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // INIT Current time and main layout
        val currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        layoutApp = findViewById(R.id.wrapper)

        // SELECT Current background
        when (currentTime) {
            in 6..11 -> layoutApp.setBackgroundResource(R.drawable.background_morning)
            in 12..17 -> layoutApp.setBackgroundResource(R.drawable.background_lunch)
            in 18..21 -> layoutApp.setBackgroundResource(R.drawable.background_evening)
            else -> layoutApp.setBackgroundResource(R.drawable.background_night)
        }

        // INIT App Layouts
        layoutTitleData = findViewById(R.id.wrapper_title)
        layoutMainData = findViewById(R.id.wrapper_data)
        // INIT API Loading Status
        outAppStatus = findViewById(R.id.app_status)
        progressBar = findViewById(R.id.circularProgressIndicator)
        // INIT Weather title data
        outWeatherTemperature = findViewById(R.id.out_temperature)
        outWeatherStatus = findViewById(R.id.out_status)
        // INIT Weather wind data
        outWeatherWindSpeed = findViewById(R.id.out_wind_speed)
        // INIT Weather humidity data
        outWeatherHumidity = findViewById(R.id.out_humidity)

        checkPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                applicationContext,
                "Location permission not granted. Go to settings and provide them ",
                Toast.LENGTH_SHORT
            ).show()
            outAppStatus.text = "No permissions"
        }

        outAppStatus.text = "Update in progress..."
        progressBar.visibility = View.VISIBLE

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*60, 100F, this)
    }

    private fun checkPermissions(array: Array<String>) {
        requestPermission.launch(array)
    }
    override fun onLocationChanged(location: Location) {

        service.getData(longitude = location.longitude, latitude = location.latitude)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {

                    progressBar.visibility = View.INVISIBLE
                    outAppStatus.visibility = View.INVISIBLE

                    layoutTitleData.visibility = View.VISIBLE
                    layoutMainData.visibility = View.VISIBLE

                    parseResponse(response.body())
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(applicationContext,"Server " +  t.message, Toast.LENGTH_SHORT).show()
                }

            })

    }
    private fun parseResponse(response: ApiResponse?) {
        val temp = response?.currentWeather?.temperature
        val speed = response?.currentWeather?.windspeed
        val humidity = response?.currentWeather?.winddirection
        val code = response?.currentWeather?.weathercode
        val status = getWeatherStatus(code!!)


        outWeatherTemperature.text = "$temp °C"
        outWeatherStatus.text = status
        outWeatherWindSpeed.text = "$speed M/S"
        outWeatherHumidity.text = "$humidity %"
    }
    override fun onActivityResult(result: Map<String, Boolean>?) {

        if (result == null) return
        for (i in result) {
            if (!i.value) {
                Toast.makeText(
                    applicationContext,
                    "Permission ${i.key} not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }
    override fun onDestroy() {
        locationManager.removeUpdates(this)
        super.onDestroy()
    }
    private fun getWeatherStatus(code: Int) =
        when(code){
            0 -> "Clear sky"
            1,2,3 -> "Partly cloudy"
            45, 48 -> "Fog"
            51,53,55 -> "Drizzle"
            56,57 -> "Ice drizzle"
            61,63,65 -> "Дождь"
            66,67 -> "Freezing rain"
            71,73,75 -> "Snowfall"
            80,81,82 -> "Rainfall"
            else -> null
        }
}

