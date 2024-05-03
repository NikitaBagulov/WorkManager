package com.example.workmanager

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL
import kotlinx.coroutines.Dispatchers

data class WeatherData(
    @SerializedName("sys") val sys: Sys,
    @SerializedName("name") val name: String,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("wind") val wind: Wind
)

data class Sys(
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

data class Weather(
    @SerializedName("description") val description: String
)

data class Wind(
    @SerializedName("speed") val speed: Double
)

class WeatherActivity : AppCompatActivity() {
    private lateinit var cityEditText1: EditText
    private lateinit var cityEditText2: EditText

    private lateinit var weatherTextView1: TextView
    private lateinit var weatherTextView2: TextView

    private lateinit var windTextView1: TextView
    private lateinit var windTextView2: TextView

    private lateinit var cityNameTextView1: TextView
    private lateinit var cityNameTextView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityEditText1 = findViewById(R.id.editTextCity1)
        cityEditText2 = findViewById(R.id.editTextCity2)

        cityNameTextView1 = findViewById(R.id.cityNameTextView1)
        cityNameTextView2 = findViewById(R.id.cityNameTextView2)

        weatherTextView1 = findViewById(R.id.weatherTextView1)
        weatherTextView2 = findViewById(R.id.weatherTextView2)
        windTextView1 = findViewById(R.id.windTextView1)
        windTextView2 = findViewById(R.id.windTextView2)

        val refreshButton = findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            onRefreshButtonClick(it)
        }
    }

    fun onRefreshButtonClick(view: View) {
        val city1Name = cityEditText1.text.toString()
        val city2Name = cityEditText2.text.toString()

        if (city1Name.isEmpty() || city2Name.isEmpty()) {
            showToast("Please enter two cities!")
        } else {
            fetchWeatherData(city1Name) { weatherData ->
                updateWeatherInfo(cityNameTextView1, weatherTextView1, windTextView1, city1Name, weatherData)
            }
            fetchWeatherData(city2Name) { weatherData ->
                updateWeatherInfo(cityNameTextView2, weatherTextView2, windTextView2, city2Name, weatherData)
            }
        }
    }

    private fun fetchWeatherData(cityName: String, callback: (WeatherData) -> Unit) {
        val apiKey = getString(R.string.api_key)
        val weatherURL = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey&units=metric"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val stream = URL(weatherURL).openStream()
                val data = stream.bufferedReader().use { it.readText() }
                val weatherData = Gson().fromJson(data, WeatherData::class.java)
                launch(Dispatchers.Main) {
                    callback(weatherData)
                }
            } catch (e: Exception) {
                showToast("City not found!")
            }
        }
    }

    private fun updateWeatherInfo(cityNameTextView: TextView, weatherTextView: TextView, windTextView: TextView, cityName: String, weatherData: WeatherData) {
        cityNameTextView.text = cityName
        weatherTextView.text = "Weather: ${weatherData.weather[0].description}"
        windTextView.text = "Wind: ${weatherData.wind.speed}"
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}

