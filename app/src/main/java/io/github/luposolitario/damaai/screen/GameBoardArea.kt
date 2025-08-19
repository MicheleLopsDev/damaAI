package io.github.luposolitario.damaai.screen

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import io.github.luposolitario.damaai.data.AiOpponent
import io.github.luposolitario.damaai.data.BoardStyle
import io.github.luposolitario.damaai.data.GameState
import io.github.luposolitario.damaai.data.PlayerColor
import io.github.luposolitario.damaai.data.TeamStyle
import io.github.luposolitario.damaai.game_logic.Posizione
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameBoardArea(
    gameState: GameState,
    playerTeamStyle: TeamStyle,
    aiTeamStyle: TeamStyle?,
    boardStyle: BoardStyle,
    selectedSquare: Posizione?,
    validMoveSquares: List<Posizione>,
    aiOpponent: AiOpponent?,
    onSquareClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerPainter: Painter? = if (playerTeamStyle.id != "default") {
        painterResource(id = playerTeamStyle.flagResId)
    } else {
        null
    }

    val aiPainter: Painter? = if (aiTeamStyle?.id != "default") {
        aiTeamStyle?.let { painterResource(id = it.flagResId) }
    } else {
        null
    }

    // Animazione per la dissolvenza della pedina catturata
    val capturedPieceAlpha by animateFloatAsState(
        targetValue = if (gameState.capturedPiece != null) 0f else 1f,
        animationSpec = tween(durationMillis = 0),
        label = "CapturedPieceAlpha"
    )

    // Animazione per il contorno lampeggiante
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkingBorder")
    val blinkingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlinkingAlpha"
    )
    val mandatoryCaptureColor = Color.Red.copy(alpha = blinkingAlpha)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val squareSize = size.width / 8f
                val row = (offset.y / squareSize).toInt().coerceIn(0, 7)
                val col = (offset.x / squareSize).toInt().coerceIn(0, 7)
                onSquareClick(row, col)
            }
        }
    ) {
        val squareSize = size.width / 8f

        // 1. Disegna la scacchiera e la selezione
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val isLightSquare = (row + col) % 2 == 0
                val squareColor =
                    if (isLightSquare) boardStyle.lightSquareColor else boardStyle.darkSquareColor
                drawRect(
                    color = squareColor,
                    topLeft = Offset(x = col * squareSize, y = row * squareSize),
                    size = Size(width = squareSize, height = squareSize)
                )

                if (selectedSquare != null && selectedSquare.riga == row && selectedSquare.colonna == col) {
                    drawRect(
                        color = Color.Yellow.copy(alpha = 0.5f),
                        topLeft = Offset(x = col * squareSize, y = row * squareSize),
                        size = Size(width = squareSize, height = squareSize)
                    )
                }
            }
        }

        // 2. Disegna i puntini per le mosse valide
        validMoveSquares.forEach { pos ->
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.4f),
                radius = squareSize * 0.15f,
                center = Offset(
                    x = pos.colonna * squareSize + squareSize / 2,
                    y = pos.riga * squareSize + squareSize / 2
                )
            )
        }

        // 3. Disegna tutte le pedine
        val piecesToDraw = if (gameState.capturedPiece != null && capturedPieceAlpha > 0f) {
            gameState.pieces + gameState.capturedPiece!!
        } else {
            gameState.pieces
        }

        piecesToDraw.forEach { piece ->
            val isCapturedPiece = piece == gameState.capturedPiece
            val alpha = if (isCapturedPiece) capturedPieceAlpha else 1f

            val center = Offset(
                x = piece.col * squareSize + squareSize / 2,
                y = piece.row * squareSize + squareSize / 2
            )
            val pieceRadius = squareSize * 0.38f

            // Ombra
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f * alpha),
                radius = pieceRadius,
                center = center.copy(y = center.y + 4f)
            )

            // Logica per disegnare la pedina (con bandiera o standard)
            if (piece.color == PlayerColor.WHITE) {
                drawCircle(color = Color.White.copy(alpha = alpha), radius = pieceRadius, center = center)
                drawCircle(color = Color(0xFFBBBBBB).copy(alpha = alpha), radius = pieceRadius, center = center, style = Stroke(width = squareSize * 0.04f))
            } else {
                drawCircle(color = Color(0xFF222222).copy(alpha = alpha), radius = pieceRadius, center = center)
                if (aiPainter != null) {
                    val clipPath = Path().apply { addOval(Rect(center = center, radius = pieceRadius)) }
                    clipPath(path = clipPath) {
                        translate(left = center.x - pieceRadius, top = center.y - pieceRadius) {
                            with(aiPainter) { draw(size = Size(pieceRadius * 2, pieceRadius * 2), alpha = alpha) }
                        }
                    }
                }
                drawCircle(color = Color.Black.copy(alpha = alpha), radius = pieceRadius, center = center, style = Stroke(width = squareSize * 0.04f))
            }

            if (piece.isDama) {
                val starPath = Path()
                val numPoints = 5
                val angle = (Math.PI / numPoints).toFloat()
                val radiusOuter = pieceRadius * 0.6f // Raggio esterno della stella
                val radiusInner = pieceRadius * 0.3f // Raggio interno della stella

                // Sposta il punto di partenza della stella
                starPath.moveTo(
                    center.x + radiusOuter * cos(0f),
                    center.y + radiusOuter * sin(0f)
                )
                // Disegna i segmenti della stella
                for (i in 1 until numPoints * 2) {
                    val radius = if (i % 2 == 0) radiusOuter else radiusInner
                    val x = center.x + radius * cos(i * angle)
                    val y = center.y + radius * sin(i * angle)
                    starPath.lineTo(x, y)
                }
                starPath.close()

                // Disegna il percorso della stella
                drawPath(
                    path = starPath,
                    color = Color(0xFFFFD700) // Un bel color oro
                )
            }
        }

        // 4. Disegna i contorni lampeggianti solo se c'è una pedina
        gameState.mandatoryCapturePieces.forEach { mandatoryPos ->
            // Controlla se c'è una pedina in questa posizione
            val pieceExists = gameState.pieces.any { it.row == mandatoryPos.riga && it.col == mandatoryPos.colonna }
            Log.d("GameBoardArea", "Checking mandatory capture at [${mandatoryPos.riga}, ${mandatoryPos.colonna}]. Piece exists: $pieceExists")
            if (pieceExists) {
                val center = Offset(
                    x = mandatoryPos.colonna * squareSize + squareSize / 2,
                    y = mandatoryPos.riga * squareSize + squareSize / 2
                )
                val pieceRadius = squareSize * 0.38f

                drawCircle(
                    color = mandatoryCaptureColor,
                    radius = pieceRadius + (squareSize * 0.05f), // Leggermente più grande del raggio della pedina
                    center = center,
                    style = Stroke(
                        width = squareSize * 0.08f, // Spessore del contorno
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}