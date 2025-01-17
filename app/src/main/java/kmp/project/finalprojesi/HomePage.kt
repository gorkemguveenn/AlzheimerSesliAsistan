package kmp.project.finalprojesi

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.Calendar


@Composable
fun MedicationAlarmScreen() {
    var medicationList by remember { mutableStateOf(listOf<Medication>()) }
    val context = LocalContext.current // Context, alarm kurmak için gerekli

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        medicationList.forEachIndexed { index, medication ->
            MedicationRow(
                medication = medication,
                onTimeSelected = { time ->
                    medicationList = medicationList.toMutableList().apply {
                        this[index] = medication.copy(time = time)
                    }
                    // Alarm güncelleniyor
                    setAlarm(context, time)
                },
                onMedicationNameChanged = { name ->
                    medicationList = medicationList.toMutableList().apply {
                        this[index] = medication.copy(name = name)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "+" butonuna tıklanıldığında yeni bir Medication nesnesi ekleyin ve alarm kurun
        IconButton(onClick = {
            medicationList = medicationList + Medication(name = "", time = "")
            // Yeni eklenen ilacın zamanı boş olduğu için alarm kurma gerekliliği kontrol edilir
            val newMedication = medicationList.lastOrNull()
            if (newMedication != null && newMedication.time.isNotEmpty()) {
                setAlarm(context, newMedication.time)
            }
        }) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Medication",
                tint = Color.White // Burada tint özelliğiyle rengi beyaz yapıyoruz
            )
        }

    }
}


fun setAlarm(context: Context, time: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar = Calendar.getInstance().apply {
        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1) // Geçmişse bir sonraki güne kur
        }
    }

    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}



fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "alarm_channel"
        val channelName = "Medication Alarms"
        val channelDescription = "Channel for medication alarms"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current // Burada çağrı doğrudan yapılabilir
    val calendar = Calendar.getInstance()

    android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
            onTimeSelected(selectedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()

    // Dialog kapanması için çağrı
    onDismissRequest()
}

@Composable
fun MedicationRow(
    medication: Medication,
    onTimeSelected: (String) -> Unit,
    onMedicationNameChanged: (String) -> Unit
) {
    val context = LocalContext.current // Context, alarm kurmak için gerekli
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(medication.time) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = medication.name,
            onValueChange = { onMedicationNameChanged(it) },
            label = { Text("İlaç İsmi") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = { showTimePicker = true }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_access_time_24),
                contentDescription = "Set Time"
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        if (selectedTime.isNotEmpty()) {
            Text(text = "Alarm: $selectedTime", color = Color.White)
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                onTimeSelected = { time ->
                    selectedTime = time
                    onTimeSelected(time)
                    showTimePicker = false

                    // Alarm kuruluyor
                    setAlarm(context, time)
                }
            )
        }
    }
}


data class Medication(val name: String, val time: String)



@Composable
fun DrawerContent(
    onCloseDrawer: () -> Unit,
    navController: NavController,
    onSaveEmergencyNumber: (String) -> Unit // Yeni parametre
) {
    var emergencyNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp, top = 60.dp)) {
        Text(
            text = "Menü",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        MedicationAlarmScreen()
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = emergencyNumber,
            onValueChange = { emergencyNumber = it },
            label = { Text("Acil Durum Numarası") },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (emergencyNumber.isNotBlank()) {
                    onSaveEmergencyNumber(emergencyNumber) // Numarayı kaydet
                    message = "Numara kaydedildi!"
                } else {
                    message = "Lütfen bir numara giriniz."
                }
            },
            modifier = Modifier.size(210.dp, 40.dp),
            colors = ButtonDefaults.buttonColors(Color.Red)
        ) {
            Text(
                text = "Kaydet",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = Color.Green
        )
    }
}

@Composable
fun HomePage(
    onVoiceCommandReceived: (String) -> Unit,
    onEmergencyCallClick: () -> Unit
) {
    val speechRecognizerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                onVoiceCommandReceived(spokenText)
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Blue, Color.Black),
                    startY = 0.3f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(top = 70.dp, bottom = 70.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {},
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB0BEC5),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(50.dp)
                    .width(150.dp)
            ) {
                Text("Alarm")
            }

            Button(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                    }
                    speechRecognizerLauncher.launch(intent)
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(200.dp)
            ) {
                Text("Konuş")
            }

            Button(
                onClick = onEmergencyCallClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(50.dp)
                    .width(150.dp)
            ) {
                Text("Acil Arama")
            }
        }
    }
}