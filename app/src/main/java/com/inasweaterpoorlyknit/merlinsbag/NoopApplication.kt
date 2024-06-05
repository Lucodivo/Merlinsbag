package com.inasweaterpoorlyknit.merlinsbag

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

const val NOOP_NOTIFICATION_CHANNEL = "com.inasweaterpoorlyknit.merlinsbag"

@HiltAndroidApp
class NoopApplication: Application(){

  private fun createNotificationChannel() {
    val name = "Merlinsbag"
    val descriptionText = "notification channel for Merlinsbag app"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(NOOP_NOTIFICATION_CHANNEL, name, importance).apply { description = descriptionText }
    val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }
}