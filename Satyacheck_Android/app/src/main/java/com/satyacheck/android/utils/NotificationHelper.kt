package com.satyacheck.android.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.satyacheck.android.R
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.presentation.MainActivity

object NotificationHelper {
    
    private const val CHANNEL_ID = "misinformation_alerts"
    private const val NOTIFICATION_ID = 1001
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = "Misinformation alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showMisinformationAlert(context: Context, verdict: Verdict, explanation: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val titleResId = when (verdict) {
            Verdict.POTENTIALLY_MISLEADING -> R.string.verdicts_potentially_misleading
            Verdict.HIGH_MISINFORMATION_RISK -> R.string.verdicts_high_misinformation_risk
            Verdict.SCAM_ALERT -> R.string.verdicts_scam_alert
            else -> R.string.verdicts_credible
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(titleResId))
            .setContentText(explanation)
            .setStyle(NotificationCompat.BigTextStyle().bigText(explanation))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
