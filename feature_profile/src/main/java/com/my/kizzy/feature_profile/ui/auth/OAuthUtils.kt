package com.my.kizzy.feature_profile.ui.auth

import java.util.*

object OAuthUtils {
    private const val CLIENT_ID = "1292676584741802125"
    private const val REDIRECT_URI = "kizzy://oauth-callback"
    private const val SCOPE = "identify rpc.activities.write"
    private const val DISCORD_AUTHORIZE = "https://discord.com/api/oauth2/authorize"

    fun buildAuthorizationUrl(): String {
        val state = UUID.randomUUID().toString()
        val sb = StringBuilder()
        sb.append(DISCORD_AUTHORIZE)
        sb.append("?client_id=").append(CLIENT_ID)
        sb.append("&redirect_uri=").append(UriEncode(REDIRECT_URI))
        sb.append("&response_type=code")
        sb.append("&scope=").append(UriEncode(SCOPE))
        sb.append("&state=").append(state)
        return sb.toString()
    }

    private fun UriEncode(text: String): String = java.net.URLEncoder.encode(text, "utf-8")
}
