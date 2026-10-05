package com.example.msgsaver

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    private val tick = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Screen(tick.intValue) } }
    }

    override fun onResume() {
        super.onResume()
        tick.intValue++
    }
}

@Composable
fun Screen(tick: Int) {
    val ctx = LocalContext.current
    var onlyDeleted by remember { mutableStateOf(true) }
    val enabled = NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)
    val items = remember(tick, onlyDeleted) { MessageStore(ctx).all(onlyDeleted) }
    val fmt = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        if (!enabled) {
            Text("Нужен доступ к уведомлениям, иначе приложение не увидит сообщения.")
            Button(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }) { Text("Включить доступ") }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = onlyDeleted, onCheckedChange = { onlyDeleted = it })
            Spacer(Modifier.width(8.dp))
            Text("Только удалённые")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { m ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(m.sender, fontWeight = FontWeight.Bold)
                        Text(m.text)
                        val app = MessageListener.APPS[m.pkg] ?: m.pkg
                        Text(
                            "$app • ${fmt.format(Date(m.time))}" + if (m.deleted) " • удалено" else "",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
