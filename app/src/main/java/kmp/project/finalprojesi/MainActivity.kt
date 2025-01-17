package kmp.project.finalprojesi

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kmp.project.finalprojesi.ui.theme.FinalprojesiTheme
import java.util.Locale

import android.Manifest
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kmp.project.finalprojesi.MainActivity.Companion.REQUEST_CALL_PERMISSION
import kotlinx.coroutines.launch
import kotlin.math.sign

class MainActivity : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val REQUEST_CALL_PERMISSION = 1
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("tr", "TR")
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        enableEdgeToEdge()
        setContent {
            MainContent() // MainContent adında bir @Composable işlev çağırılır
        }
    }

    @Composable
    fun MainContent() {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val emergencyNumber = remember { mutableStateOf("") } // remember kullanımı @Composable işlev içinde

        FinalprojesiTheme {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        },
                        navController = navController,
                        onSaveEmergencyNumber = { number -> emergencyNumber.value = number } // Değer güncelleniyor
                    )
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("sign") {
                            sign(navController = navController)
                        }
                        composable("login") { login(navController = navController) }
                        composable("greeting") {
                            Greeting(
                                onVoiceCommandReceived = { spokenText ->
                                    handleVoiceCommand(spokenText)
                                },
                                onEmergencyCallClick = { makePhoneCall(emergencyNumber.value) } // Değer burada kullanılıyor
                            )
                        }
                    }
                }
            }
        }
    }



    private fun handleVoiceCommand(spokenText: String) {
        when {
            spokenText.contains("için hava durumu") -> {
                val cityName = spokenText.replace("için hava durumu", "").trim()
                WeatherRepository.fetchWeather(cityName) { weatherResponse ->
                    textToSpeech.speak(weatherResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
            spokenText.contains("neredeyim") -> {
                locations()
            }
            spokenText.equals("sen kimsin", ignoreCase = true) -> {
                textToSpeech.speak("ben senin kişisel asistanınım", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            spokenText.equals("ne yapıyorsun", ignoreCase = true) -> {
                textToSpeech.speak("sana yardımcı olmaya çalışıyorum", TextToSpeech.QUEUE_FLUSH, null, null)
            }
            else -> {
                textToSpeech.speak("Lütfen geçerli bir komut kullanın.", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makePhoneCall("000000")
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
    }

    fun locations() {
        textToSpeech.speak(
            "kastamonu şehri kuzeykent mahallesi sarı ömer desiniz",
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }
}

@Composable
fun Greeting(
    onVoiceCommandReceived: (String) -> Unit,
    onEmergencyCallClick: () -> Unit
) {
    HomePage(onVoiceCommandReceived = onVoiceCommandReceived, onEmergencyCallClick = onEmergencyCallClick)
}





@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FinalprojesiTheme {

    }
}