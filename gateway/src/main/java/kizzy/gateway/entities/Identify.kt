package kizzy.gateway.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.my.kizzy.preference.Prefs

@Serializable
data class Identify(
    @SerialName("capabilities")
    val capabilities: Int,
    @SerialName("compress")
    val compress: Boolean,
    @SerialName("largeThreshold")
    val largeThreshold: Int,
    @SerialName("properties")
    val properties: Properties,
    @SerialName("token")
    val token: String
) {
    companion object {
        fun String.toIdentifyPayload(): Identify {
            val websocketConfig = Prefs[Prefs.WEBSOCKET_CONFIG, "Unknown"]
            val browser = when (websocketConfig) {
                "Discord Client" -> "Discord Client"
                "Discord Android" -> "Discord Android"
                else -> "Unknown"
            }

            return Identify(
                capabilities = 65,
                compress = false,
                largeThreshold = 100,
                properties = Properties(
                    browser = browser,
                    device = "ktor",
                    os = "Windows"
                ),
                token = this
            )
        }
    }
}

@Serializable
data class Properties(
    @SerialName("browser")
    val browser: String,
    @SerialName("device")
    val device: String,
    @SerialName("os")
    val os: String
)
