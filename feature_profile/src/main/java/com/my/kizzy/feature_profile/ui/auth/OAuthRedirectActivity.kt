package com.my.kizzy.feature_profile.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.my.kizzy.feature_profile.getUserInfo
import com.my.kizzy.preference.Prefs
import com.my.kizzy.preference.Prefs.TOKEN
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OAuthRedirectActivity : Activity() {
    private val httpClient = HttpClient(CIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data
        if (data == null) {
            finish()
            return
        }
        val code = data.getQueryParameter("code")
        val state = data.getQueryParameter("state")
        if (code.isNullOrEmpty()) {
            finish()
            return
        }

        lifecycleScope.launch {
            val token = exchangeCodeForToken(code)
            if (!token.isNullOrEmpty()) {
                Prefs[TOKEN] = token
                getUserInfo(token, onInfoSaved = {
                    val i = Intent()
                    setResult(RESULT_OK, i)
                    finish()
                })
            } else {
                finish()
            }
        }
    }

    private suspend fun exchangeCodeForToken(code: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://discord.com/api/v10/oauth2/token"
            val params = Parameters.build {
                append("client_id", "1292676584741802125")
                // when not using PKCE in a native app, include client_secret
                append("client_secret", "uToL-VesoeSIIcxq4RvUzoe3HA90lNwn")
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", "kizzy://oauth-callback")
            }
            val response: HttpResponse = httpClient.post(url) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(params.formUrlEncode())
            }
            val body = response.bodyAsText()
            val json = JSONObject(body)
            return@withContext json.optString("access_token", null)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
