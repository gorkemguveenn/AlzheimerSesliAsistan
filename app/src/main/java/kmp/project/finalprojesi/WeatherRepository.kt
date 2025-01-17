import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object WeatherRepository {
    private const val API_KEY = "5c31e7b79d27a5c1e22ae8d36801456c"

    fun fetchWeather(cityName: String, callback: (String) -> Unit) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$API_KEY&units=metric&lang=tr"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                // Timeout değerlerini ayarlama
                connection.connectTimeout = 10000 // 10 saniye
                connection.readTimeout = 10000   // 10 saniye

                connection.connect()  // Bağlantıyı kur

                // Bağlantı durumunu kontrol etme
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()

                    // Yanıtı loglama
                    Log.d("WeatherRepository", "Response: $response")

                    if (response.contains("weather") && response.contains("main")) {
                        val jsonObject = JSONObject(response)
                        val description =
                            jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
                        val temp = jsonObject.getJSONObject("main").getDouble("temp")
                        val responseText =
                            "$cityName için hava durumu: $description, sıcaklık: $temp°C."
                        callback(responseText)
                    } else {
                        Log.e("WeatherRepository", "Invalid response: $response")
                        callback("Hava durumu alınamadı.")
                    }
                } else {
                    // Bağlantı hatası durumunda loglama
                    Log.e("WeatherRepository", "HTTP error: ${connection.responseCode}")
                    callback("Hava durumu alınamadı.")
                }

            } catch (e: Exception) {
                // Hata durumunda loglama
                Log.e("WeatherRepository", "Error: ${e.message}")
                e.printStackTrace()
                callback("Hava durumu alınamadı.")
            }
        }.start()
    }
}
