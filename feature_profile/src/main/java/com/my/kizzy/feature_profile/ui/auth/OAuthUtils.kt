package com.my.kizzy.feature_profile.ui.auth

import java.util.*

object OAuthUtils {
    private const val CLIENT_ID = "1292676584741802125"
    private const val REDIRECT_URI = "https://mutsuki.prplmoe.me/kizzycallback"
    private const val SCOPE = "identify rpc.activities.write"
    private const val DISCORD_AUTHORIZE = "https://discord.com/api/oauth2/authorize"

    fun buildAuthorizationUrl(): String {
        val state = UUID.randomUUID().toString()
        return buildString {
            append(DISCORD_AUTHORIZE)
            append("?client_id=").append(CLIENT_ID)
            append("&redirect_uri=").append(UriEncode(REDIRECT_URI))
            append("&response_type=code")
            append("&scope=").append(UriEncode(SCOPE))
            append("&state=").append(state)
        }
    }

    private fun UriEncode(text: String): String = java.net.URLEncoder.encode(text, "utf-8")
}
