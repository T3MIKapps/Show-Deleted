package com.example.msgsaver

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MessageListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in APPS) return
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val sender = n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val text = n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        val store = MessageStore(this)
        if (DELETED_MARKERS.any { text.contains(it, ignoreCase = true) }) {
            store.markLastDeleted(sbn.packageName, sender)
        } else {
            store.insert(sbn.packageName, sender, text, sbn.postTime)
        }
    }

    companion object {
        val APPS = mapOf(
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WhatsApp Business",
            "org.telegram.messenger" to "Telegram",
            "org.thoughtcrime.securesms" to "Signal",
            "com.viber.voip" to "Viber",
            "com.facebook.orca" to "Messenger",
            "com.instagram.android" to "Instagram"
        )
        // Тексты, которыми мессенджеры заменяют уведомление при удалении
        val DELETED_MARKERS = listOf(
            "this message was deleted", "message deleted", "you deleted this message",
            "это сообщение удалено", "сообщение удалено", "удалённое сообщение", "удаленное сообщение"
        )
    }
}
