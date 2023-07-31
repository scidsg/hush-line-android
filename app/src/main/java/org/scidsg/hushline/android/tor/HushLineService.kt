package org.scidsg.hushline.android.tor

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.app.ServiceCompat.stopForeground
import dagger.hilt.android.AndroidEntryPoint
import org.scidsg.hushline.android.NOTIFICATION_ID_FOREGROUND
import org.scidsg.hushline.android.HushLineNotificationManager
import org.slf4j.LoggerFactory.getLogger
import javax.inject.Inject

private val LOG = getLogger(HushLineService::class.java)

@AndroidEntryPoint
class HushLineService : Service() {

    @Inject
    internal lateinit var nm: HushLineNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LOG.debug("onStartCommand $intent")
        startForeground(NOTIFICATION_ID_FOREGROUND, nm.getForegroundNotification())
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        LOG.debug("onDestroy")
        stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
