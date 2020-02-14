package vip.yazilim.p2g.android.service

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.GsonBuilder
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import vip.yazilim.p2g.android.R
import vip.yazilim.p2g.android.activity.MainActivity
import vip.yazilim.p2g.android.constant.GeneralConstants
import vip.yazilim.p2g.android.model.p2g.RoomInvite
import vip.yazilim.p2g.android.util.gson.ThreeTenGsonAdapter
import vip.yazilim.p2g.android.util.stomp.WebSocketClient


/**
 * @author mustafaarifsisman - 13.02.2020
 * @contact mustafaarifsisman@gmail.com
 */
class UserWebSocketService : Service() {
    private var userId: String? = null
    private lateinit var userWSClient: StompClient

    companion object {
        private val TAG = UserWebSocketService::class.simpleName
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userId = intent?.getStringExtra("userId")
        userId?.run {
            connectWebSocket(this)
            subscribe("/p2g/user/${this}/invites")
        }

        return START_STICKY
    }

    private fun connectWebSocket(userId: String) {
        userWSClient = WebSocketClient.getUserWebSocketClient(userId)
        userWSClient.run {
            connect()

            lifecycle()
                .subscribe({
                    when (it.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.i(TAG, it.toString())
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            Log.i(TAG, it.toString())
                        }
                        LifecycleEvent.Type.ERROR -> {
                            Log.i(TAG, it.toString())
                        }
                        else -> Log.i(TAG, it.toString())
                    }
                }, { t: Throwable? ->
                    Log.d(TAG, t?.message.toString())
                })
        }
    }

    private fun subscribe(destinationPath: String) {
        userWSClient.run {
            topic(destinationPath)
                .subscribe({
                    Log.d(GeneralConstants.LOG_TAG, it.payload)

                    val gsonBuilder = GsonBuilder()
                    val gson = ThreeTenGsonAdapter.registerLocalDateTime(gsonBuilder).create()

                    val roomInvite = gson.fromJson(it.payload, RoomInvite::class.java)

                    showInviteNotification(roomInvite)
                }, { t: Throwable? -> Log.d(GeneralConstants.LOG_TAG, t?.message.toString()) })
        }
    }

    private fun disconnect() {
        if (this::userWSClient.isInitialized) {
            userWSClient.disconnect()
        }
    }

    private fun showInviteNotification(roomInvite: RoomInvite) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "p2g_room_invite"
            val channelName: CharSequence = "Play2Gether"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification =
            NotificationCompat.Builder(baseContext, "room_invite")
                .setChannelId("p2g_room_invite")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.ic_launcher_foreground
                    )
                )
                .setContentTitle("Play2Gether")
                .setContentText(roomInvite.inviterId + " invited you to Play2Gether!")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .build()

        notificationManager.notify(0, notification)
    }

}