package com.gokturk.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val Navy = Color(0xFF071A2B)
private val DeepNavy = Color(0xFF04111D)
private val Turquoise = Color(0xFF25C6B7)
private val Gold = Color(0xFFD7AF58)
private val Cream = Color(0xFFF4F1E8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GokturkTheme { GokturkApp() } }
    }
}

@Composable
private fun GokturkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(primary = Turquoise, secondary = Gold, background = DeepNavy, surface = Navy, onPrimary = Navy, onBackground = Cream),
        typography = Typography(bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)),
        content = content
    )
}

@Composable
private fun GokturkApp() {
    val api = remember { GokturkApi() }
    val repo = remember { ChatRepository() }
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf(ChatMessage(role = "assistant", content = "Selam! Ben Göktürk. Bugün senin için ne yapabilirim?")) }
    var prompt by remember { mutableStateOf("") }
    var imageMode by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    fun submit() {
        val text = prompt.trim()
        if (text.isEmpty() || working) return
        prompt = ""
        val user = ChatMessage(role = "user", content = text)
        messages += user
        working = true
        scope.launch {
            repo.save(user)
            val answer = try {
                if (imageMode) {
                    val url = api.generateImage(text)
                    ChatMessage(content = "İstediğin görsel hazır.", imageUrl = url)
                } else ChatMessage(content = api.chat(messages))
            } catch (e: Exception) {
                ChatMessage(content = "Bağlantı kurulamadı: ${e.message ?: "Bilinmeyen hata"}")
            }
            messages += answer
            repo.save(answer)
            working = false
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(Modifier.fillMaxSize().background(DeepNavy)) {
        SeljukPattern(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {
            Header(imageMode) { imageMode = !imageMode }
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { MessageBubble(it) }
                if (working) item { LinearProgressIndicator(Modifier.width(96.dp), color = Turquoise, trackColor = Navy) }
            }
            PromptBar(prompt, imageMode, working, { prompt = it }, ::submit)
        }
    }
}

@Composable
private fun Header(imageMode: Boolean, toggle: () -> Unit) {
    Surface(color = Navy.copy(alpha = .96f), shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(46.dp), shape = CircleShape, color = Turquoise) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AutoAwesome, null, tint = Navy) }
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("GÖKTÜRK", color = Cream, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text(if (imageMode) "Görsel üretim modu" else "Türkçe dijital yardımcın", color = Gold, fontSize = 12.sp)
            }
            FilledTonalIconButton(onClick = toggle, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = if (imageMode) Gold else DeepNavy)) {
                Icon(Icons.Rounded.Image, "Görsel modunu değiştir", tint = if (imageMode) Navy else Turquoise)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val mine = message.role == "user"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Surface(
            modifier = Modifier.fillMaxWidth(.84f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (mine) 20.dp else 4.dp, bottomEnd = if (mine) 4.dp else 20.dp),
            color = if (mine) Turquoise else Navy.copy(alpha = .94f),
            tonalElevation = 3.dp
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(message.content, color = if (mine) Navy else Cream)
                message.imageUrl?.let { url ->
                    Spacer(Modifier.height(10.dp))
                    AsyncImage(model = url, contentDescription = "Göktürk tarafından üretilen görsel", modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp))
                }
            }
        }
    }
}

@Composable
private fun PromptBar(value: String, imageMode: Boolean, working: Boolean, onChange: (String) -> Unit, submit: () -> Unit) {
    Surface(color = Navy.copy(alpha = .98f)) {
        Row(Modifier.navigationBarsPadding().padding(12.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (imageMode) "Nasıl bir görsel oluşturalım?" else "Göktürk'e sor...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Turquoise, unfocusedBorderColor = Gold.copy(alpha = .45f), focusedTextColor = Cream, unfocusedTextColor = Cream)
            )
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(onClick = submit, modifier = Modifier.size(52.dp), containerColor = if (working) Gold else Turquoise, contentColor = Navy) {
                Icon(Icons.Rounded.Send, "Gönder")
            }
        }
    }
}

@Composable
private fun SeljukPattern(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val step = 145.dp.toPx()
        var y = 35.dp.toPx()
        while (y < size.height) {
            var x = 30.dp.toPx()
            while (x < size.width) {
                val radius = 28.dp.toPx()
                val path = Path()
                repeat(16) { i ->
                    val angle = -PI / 2 + i * PI / 8
                    val r = if (i % 2 == 0) radius else radius * .42f
                    val point = Offset(x + (cos(angle) * r).toFloat(), y + (sin(angle) * r).toFloat())
                    if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                path.close()
                drawPath(path, Gold.copy(alpha = .075f), style = Stroke(width = 1.5.dp.toPx()))
                drawRect(Turquoise.copy(alpha = .035f), topLeft = Offset(x-radius*.55f, y-radius*.55f), size = Size(radius*1.1f, radius*1.1f), style = Stroke(1.dp.toPx()))
                x += step
            }
            y += step
        }
    }
}
