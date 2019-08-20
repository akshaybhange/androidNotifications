package com.ab.demo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val MYNOTIF_CHANNEL_ID = "com.ab.demo.myNotif"
    private val MYNOTIF_CHANNEL_NAME = "MYNOTIF CHANNEL"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNotificationChannel()
        checkNotification()
    }

    private fun checkNotification() {
        val permission = isNotificationPermissionGranted()

        text_notif_permission.text = if (permission) {
            btn_create_notif.visibility = VISIBLE
            getString(R.string.msg_permission_granted)
        } else {
            showDialog()
            btn_create_notif.visibility = GONE
            getString(R.string.msg_permission_denied)
        }


        btn_create_notif.setOnClickListener {
            fireNotification()
        }
    }

    private fun fireNotification() {
        val builder = NotificationCompat
            .Builder(this, MYNOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check_notif)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            notify(Random().nextInt(), builder.build())
        }
    }

    private fun addNotificationChannel() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val myNotifChannel = NotificationChannel(
                MYNOTIF_CHANNEL_ID,
                MYNOTIF_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            myNotifChannel.enableLights(true)
            myNotifChannel.enableVibration(true)
            myNotifChannel.lightColor = Color.BLUE
            myNotifChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(myNotifChannel)
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (VERSION.SDK_INT >= VERSION_CODES.O) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                isChannelEnabled(MYNOTIF_CHANNEL_ID)
            } else false
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }

    private fun openNotificationSettings() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val intent = Intent(ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(EXTRA_APP_PACKAGE, packageName)
            startActivity(intent)
        } else {
            val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    @RequiresApi(VERSION_CODES.O)
    private fun isChannelEnabled(channelId: String): Boolean {
        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(channelId)
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    @RequiresApi(VERSION_CODES.O)
    private fun openChannelSettings(channelId: String) {
        val intent = Intent(ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(EXTRA_APP_PACKAGE, packageName)
        intent.putExtra(EXTRA_CHANNEL_ID, channelId)
        startActivity(intent)
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.msg_dialog_title))
        dialog.setMessage(getString(R.string.msg_dialog_body))
        dialog.setPositiveButton(getString(R.string.msg_dialog_btn_allow)) { _, _ ->
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                if (!isChannelEnabled(MYNOTIF_CHANNEL_ID))
                    openChannelSettings(MYNOTIF_CHANNEL_ID)
            } else {
                openNotificationSettings()
            }
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        checkNotification()
    }
}
