package kmp.project.finalprojesi

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Alarm tetiklendiğinde yapılacak işlemler (örneğin, bir bildirim gönderme)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setContentTitle("Alarm")
            .setContentText("Zaman geldi!")
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(0, notification)
        val mediaPlayer = MediaPlayer.create(context, R.raw.ses) // alarm_sound.mp3 dosyasını 'res/raw' klasörüne ekleyin
        mediaPlayer.start()
    }
}
