package org.scidsg.hushline.android.server

import android.net.TrafficStats
import android.util.Base64
import android.util.Base64.NO_PADDING
import android.util.Base64.URL_SAFE
import android.util.Log
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.pebbletemplates.pebble.loader.ClasspathLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.scidsg.hushline.android.model.SuccessResponse
import org.scidsg.hushline.android.repo.MessageRepository
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.concurrent.RejectedExecutionException
import javax.inject.Inject
import javax.inject.Singleton

private val LOG = LoggerFactory.getLogger(WebServerManager::class.java)
internal const val PORT: Int = 17638

sealed class WebServerState {
    object Starting : WebServerState()
    object Started : WebServerState()
    object Stopping : WebServerState()
    object Stopped : WebServerState()
}

@Singleton
class WebServerManager @Inject constructor(private val messageRepository: MessageRepository) {

    private val secureRandom = SecureRandom()
    private var server: ApplicationEngine? = null

    private val _state = MutableStateFlow<WebServerState>(WebServerState.Stopped)
    val state = _state.asStateFlow()

    fun start(messagePage: MessagePage) {
        _state.value = WebServerState.Starting
        val staticPath = getStaticPath()
        val staticPathMap = mapOf("static_url_path" to staticPath)
        TrafficStats.setThreadStatsTag(0x42)
        server = embeddedServer(
            factory = Netty,
            host = "127.0.0.1",
            port = PORT,
            watchPaths = emptyList(),
            configure = {
                // disable response timeout
                responseWriteTimeoutSeconds = 0
            }) {
            install(CallLogging) {
                logger = LOG
            }
            install(Pebble) {
                loader(ClasspathLoader().apply { prefix = "assets/web/templates" })
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }

            installStatusPages(staticPathMap)
            addListener()
            routing {
                defaultRoutes(staticPath)
                sendRoutes(messagePage, staticPathMap)
            }
        }.also { it.start() }
    }

    fun stop() {
        LOG.info("Stopping...")
        try {
            // Netty doesn't start to really shut down until gracePeriodMillis is over.
            // So we can't use Long.MAX_VALUE for this or the server will never stop.
            val timeout = 1000L
            server?.stop(timeout, timeout * 2)
        } catch (e: RejectedExecutionException) {
            LOG.warn("Error while stopping webserver", e)
        }
    }

    private fun getStaticPath(): String {
        val staticSuffixBytes = ByteArray(16).apply { secureRandom.nextBytes(this) }
        val staticSuffix =
            Base64.encodeToString(staticSuffixBytes, NO_PADDING or URL_SAFE).trimEnd()
        return "/static_$staticSuffix"
    }

    private fun Application.addListener() {
        environment.monitor.subscribe(ApplicationStarted) {
            _state.value = WebServerState.Started
        }
        environment.monitor.subscribe(ApplicationStopping) {
            // only update if we are not already stopping
            if (state.value !is WebServerState.Stopping) _state.value = WebServerState.Stopping
        }
        environment.monitor.subscribe(ApplicationStopped) {
            LOG.info("Stopped")
            //val downloadComplete = (state.value as? WebServerState.Stopping)?.downloadComplete ?: false
            _state.value = WebServerState.Stopped
            server = null
        }
    }

    private fun Application.installStatusPages(staticPathMap: Map<String, String>) {
        install(StatusPages) {
            status(HttpStatusCode.NotFound) { call, _ ->
                call.respond(PebbleContent("404.html", staticPathMap))
            }
            status(HttpStatusCode.MethodNotAllowed) { call, _ ->
                call.respond(PebbleContent("405.html", staticPathMap))
            }
            status(HttpStatusCode.InternalServerError) { call, _ ->
                call.respond(PebbleContent("500.html", staticPathMap))
            }
        }
    }

    private fun Route.defaultRoutes(staticPath: String) {
        staticResources("$staticPath/favicon", "assets/web/static/favicon")
        staticResources("$staticPath/fonts", "assets/web/static/fonts")
        staticResources("$staticPath/images", "assets/web/static/images")
        staticResources(staticPath, "assets/web/static")
    }

    private fun Route.sendRoutes(messagePage: MessagePage, staticPathMap: Map<String, String>) {
        get("/") {
            val model = messagePage.model + staticPathMap
            call.respond(PebbleContent("index.html", model))
        }
        post("/save_message") {
            Log.e("WebServerManager", "Post request received.")
            //val message = call.parameters["message"] //request.queryParameters["message"]
            val message = call.receiveParameters()["message"]
            message?.let {
                Log.e("WebServerManager", "Message received: $message")
                //todo wait to handle by message repo
                var messageSent = false
                val task = async {
                    messageSent = messageRepository.notifyMessage(message)
                }
                task.await()
                //FIXME content negotiation not working for some reason
                if (messageSent) {
                    Log.e("WebServerManager", "Message sent")
                    call.respondText("{\"success\": true}", ContentType.Application.Json, HttpStatusCode.OK)
                    //call.respond(SuccessResponse(true))
                } else {
                    Log.e("WebServerManager", "Message not sent")
                    call.respondText("{\"success\": false}", ContentType.Application.Json, HttpStatusCode.OK)
                    //call.respond(SuccessResponse(false))
                }
            } ?: //call.respond(HttpStatusCode.BadRequest, SuccessResponse(false))
            call.respondText("{\"success\": false}", ContentType.Application.Json, HttpStatusCode.BadRequest)
            //call.respond(HttpStatusCode.BadRequest, SuccessResponse(false))
        }
        get("/pgp_owner_info") {
            //todo return pgp data here
        }
    }
}
