package com.daegubus.alert

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*

class WatchService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val last = mutableMapOf<String, Int>()
    private val sent = mutableSetOf<String>()

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("watch", "감시 중", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel("alert", "버스 도착 알림", NotificationManager.IMPORTANCE_HIGH))
        startForeground(1, Notification.Builder(this, "watch").setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("버스 도착 감시 중").build())
        scope.launch { while (isActive) { poll(nm); delay(30_000) } }
        return START_STICKY
    }

    private fun poll(nm: NotificationManager) {
        try {
            val want = Prefs.routes(this).toSet()
            val list = BusApi.fetch(Prefs.stop(this)).filter { it.route in want }
            Live.arrivals.value = list; Live.error.value = ""
            list.forEach { a ->
                val m = a.min
                val prev = last[a.route]
                if (m == null || m > Prefs.LIMIT_MIN || (prev != null && m > prev + 2)) sent.remove(a.route)
                if (m != null) last[a.route] = m
                if (m != null && m <= Prefs.LIMIT_MIN && sent.add(a.route)) {
                    nm.notify(a.route.hashCode(), Notification.Builder(this, "alert")
                        .setSmallIcon(android.R.drawable.ic_dialog_info).setCategory(Notification.CATEGORY_ALARM)
                        .setContentTitle("${a.route}번 버스 곧 도착")
                        .setContentText(if (m == 0) a.state else "약 ${m}분 남음").setAutoCancel(true).build())
                }
            }
        } catch (e: Exception) { Live.error.value = "조회 실패: ${e.message}" }
    }

    override fun onDestroy() { scope.cancel() }

    companion object {
        fun start(c: Context) = c.startForegroundService(Intent(c, WatchService::class.java))
        fun stop(c: Context) = c.stopService(Intent(c, WatchService::class.java))
    }
}
