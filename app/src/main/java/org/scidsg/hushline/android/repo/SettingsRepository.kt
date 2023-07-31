package org.scidsg.hushline.android.repo

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.briarproject.onionwrapper.TorWrapper.LOG
import org.scidsg.hushline.android.HushLineNotificationManager
import org.scidsg.hushline.android.UIState
import org.scidsg.hushline.android.common.C
import org.scidsg.hushline.android.database.SettingsDao
import org.scidsg.hushline.android.database.SettingsEntity
import org.scidsg.hushline.android.server.MessagePage
import org.scidsg.hushline.android.server.WebServerManager
import org.scidsg.hushline.android.server.WebServerState
import org.scidsg.hushline.android.tor.TorManager
import org.scidsg.hushline.android.tor.TorState
import java.util.*
import java.util.logging.Level
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val torManager: TorManager,
    private val webServerManager: WebServerManager,
    private val notificationManager: HushLineNotificationManager
) {
    val runningStatus: LiveData<SettingsEntity> = settingsDao.getSetting(C.HUSHLINE_STATUS)
    val reuseStatus: LiveData<SettingsEntity> = settingsDao.getSetting(C.REUSE_ONION_ADDRESS)
    val emailNotification: LiveData<SettingsEntity> = settingsDao.getSetting(C.EMAIL_NOTIFICATION)
    val emailAddressData: LiveData<SettingsEntity> = settingsDao.getSetting(C.EMAIL_ADDRESS)
    val smtpAddressData: LiveData<SettingsEntity> = settingsDao.getSetting(C.SMTP_ADDRESS)
    val passwordData: LiveData<SettingsEntity> = settingsDao.getSetting(C.PASSWORD)
    val smtpPortData: LiveData<SettingsEntity> = settingsDao.getSetting(C.SMTP_PORT)
    val rotatePGP: LiveData<SettingsEntity> = settingsDao.getSetting(C.ROTATE_PGP)

    @Volatile
    private var startRunningJob: Job? = null

    val runState: StateFlow<UIState> = combineTransform(
        flow = torManager.state,
        flow2 = webServerManager.state,
    ) { t, w ->
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("New state from: t-${t::class.simpleName} w-${w::class.simpleName}")
        }

        if ((t is TorState.Starting || t is TorState.Started) && w is WebServerState.Stopped
        ) {
            webServerManager.start(MessagePage("Test Owner", "test key id",
                "test expiration"))
            val torPercent = (t as? TorState.Starting)?.progress ?: 0
            emit(UIState.Starting(torPercent))
        }
        else if (t is TorState.Started && w is WebServerState.Started) {
            val url = "http://\n${t.onion}.onion"
            notificationManager.onHushLineRunning()
            emit(UIState.Started(url))
        }
        else if (w is WebServerState.Stopping) {
            torManager.stop(false)
        }
        else if (t is TorState.Stopping) {
            emit(UIState.Stopping(""))
        }
        /*else if (
            (t is TorState.Stopping || t is TorState.Stopped || w is WebServerState.Stopped)
        ) {
            emit(UIState.Error())
            notificationManager.onError()
            val torFailed = (t as? TorState.Stopping)?.failedToConnect == true ||
                    (t as? TorState.Stopped)?.failedToConnect == true
            LOG.info("Tor failed: $torFailed")

            // state hack to ensure the webserver also stops when tor fails, so we add files again
            if (webServerManager.state.value !is WebServerState.Stopped) webServerManager.stop()
        }*/
        else if (t is TorState.Stopped || w is WebServerState.Stopped
        ) {
            emit(UIState.Stopped)
        }

    }.distinctUntilChanged().onEach {
        LOG.info("New state: ${it::class.simpleName}")
    }.stateIn(GlobalScope, SharingStarted.Lazily, UIState.Stopped)

    suspend fun setRunningStatus(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.HUSHLINE_STATUS, "$value"))
    }

    suspend fun setReuseStatus(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.REUSE_ONION_ADDRESS, "$value"))
    }

    suspend fun setEmailNotification(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.EMAIL_NOTIFICATION, "$value"))
    }

    suspend fun setEmailAddress(value: String) {
        settingsDao.insert(SettingsEntity(0, C.EMAIL_ADDRESS, value))
    }

    suspend fun setSMTPAddress(value: String) {
        settingsDao.insert(SettingsEntity(0, C.SMTP_ADDRESS, value))
    }

    suspend fun setPassword(value: String) {
        settingsDao.insert(SettingsEntity(0, C.PASSWORD, value))
    }

    suspend fun setSMTPPort(value: String) {
        settingsDao.insert(SettingsEntity(0, C.SMTP_PORT, value))
    }

    suspend fun setRotatePGP(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.ROTATE_PGP, "$value"))
    }

    suspend fun startHushLineRun() {
        if (startRunningJob?.isActive == true) {
            // TODO check if this always works as expected
            startRunningJob?.cancelAndJoin()
        }

        // Attention: We'll launch sharing in Global scope, so it survives ViewModel death,
        // because this gets called implicitly by the ViewModel in ViewModelScope
        @Suppress("OPT_IN_USAGE")
        startRunningJob = GlobalScope.launch(Dispatchers.IO) {
            coroutineScope {
                // call ensureActive() before any heavy work to ensure we don't continue when cancelled
                ensureActive()
                // start tor and onion service
                val torTask = async { torManager.start() }
                torTask.await()
            }
        }
    }

    suspend fun stopHushLineRun() {

    }

    suspend fun onStateChangeRequested() = when (runState.value) {
        is UIState.Starting -> stopHushLineRun()
        is UIState.Started -> stopHushLineRun()
        is UIState.Error -> startHushLineRun()
        is UIState.Stopping -> error("Pressing sheet button while stopping should not be possible")
        is UIState.Stopped -> error("")
        else -> {}
    }
}