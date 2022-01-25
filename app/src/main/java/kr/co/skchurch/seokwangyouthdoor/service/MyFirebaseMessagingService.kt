package kr.co.skchurch.seokwangyouthdoor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.orhanobut.logger.Logger
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.co.skchurch.seokwangyouthdoor.MainActivity
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.data.NotificationType

class MyFirebaseMessagingService: FirebaseMessagingService() {
    companion object {
        private var TAG = MyFirebaseMessagingService::class.java.simpleName
        private const val CHANNEL_NAME = "Emoji Party"
        private const val CHANNEL_DESCRIPTION = "Emoji Party를 위한 채널"
        private const val CHANNEL_ID = "channel_id"
        private const val KEY_MSG_TYPE = "type"
        private const val KEY_MSG_TITLE = "title"
        private const val KEY_MSG_MESSAGE = "message"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.d("onNewToken token : $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Logger.d("onMessageReceived message : $remoteMessage")

        createNotificationChannel()

        val type = remoteMessage.data[KEY_MSG_TYPE] ?.let {
            NotificationType.valueOf(it)
        }
        val title = remoteMessage.data[KEY_MSG_TITLE]
        val message = remoteMessage.data[KEY_MSG_MESSAGE]

        type ?: return

        NotificationManagerCompat.from(this)
            .notify(type.id, createNotification(type, title, message))
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESCRIPTION

            val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notiManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(type: NotificationType, title: String?, message: String?): Notification {

        val notiBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent(type))
            .setAutoCancel(true)
        when(type) {
            NotificationType.NORMAL -> Unit
            NotificationType.EXPANDABLE -> {
                notiBuilder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "😀 😃 😄 😁 😆 😅 😂 🤣 🥲 ☺" +
                                    " 😊 😇 🙂 🙃 😉 😌 😍 🥰 😘 😗 😙 😚 😋 😛 " +
                                    "😝 😜 🤪 🤨 🧐 🤓 😎 🥸 🤩 🥳 😏 😒 😞 😔 😟 " +
                                    "😕 🙁 ☹ 😣 😖 😫 😩 🥺 😢 😭 😤 😠 😡 🤬 🤯 " +
                                    "😳 🥵 🥶 😱 😨 😰 😥 😓 🤗 🤔 🤭 🤫 🤥 😶 😐 😑"
                        )
                )
            }
            NotificationType.CUSTOM -> {
                notiBuilder.setStyle(
                    NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(
                        RemoteViews(
                            packageName,
                            R.layout.view_custom_notification
                        ).apply {
                            setTextViewText(R.id.title, title)
                            setTextViewText(R.id.message, message)
                        }
                    )
            }
        }
        return notiBuilder.build()
    }

    private fun createPendingIntent(type: NotificationType): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notiType", "${type.title} 타입")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, type.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }
}