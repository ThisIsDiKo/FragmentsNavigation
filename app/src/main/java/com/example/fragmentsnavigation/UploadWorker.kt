package com.example.fragmentsnavigation

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.delay
import java.io.InputStream

class UploadWorker(
    appContext: Context,
    workerParameters: WorkerParameters
): CoroutineWorker(appContext, workerParameters) {

    private val TAG = this::class.java.simpleName

    private val notificationId = 1

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotification(progress: String): Notification {
        val channelId = "1"
        val title = "Notif channel"
        val cancel = "Cancel"
        val name = "this is my name"
        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val builder = Notification.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_baseline_delete_24)
            .setOngoing(true)
            .setProgress(0, 0, true)

        createNotificationChannel(channelId, name).also {
            builder.setChannelId(it.id)
        }
        return builder.build()
    }

    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }



    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1, createNotification("Hello from foreground service")
        )
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo{
        return ForegroundInfo(notificationId, createNotification(progress))
    }


    override suspend fun doWork(): Result {
        Log.e(TAG, "Hello from WorkManager")
        try{
            val orderId = inputData.getString(ORDER_ID_KEY)
            val userId = inputData.getString(USER_ID_KEY)
            val imagesUri = inputData.getStringArray(IMAGES_URI_ARRAY_KEY)

            Log.e(TAG, "Order id: $orderId, User id: $userId, Images: $imagesUri")

            val uploadRepository = (applicationContext as App).uploadRepository

            imagesUri?.forEach { uri ->
                Log.e(TAG, "Uploading image $uri")
                val inputStream = inputStreamFor(applicationContext, uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                uploadRepository.uploadImage(
                    orderId = orderId ?: throw IllegalStateException("order id is null"),
                    userId = userId ?: throw IllegalStateException("user id is null"),
                    image = bitmap
                )
            }

            Log.e(TAG, "Files upload completed")

            val outputData = workDataOf("OUTPUT_DATA" to "Output Successfull")
            return Result.success(outputData)
        }
        catch (e:Exception){
            return Result.failure()
        }

    }

    companion object {
        const val ASSET_PREFIX = "file:///android_asset/"

        /**
         * Creates an input stream which can be used to read the given `resourceUri`.
         *
         * @param context the application [Context].
         * @param resourceUri the [String] resourceUri.
         * @return the [InputStream] for the resourceUri.
         */
        fun inputStreamFor(
            context: Context,
            resourceUri: String
        ): InputStream? {

            // If the resourceUri is an Android asset URI, then use AssetManager to get a handle to
            // the input stream. (Stock Images are Asset URIs).
            return if (resourceUri.startsWith(ASSET_PREFIX)) {
                val assetManager = context.resources.assets
                assetManager.open(resourceUri.substring(ASSET_PREFIX.length))
            } else {
                // Not an Android asset Uri. Use a ContentResolver to get a handle to the input stream.
                val resolver = context.contentResolver
                resolver.openInputStream(Uri.parse(resourceUri))
            }
        }
    }
}