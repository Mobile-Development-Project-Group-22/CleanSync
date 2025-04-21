package com.example.cleansync.utils


import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.cleansync.utils.NotificationUtils

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: "You have an upcoming booking."

        NotificationUtils.triggerNotification(
            context = applicationContext,
            title = title,
            message = message,
            scheduleTimeMillis = System.currentTimeMillis()
        )

        return Result.success()
        Log.d("NotificationWorker", "Executing scheduled notification.")

    }
}
