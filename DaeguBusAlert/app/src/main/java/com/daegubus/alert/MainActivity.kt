package com.daegubus.alert

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContent { MaterialTheme { Screen() } }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    fun Screen() {
        var stop by remember { mutableStateOf(Prefs.stop(this@MainActivity)) }
        var routes by remember { mutableStateOf(Prefs.routes(this@MainActivity)) }
        var newRoute by remember { mutableStateOf("") }
        var running by remember { mutableStateOf(false) }
        val arr by Live.arrivals.collectAsState()
        val err by Live.error.collectAsState()
        val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
        LaunchedEffect(Unit) { perm.launch(Manifest.permission.POST_NOTIFICATIONS) }

        Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("대구 버스 도착 알림", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(stop, { stop = it }, label = { Text("정류장 ID (bsId)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                routes.forEach { r -> InputChip(true, { routes = routes - r }, label = { Text("$r ×") }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(newRoute, { newRoute = it }, label = { Text("버스 추가") }, singleLine = true, modifier = Modifier.weight(1f))
                Button({ if (newRoute.isNotBlank() && newRoute !in routes) routes = routes + newRoute.trim(); newRoute = "" }) { Text("추가") }
            }
            Button({
                if (!running) { Prefs.save(this@MainActivity, stop.trim(), routes); WatchService.start(this@MainActivity) }
                else WatchService.stop(this@MainActivity)
                running = !running
            }, Modifier.fillMaxWidth()) { Text(if (running) "감시 중지" else "감시 시작 (${Prefs.LIMIT_MIN}분 이내 알림)") }
            if (err.isNotEmpty()) Text(err, color = MaterialTheme.colorScheme.error)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(arr) { a ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${a.route}번", style = MaterialTheme.typography.titleLarge)
                            Text(a.state, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
