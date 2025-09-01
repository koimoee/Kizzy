package com.my.kizzy.feature_profile.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.my.kizzy.feature_profile.getUserInfo
import com.my.kizzy.preference.Prefs
import com.my.kizzy.preference.Prefs.TOKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class OAuthRedirectActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data
        val code = data?.getQueryParameter("code")
        if (code.isNullOrEmpty()) {
            finish()
            return
        }

        lifecycleScope.launch {
            val token = exchangeCodeForToken(code)
            if (!token.isNullOrEmpty()) {
                Prefs[TOKEN] = token
                getUserInfo(token) {
                    setResult(RESULT_OK, Intent())
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    private suspend fun exchangeCodeForToken(code: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://discord.com/api/v10/oauth2/token")
            val postData = buildString {
                append("client_id=").append(URLEncoder.encode("1292676584741802125", "UTF-8"))
                append("&client_secret=").append(URLEncoder.encode("uToL-VesoeSIIcxq4RvUzoe3HA90lNwn", "UTF-8"))
                append("&grant_type=authorization_code")
                append("&code=").append(URLEncoder.encode(code, "UTF-8"))
                append("&redirect_uri=").append(URLEncoder.encode("https://mutsuki.prplmoe.me/kizzycallback", "UTF-8"))
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connectTimeout = 15000
                readTimeout = 15000
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(postData)
                writer.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }

            val json = JSONObject(body)
            return@withContext json.optString("access_token", null)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
