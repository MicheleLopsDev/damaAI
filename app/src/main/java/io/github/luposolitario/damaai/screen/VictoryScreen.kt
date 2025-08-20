package io.github.luposolitario.damaai.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.luposolitario.damaai.R
import io.github.luposolitario.damaai.game_logic.Colore
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Confetti(
    val x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val rotation: Float
)

@Composable
fun VictoryScreen(
    winner: Colore,
    playerName: String,
    opponentName: String?,      // Accetta un nome nullable
    finalComment: String?,      // Accetta un commento nullable
    onPlayAgain: () -> Unit
) {
    var screenVisible by remember { mutableStateOf(false) }
    val winnerText = when {
        winner == Colore.BIANCO -> "Vince $playerName!"
        opponentName != null -> "Vince $opponentName!"
        else -> "Ha vinto l'IA!"
    }

    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "ScreenAlpha"
    )

    val trophyScale by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TrophyScale"
    )

    val confetti = remember { mutableStateListOf<Confetti>() }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        screenVisible = true
        val screenWidthPx = with(density) { 1080.dp.toPx() }
        repeat(150) {
            confetti.add(
                Confetti(
                    x = Random.nextFloat() * screenWidthPx,
                    y = -Random.nextFloat() * 1000,
                    size = Random.nextFloat() * 12 + 8,
                    color = listOf(Color(0xFFf44336), Color(0xFF2196f3), Color(0xFF4caf50), Color(0xFFffeb3b)).random(),
                    speed = Random.nextFloat() * 5 + 5,
                    rotation = Random.nextFloat() * 360
                )
            )
        }

        while (true) {
            delay(16)
            confetti.replaceAll { it.copy(y = it.y + it.speed, rotation = it.rotation + it.speed / 2) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.6f)
        ) {}

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            confetti.forEach {
                drawRect(
                    color = it.color,
                    topLeft = Offset(it.x, it.y),
                    size = androidx.compose.ui.geometry.Size(it.size, it.size)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.trophy_cup),
                contentDescription = "Trofeo della Vittoria",
                modifier = Modifier
                    .size(200.dp)
                    .scale(trophyScale)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = winnerText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.graphicsLayer {
                    shadowElevation = 8.dp.toPx()
                }
            )

            // Mostra il commento solo se esiste (non è null)
            if (opponentName != null && finalComment != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "\"$finalComment\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "- $opponentName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.End).padding(end = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onPlayAgain) {
                Text(text = "Gioca Ancora", fontSize = 18.sp)
            }
        }
    }
}