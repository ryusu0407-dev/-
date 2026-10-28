package com.daegubus.alert

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Arr(val route: String, val state: String, val min: Int?)

object Live { val arrivals = MutableStateFlow<List<Arr>>(emptyList()); val error = MutableStateFlow("") }

object Prefs {
    const val LIMIT_MIN = 6
    private fun p(c: Context) = c.getSharedPreferences("p", Context.MODE_PRIVATE)
    fun stop(c: Context) = p(c).getString("stop", "") ?: ""
    fun routes(c: Context) = (p(c).getString("routes", "848,425,724") ?: "").split(",").filter { it.isNotBlank() }
    fun save(c: Context, stop: String, routes: List<String>) =
        p(c).edit().putString("stop", stop).putString("routes", routes.joinToString(",")).apply()
}

object BusApi {
    // ⚠ 공공데이터포털 '대구광역시_버스정보시스템' 활용가이드에서 주소/파라미터명/응답태그를 꼭 확인하세요.
    private const val URL_REALTIME = "https://apis.data.go.kr/6270000/dbmsapi01/getRealtime"

    fun fetch(stopId: String): List<Arr> {
        val q = "$URL_REALTIME?serviceKey=${URLEncoder.encode(BuildConfig.API_KEY, "UTF-8")}&bsId=$stopId&routeNo="
        val c = URL(q).openConnection() as HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 8000
        val body = c.inputStream.bufferedReader().use { it.readText() }
        return Regex("<routeNo>(.*?)</routeNo>.*?<arrState>(.*?)</arrState>", RegexOption.DOT_MATCHES_ALL)
            .findAll(body).map { Arr(it.groupValues[1].trim(), it.groupValues[2].trim(), parseMin(it.groupValues[2])) }.toList()
    }

    fun parseMin(s: String): Int? {
        Regex("(\\d+)").find(s)?.let { return it.groupValues[1].toInt() }
        return if (s.contains("곧") || s.contains("전") || s.contains("진입")) 0 else null
    }
}
